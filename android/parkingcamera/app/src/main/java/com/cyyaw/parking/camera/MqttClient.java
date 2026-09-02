package com.cyyaw.parking.camera;

import android.os.Handler;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MQTT 客户端封装（基于 Eclipse Paho 异步客户端）。
 *
 * <p>负责连接 broker、自动重连、发布与订阅，并通过主线程 {@link Handler} 回调 UI，
 * 避免 Paho 工作线程直接触碰界面。
 *
 * <p>主题约定（与后端 parking-gate 的 {@code /server/parking/#} 匹配）：
 * <ul>
 *   <li>上行（发布）：{@code /server/parking/{clientId}/plate}，车牌识别事件</li>
 *   <li>下行（订阅）：{@code /client/parking/{clientId}/cmd}，后端将来下发开闸等指令</li>
 * </ul>
 *
 * <p>注意：Paho 的 automaticReconnect 只在<b>首次连接成功后</b>的网络掉线时自动重连，
 * 因此首次连接失败由本类用 Handler 退避重试。
 */
public class MqttClient implements MqttCallbackExtended {

    private static final String TAG = "MqttClient";
    private static final long RETRY_DELAY_MS = 5000L;
    private static final int KEEP_ALIVE_SEC = 60;
    private static final int CONNECT_TIMEOUT_SEC = 30;

    public enum Status { IDLE, CONNECTING, CONNECTED, DISCONNECTED }

    public interface StatusListener {
        void onStatus(Status status, String address);
    }

    public interface MessageListener {
        void onMessage(String topic, byte[] payload);
    }

    private String address;
    private String username;
    private String password;
    private String clientId;

    private final StatusListener statusListener;
    private final Handler uiHandler;
    private MessageListener messageListener;

    private MqttAsyncClient client;
    private volatile Status status = Status.IDLE;

    /** cleanSession=true 下重连后需重新订阅，记录期望订阅的主题。 */
    private final Map<String, Integer> subscriptions = new LinkedHashMap<>();

    /** 首次连接失败的退避重试 Runnable（单实例，便于精确 removeCallbacks）。 */
    private final Runnable retryRunnable = () -> {
        synchronized (MqttClient.this) {
            retryPending = false;
            connectInternal();
        }
    };
    private boolean retryPending = false;
    private boolean manualDisconnect = false;

    public MqttClient(String address, String username, String password, String clientId,
                      StatusListener statusListener, Handler uiHandler) {
        this.address = address == null ? "" : address.trim();
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
        this.clientId = normalizeClientId(clientId);
        this.statusListener = statusListener;
        this.uiHandler = uiHandler;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // ───── 生命周期 ─────

    /** 用构造时的配置发起连接（已连接则空操作）。 */
    public synchronized void connect() {
        connectInternal();
    }

    private void connectInternal() {
        if (address.isEmpty()) {
            setStatus(Status.IDLE);
            return;
        }
        if (manualDisconnect) manualDisconnect = false; // 主动重连，取消挂起的拆除标记
        if (client != null && client.isConnected()) return; // 已连接
        if (retryPending) return; // 已有重试在排队

        if (client == null) {
            try {
                client = new MqttAsyncClient(address, clientId, new MemoryPersistence());
                client.setCallback(this);
            } catch (MqttException e) {
                Log.e(TAG, "create client failed", e);
                setStatus(Status.DISCONNECTED);
                scheduleRetry();
                return;
            }
        }

        setStatus(Status.CONNECTING);
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setKeepAliveInterval(KEEP_ALIVE_SEC);
        opts.setConnectionTimeout(CONNECT_TIMEOUT_SEC);
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        if (!username.isEmpty()) {
            opts.setUserName(username);
            opts.setPassword(password.toCharArray());
        }
        try {
            client.connect(opts, null, new IMqttActionListener() {
                @Override public void onSuccess(IMqttToken asyncActionToken) {
                    // 连接真正建立由 connectComplete() 通知
                }
                @Override public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "connect failed: " + exception);
                    setStatus(Status.DISCONNECTED);
                    scheduleRetry();
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "connect call failed", e);
            setStatus(Status.DISCONNECTED);
            scheduleRetry();
        }
    }

    /** 主动断开（onStop 等）；Paho 对显式 disconnect() 不会触发 automaticReconnect。 */
    public synchronized void disconnect() {
        manualDisconnect = true;
        cancelRetry();
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                Log.w(TAG, "disconnect failed", e);
            }
        }
        setStatus(Status.IDLE);
    }

    /** 用最新配置重连（MQTT 设置保存后调用，address/clientId 可能已变）。 */
    public synchronized void reconnect(String address, String username, String password, String clientId) {
        this.address = address == null ? "" : address.trim();
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
        this.clientId = normalizeClientId(clientId);
        cancelRetry();
        manualDisconnect = true; // 拆除旧连接期间抑制重试
        MqttAsyncClient old = client;
        client = null;
        if (old != null && old.isConnected()) {
            try { old.disconnect(); } catch (MqttException e) { Log.w(TAG, "old disconnect failed", e); }
        }
        manualDisconnect = false;
        connectInternal();
    }

    // ───── 发布 / 订阅 ─────

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public Status getStatus() {
        return status;
    }

    public String getClientId() {
        return clientId;
    }

    /** 非阻塞发布，未连接时返回 false。 */
    public boolean publish(String topic, byte[] payload, int qos) {
        if (!isConnected()) return false;
        try {
            MqttMessage msg = new MqttMessage(payload);
            msg.setQos(qos);
            msg.setRetained(false);
            client.publish(topic, msg);
            return true;
        } catch (MqttException e) {
            Log.w(TAG, "publish failed: " + topic, e);
            return false;
        }
    }

    public void subscribe(String topic, int qos) {
        synchronized (subscriptions) {
            subscriptions.put(topic, qos);
        }
        if (isConnected()) {
            try {
                client.subscribe(topic, qos);
            } catch (MqttException e) {
                Log.w(TAG, "subscribe failed: " + topic, e);
            }
        }
    }

    /** 清空期望订阅集合（配合 clientId 变更后重新登记）。 */
    public void clearSubscriptions() {
        synchronized (subscriptions) {
            subscriptions.clear();
        }
    }

    /** cleanSession 下重连成功后把期望订阅重新应用到当前会话。 */
    private void resubscribeAll() {
        MqttAsyncClient c = client;
        if (c == null || !c.isConnected()) return;
        synchronized (subscriptions) {
            for (Map.Entry<String, Integer> e : subscriptions.entrySet()) {
                try {
                    c.subscribe(e.getKey(), e.getValue());
                } catch (MqttException ex) {
                    Log.w(TAG, "resubscribe failed: " + e.getKey(), ex);
                }
            }
        }
    }

    // ───── 退避重试 ─────

    private void scheduleRetry() {
        if (manualDisconnect || retryPending) return;
        retryPending = true;
        uiHandler.postDelayed(retryRunnable, RETRY_DELAY_MS);
    }

    private void cancelRetry() {
        retryPending = false;
        uiHandler.removeCallbacks(retryRunnable);
    }

    // ───── Paho 回调（Paho 工作线程触发） ─────

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (manualDisconnect) {
            // 主动断开期间收到连接完成，直接再断开
            try { if (client != null) client.disconnect(); } catch (MqttException ignored) {}
            return;
        }
        Log.i(TAG, "connect complete (reconnect=" + reconnect + ") " + serverURI);
        setStatus(Status.CONNECTED);
        resubscribeAll();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.w(TAG, "connection lost: " + cause);
        setStatus(Status.DISCONNECTED);
        // Paho automaticReconnect 会自动重连，无需手动重试
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        final MessageListener l = messageListener;
        if (l == null) return;
        final byte[] payload = message.getPayload();
        uiHandler.post(() -> l.onMessage(topic, payload));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // 无需处理
    }

    // ───── 辅助 ─────

    private void setStatus(Status s) {
        status = s;
        if (statusListener != null) {
            final Status cur = s;
            uiHandler.post(() -> statusListener.onStatus(cur, address));
        }
    }

    private static String normalizeClientId(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            return "parking-cam-" + Long.toHexString(System.currentTimeMillis());
        }
        return clientId.trim();
    }
}
