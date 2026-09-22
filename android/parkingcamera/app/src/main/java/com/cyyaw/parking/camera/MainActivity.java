package com.cyyaw.parking.camera;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cyyaw.parking.camera.entity.PlateRecognitionRequest;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 停车场摄像头主界面：全屏横屏预览（Camera2 + SurfaceView，按相机宽高比信箱裁切
 * 不变形），底部状态栏含摄像头切换 / 网络状态 / MQTT·信令·回调状态 / 设置入口。
 *
 * <p>核心能力：
 * <ul>
 *   <li>HyperLPR3 + onnxruntime 本地车牌识别（后台线程节流推理，置信度 + 冷却去重）</li>
 *   <li>识别结果双通道上报：HTTP 回调 与 MQTT 发布（{@code /server/parking/{clientId}/plate}）</li>
 *   <li>MQTT 下行指令订阅（{@code /client/parking/{clientId}/cmd}），onStart 连接、onStop 断开</li>
 *   <li>设置面板四组配置（基础信息 / 信令 / MQTT / 车牌回调）全部持久化到 SharedPreferences</li>
 * </ul>
 */
public class MainActivity extends BaseActivity {

    // 相机权限请求码 + 预览分辨率上限（择优时不超过此上限，避免过高分辨率拖慢识别）
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1440;

    // ───── 相机预览（Camera2） ─────
    private AspectRatioSurfaceView cameraSurface;
    private CameraManager cameraManager;
    private String cameraId;

    private Size previewSize;        // 选定一次后保持稳定，避免反复重建会话
    private boolean surfaceConfigured = false;   // Surface 尺寸已应用

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private boolean surfaceReady = false;   // SurfaceHolder 已创建
    private boolean opening = false;        // openCamera 进行中，防重入

    // License-plate recognition (HyperLPR3, onnxruntime)
    private static final int LPR_MAX_FPS = 3;              // 识别节流
    private ImageReader lprReader;                         // YUV_420_888 帧
    private LprRecognizer lpr;
    private HandlerThread lprThread;
    private Handler lprHandler;
    private boolean lprBusy = false;
    private long lastLprAt = 0;
    private TextView plateResult;
    private PlateOverlayView plateOverlay;

    // 车牌回调上报：去重 + 冷却
    private static final float LPR_REPORT_MIN_CONF = 0.85f;   // 置信度低于此不上报
    private static final long LPR_REPORT_COOLDOWN_MS = 5000;  // 同一车牌 5 秒内只报一次
    private String lastReportedPlate = null;
    private long lastReportAt = 0;

    // Bottom bar views
    private ImageView cameraSwitchIcon;
    private TextView networkStatus;
    private TextView mqttStatus;
    private TextView signalingStatus;
    private TextView callbackStatus;
    private ImageView settingsIcon;

    // Settings panel views
    private View settingsOverlay;
    private View settingsPanel;
    private ImageView closePanel;
    private TextView bottomCameraId;
    private TextView bottomBarrierId;

    // MQTT settings
    private View mqttSettingsRow;
    private TextView mqttSummary;

    // MQTT client (Eclipse Paho)
    private MqttClient mqtt;
    private static final String TOPIC_PLATE_PREFIX = "/server/parking/";
    private static final String TOPIC_PLATE_SUFFIX = "/plate";
    private static final String TOPIC_CMD_PREFIX = "/client/parking/";
    private static final String TOPIC_CMD_SUFFIX = "/cmd";

    // License-plate recognition callback URL
    private View lprCallbackRow;
    private TextView lprCallbackSummary;

    // Basic info: timestamp toggle / camera ID / barrier ID
    private View basicInfoRow;
    private TextView basicInfoSummary;

    // Signaling server address
    private View signalingServerRow;
    private TextView signalingServerSummary;

    private static final String PREFS_NAME = "parking_settings";
    private static final String KEY_MQTT_ADDRESS = "mqtt_address";
    private static final String KEY_MQTT_USERNAME = "mqtt_username";
    private static final String KEY_MQTT_PASSWORD = "mqtt_password";
    private static final String KEY_MQTT_CLIENT_ID = "mqtt_client_id";
    private static final String KEY_LPR_CALLBACK = "lpr_callback";
    private static final String KEY_TIMESTAMP_VISIBLE = "timestamp_visible";
    private static final String KEY_CAMERA_ID = "camera_id";
    private static final String KEY_BARRIER_ID = "barrier_id";
    private static final String KEY_SIGNALING_SERVER = "signaling_server";

    // 默认值：首次启动写入 SharedPreferences，使设置面板的信息持久化保存
    private static final String DEFAULT_MQTT_ADDRESS = "tcp://192.168.83.38:1883";
    private static final String DEFAULT_MQTT_USERNAME = "admin";
    private static final String DEFAULT_MQTT_PASSWORD = "123456";
    private static final String DEFAULT_MQTT_CLIENT_ID = "aaa";
    private static final String DEFAULT_LPR_CALLBACK = "http://192.168.83.38:18080/api/plate/recognize";
    private static final String DEFAULT_SIGNALING_SERVER = "ws://192.168.83.38:8080";
    private static final boolean DEFAULT_TIMESTAMP_VISIBLE = true;
    private static final String DEFAULT_CAMERA_ID = "CAM-001";
    private static final String DEFAULT_BARRIER_ID = "B-001";

    // Camera IDs for the spinner
    private List<String> allCameraIds = new ArrayList<>();

    // Panel animation state
    private boolean isPanelOpen = false;

    /**
     * 拦截返回键：面板打开时先收起面板，否则交由系统退出。
     */
    private OnBackPressedCallback backPressedCallback;

    // Time display
    private TextView timeDisplay;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
    private boolean timeVisible = true;

    /**
     * Surface 生命周期：创建→应用预览尺寸并开相机；尺寸变化→重建预览会话；
     * 销毁→标记不可用并关会话。
     */
    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surfaceReady = true;
            configureSurface(holder);
            openCamera();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            // (Re)create the preview session once the surface has its final size.
            startPreview();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            surfaceReady = false;
            closePreviewSession();
        }
    };

    /**
     * 相机打开回调：成功→保存设备并启动预览；断开/出错→关相机并提示。
     */
    private final CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            opening = false;
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            opening = false;
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            opening = false;
            camera.close();
            cameraDevice = null;
            toast("Camera error: " + error);
        }
    };

    /**
     * 初始化：写默认配置 → 绑定视图 → 启动 LPR 引擎 → 恢复持久化基础信息 →
     * 装配相机切换 / 网络监听 / 设置面板 / 返回键拦截 / MQTT 客户端 / 时间刷新，
     * 最后选后置摄像头并注册 Surface 回调（真正开相机在 onResume）。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 首次启动写入默认值，使设置面板信息持久化保存
        ensureDefaultSettings();

        cameraSurface = findViewById(R.id.cameraSurface);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Initialize bottom bar views
        cameraSwitchIcon = findViewById(R.id.cameraSwitchIcon);
        networkStatus = findViewById(R.id.networkStatus);
        mqttStatus = findViewById(R.id.mqttStatus);
        signalingStatus = findViewById(R.id.signalingStatus);
        callbackStatus = findViewById(R.id.callbackStatus);
        settingsIcon = findViewById(R.id.settingsIcon);

        // Initialize settings panel views
        settingsOverlay = findViewById(R.id.settingsOverlay);
        settingsPanel = findViewById(R.id.settingsPanel);
        closePanel = findViewById(R.id.closePanel);
        bottomCameraId = findViewById(R.id.bottomCameraId);
        bottomBarrierId = findViewById(R.id.bottomBarrierId);
        mqttSettingsRow = findViewById(R.id.mqttSettingsRow);
        mqttSummary = findViewById(R.id.mqttSummary);
        lprCallbackRow = findViewById(R.id.lprCallbackRow);
        lprCallbackSummary = findViewById(R.id.lprCallbackSummary);
        basicInfoRow = findViewById(R.id.basicInfoRow);
        basicInfoSummary = findViewById(R.id.basicInfoSummary);
        signalingServerRow = findViewById(R.id.signalingServerRow);
        signalingServerSummary = findViewById(R.id.signalingServerSummary);

        // Initialize time display
        timeDisplay = findViewById(R.id.timeDisplay);

        // Initialize LPR UI + background engine
        plateResult = findViewById(R.id.plateResult);
        plateOverlay = findViewById(R.id.plateOverlay);
        startLprEngine();

        // Restore persisted basic-info state into the bottom bar + overlay
        SharedPreferences bootPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bottomCameraId.setText(bootPrefs.getString(KEY_CAMERA_ID, DEFAULT_CAMERA_ID));
        bottomBarrierId.setText(bootPrefs.getString(KEY_BARRIER_ID, DEFAULT_BARRIER_ID));
        timeVisible = bootPrefs.getBoolean(KEY_TIMESTAMP_VISIBLE, DEFAULT_TIMESTAMP_VISIBLE);
        timeDisplay.setVisibility(timeVisible ? View.VISIBLE : View.GONE);

        // Setup camera switch
        setupCameraSwitch();

        // Setup network monitoring
        setupNetworkMonitor();

        // Setup settings panel
        setupSettingsPanel();

        // 返回键拦截：面板打开时收起面板，关闭时才允许系统退出
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeSettingsPanel();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Build MQTT client (connects in onStart, reconnects on settings save)
        setupMqttClient();

        // Start time updater
        startTimeUpdater();

        cameraId = pickBackCameraId();
        cameraSurface.getHolder().addCallback(surfaceCallback);
    }

    /**
     * Populate camera list and set up the camera switch icon to cycle through cameras.
     */
    private void setupCameraSwitch() {
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                allCameraIds.add(id);
            }
        } catch (CameraAccessException e) {
            toast("Cannot access camera list");
        }

        if (allCameraIds.isEmpty()) return;

        // Click to cycle to next camera
        cameraSwitchIcon.setOnClickListener(v -> {
            if (allCameraIds.size() <= 1) {
                toast("只有一个摄像头可用");
                return;
            }
            int currentIndex = allCameraIds.indexOf(cameraId);
            int nextIndex = (currentIndex + 1) % allCameraIds.size();
            String newId = allCameraIds.get(nextIndex);
            if (!newId.equals(cameraId)) {
                cameraId = newId;
                surfaceConfigured = false;
                previewSize = null;
                closeCamera();
                openCamera();
            }
        });
    }

    /**
     * Monitor network connectivity and update the status indicator.
     */
    private void setupNetworkMonitor() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        // Initial check
        updateNetworkStatus(cm);

        // Listen for changes
        NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
        cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }
        });
    }

    /**
     * 根据当前活动网络刷新底部网络指示器：绿点=有互联网，红点=无。
     */
    private void updateNetworkStatus(ConnectivityManager cm) {
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        boolean connected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        Drawable dot = ContextCompat.getDrawable(this, R.drawable.ic_network_dot);
        if (dot != null) {
            dot = dot.mutate();
            dot.setTint(connected ? 0xFF66FF66 : 0xFFFF4444);
            networkStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(dot, null, null, null);
        }
        networkStatus.setText(connected ? "网络已连接" : "网络未连接");
    }

    /**
     * 底部状态药丸：左侧彩色圆点 + 文字。文字保持中性白，状态用圆点颜色表达（与网络指示器一致）。
     */
    private void setStatusPill(TextView view, String text, int dotColor) {
        Drawable dot = ContextCompat.getDrawable(this, R.drawable.ic_network_dot);
        if (dot != null) {
            dot = dot.mutate();
            dot.setTint(dotColor);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(dot, null, null, null);
        }
        view.setText(text);
    }

    /**
     * 底部 MQTT 状态药丸：按 MqttClient.Status 切颜色（绿=已连接 / 琥珀=连接中 / 红=未连接），地址为空显示"未配置"。
     */
    private void updateMqttStatusBar() {
        String address = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_MQTT_ADDRESS, "");
        if (address.isEmpty()) {
            setStatusPill(mqttStatus, "MQTT未配置", 0x80FFFFFF);
            return;
        }
        MqttClient.Status st = mqtt != null ? mqtt.getStatus() : MqttClient.Status.IDLE;
        switch (st) {
            case CONNECTED:
                setStatusPill(mqttStatus, "MQTT已连接", 0xFF66FF66);   // 绿
                break;
            case CONNECTING:
                setStatusPill(mqttStatus, "MQTT连接中", 0xFFFFC107);  // 琥珀
                break;
            default:                                                  // IDLE / DISCONNECTED
                setStatusPill(mqttStatus, "MQTT未连接", 0xFFFF4444);  // 红
                break;
        }
    }

    /**
     * 底部信令状态药丸：仅按地址是否非空判断"已配置/未配置"（信令连接尚未实现）。
     */
    private void updateSignalingStatusBar() {
        String server = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_SIGNALING_SERVER, "");
        setStatusPill(signalingStatus, server.isEmpty() ? "信令未配置" : "信令已配置", server.isEmpty() ? 0x80FFFFFF : 0xFF66FF66);
    }

    /**
     * 底部车牌回调状态药丸：仅按 URL 是否非空判断"已配置/未配置"。
     */
    private void updateCallbackStatusBar() {
        String url = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_LPR_CALLBACK, "");
        setStatusPill(callbackStatus, url.isEmpty() ? "回调未配置" : "回调已配置", url.isEmpty() ? 0x80FFFFFF : 0xFF66FF66);
    }

    // ───── Settings sliding panel ─────

    /**
     * 绑定设置面板各控件：开/关面板、四行设置项分别打开对应配置弹窗，并刷新各自摘要。
     */
    private void setupSettingsPanel() {
        // Settings icon opens the panel
        settingsIcon.setOnClickListener(v -> openSettingsPanel());

        // Close button inside the panel
        closePanel.setOnClickListener(v -> closeSettingsPanel());

        // Tapping the dimming overlay also closes the panel
        settingsOverlay.setOnClickListener(v -> closeSettingsPanel());

        // MQTT settings row opens the config dialog
        mqttSettingsRow.setOnClickListener(v -> showMqttSettingsDialog());
        updateMqttSummary();

        // License-plate callback row opens the config dialog
        lprCallbackRow.setOnClickListener(v -> showLprCallbackDialog());
        updateLprCallbackSummary();

        // Basic info row opens the config dialog
        basicInfoRow.setOnClickListener(v -> showBasicInfoDialog());
        updateBasicInfoSummary();

        // Signaling server row opens the config dialog
        signalingServerRow.setOnClickListener(v -> showSignalingServerDialog());
        updateSignalingServerSummary();
    }

    // ───── MQTT settings ─────

    /**
     * 设置面板里 MQTT 行的摘要：地址 + 连接状态，颜色随状态变化。
     */
    private void updateMqttSummary() {
        updateMqttStatusBar();
        String address = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_MQTT_ADDRESS, "");
        if (address.isEmpty()) {
            mqttSummary.setText("未配置");
            mqttSummary.setTextColor(0x80FFFFFF);
            return;
        }
        MqttClient.Status st = mqtt != null ? mqtt.getStatus() : MqttClient.Status.IDLE;
        String label;
        int color;
        switch (st) {
            case CONNECTED:
                label = "已连接";
                color = 0xFF66FF66;  // 绿
                break;
            case CONNECTING:
                label = "连接中";
                color = 0xFFFFC107;  // 琥珀
                break;
            default:                  // IDLE / DISCONNECTED
                label = "未连接";
                color = 0xFFFF4444;  // 红
                break;
        }
        mqttSummary.setText(address + " · " + label);
        mqttSummary.setTextColor(color);
    }

    /**
     * MQTT 设置弹窗：地址 / 用户名 / 密码 / ClientId。保存后写回 SharedPreferences，
     * 用新配置重连，并按新 clientId 重新登记下行指令主题（clientId 变了旧主题作废）。
     */
    private void showMqttSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_mqtt_settings);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText addressInput = dialog.findViewById(R.id.dlgMqttAddressInput);
        EditText usernameInput = dialog.findViewById(R.id.dlgMqttUsernameInput);
        EditText passwordInput = dialog.findViewById(R.id.dlgMqttPasswordInput);
        EditText clientIdInput = dialog.findViewById(R.id.dlgMqttClientIdInput);

        addressInput.setText(prefs.getString(KEY_MQTT_ADDRESS, DEFAULT_MQTT_ADDRESS));
        usernameInput.setText(prefs.getString(KEY_MQTT_USERNAME, DEFAULT_MQTT_USERNAME));
        passwordInput.setText(prefs.getString(KEY_MQTT_PASSWORD, DEFAULT_MQTT_PASSWORD));
        clientIdInput.setText(prefs.getString(KEY_MQTT_CLIENT_ID, DEFAULT_MQTT_CLIENT_ID));

        dialog.findViewById(R.id.dlgMqttSave).setOnClickListener(b -> {
            String addr = addressInput.getText().toString().trim();
            String user = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            String cid = clientIdInput.getText().toString().trim();
            prefs.edit().putString(KEY_MQTT_ADDRESS, addr).putString(KEY_MQTT_USERNAME, user).putString(KEY_MQTT_PASSWORD, pass).putString(KEY_MQTT_CLIENT_ID, cid).apply();
            updateMqttSummary();
            // 用新配置重连；clientId 可能变化，按新 clientId 重新登记下行指令主题
            if (mqtt != null) {
                mqtt.reconnect(addr, user, pass, cid);
                mqtt.clearSubscriptions();
                mqtt.subscribe(TOPIC_CMD_PREFIX + mqtt.getClientId() + TOPIC_CMD_SUFFIX, 1);
            }
            toast("已保存");
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 首次启动时把默认值写入 SharedPreferences，使设置面板的信息持久化保存
     * （而非仅作为 {@code getString} 的回退默认值）。仅写入尚未存在的键，用户在
     * 弹窗里改过的配置不受影响。基础信息 / 信令 / MQTT / 车牌回调 四组设置全部
     * 预置默认值并持久化；其中 MQTT 地址等服务器地址为占位值，用户应在设置弹窗
     * 里改成真实地址后保存。
     */
    private void ensureDefaultSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        if (!prefs.contains(KEY_MQTT_ADDRESS)) e.putString(KEY_MQTT_ADDRESS, DEFAULT_MQTT_ADDRESS);
        if (!prefs.contains(KEY_MQTT_USERNAME))
            e.putString(KEY_MQTT_USERNAME, DEFAULT_MQTT_USERNAME);
        if (!prefs.contains(KEY_MQTT_PASSWORD))
            e.putString(KEY_MQTT_PASSWORD, DEFAULT_MQTT_PASSWORD);
        if (!prefs.contains(KEY_MQTT_CLIENT_ID))
            e.putString(KEY_MQTT_CLIENT_ID, DEFAULT_MQTT_CLIENT_ID);
        if (!prefs.contains(KEY_LPR_CALLBACK)) e.putString(KEY_LPR_CALLBACK, DEFAULT_LPR_CALLBACK);
        if (!prefs.contains(KEY_SIGNALING_SERVER))
            e.putString(KEY_SIGNALING_SERVER, DEFAULT_SIGNALING_SERVER);
        if (!prefs.contains(KEY_TIMESTAMP_VISIBLE))
            e.putBoolean(KEY_TIMESTAMP_VISIBLE, DEFAULT_TIMESTAMP_VISIBLE);
        if (!prefs.contains(KEY_CAMERA_ID)) e.putString(KEY_CAMERA_ID, DEFAULT_CAMERA_ID);
        if (!prefs.contains(KEY_BARRIER_ID)) e.putString(KEY_BARRIER_ID, DEFAULT_BARRIER_ID);
        e.apply();
    }

    /**
     * 从持久化配置构造 MQTT 客户端，注册指令监听 + 预登记下行主题；真正连接发生在 onStart。
     */
    private void setupMqttClient() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String addr = prefs.getString(KEY_MQTT_ADDRESS, DEFAULT_MQTT_ADDRESS);
        String user = prefs.getString(KEY_MQTT_USERNAME, DEFAULT_MQTT_USERNAME);
        String pass = prefs.getString(KEY_MQTT_PASSWORD, DEFAULT_MQTT_PASSWORD);
        String cid = prefs.getString(KEY_MQTT_CLIENT_ID, DEFAULT_MQTT_CLIENT_ID);
        mqtt = new MqttClient(addr, user, pass, cid, (status, address) -> updateMqttSummary(), new Handler(Looper.getMainLooper()));
        mqtt.setMessageListener((topic, payload) -> {
            String msg = payload == null ? "" : new String(payload, StandardCharsets.UTF_8);
            toast("收到指令: " + msg);
            Log.i("Mqtt", "cmd received: " + topic + " = " + msg);
        });
        // 预登记下行指令主题；连接建立后由 resubscribeAll 自动订阅
        mqtt.subscribe(TOPIC_CMD_PREFIX + mqtt.getClientId() + TOPIC_CMD_SUFFIX, 1);
    }

    // ───── License-plate recognition callback ─────

    /**
     * 设置面板里车牌回调行的摘要：显示回调 URL，未配置则显示"未配置"。
     */
    private void updateLprCallbackSummary() {
        updateCallbackStatusBar();
        String url = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_LPR_CALLBACK, "");
        lprCallbackSummary.setText(url.isEmpty() ? "未配置" : url);
    }

    /**
     * 车牌回调 URL 设置弹窗，保存后写回 SharedPreferences。
     */
    private void showLprCallbackDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_lpr_callback);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText callbackInput = dialog.findViewById(R.id.dlgLprCallbackInput);
        callbackInput.setText(prefs.getString(KEY_LPR_CALLBACK, DEFAULT_LPR_CALLBACK));
        dialog.findViewById(R.id.dlgLprSave).setOnClickListener(b -> {
            prefs.edit().putString(KEY_LPR_CALLBACK, callbackInput.getText().toString().trim()).apply();
            updateLprCallbackSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    // ───── Basic info: timestamp / camera ID / barrier ID ─────

    /**
     * 设置面板里基础信息行的摘要：摄像头ID · 道闸ID。
     */
    private void updateBasicInfoSummary() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cameraIdVal = prefs.getString(KEY_CAMERA_ID, DEFAULT_CAMERA_ID);
        String barrierIdVal = prefs.getString(KEY_BARRIER_ID, DEFAULT_BARRIER_ID);
        basicInfoSummary.setText(cameraIdVal + " · " + barrierIdVal);
    }

    /**
     * 基础信息弹窗：时间戳开关 / 摄像头ID / 道闸ID；保存后同步刷新底栏与时间显示。
     */
    private void showBasicInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_basic_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        androidx.appcompat.widget.SwitchCompat timestampSw = dialog.findViewById(R.id.dlgTimestampSwitch);
        EditText cameraIdInput = dialog.findViewById(R.id.dlgCameraIdInput);
        EditText barrierIdDlg = dialog.findViewById(R.id.dlgBarrierIdInput);

        timestampSw.setChecked(prefs.getBoolean(KEY_TIMESTAMP_VISIBLE, DEFAULT_TIMESTAMP_VISIBLE));
        cameraIdInput.setText(prefs.getString(KEY_CAMERA_ID, DEFAULT_CAMERA_ID));
        barrierIdDlg.setText(prefs.getString(KEY_BARRIER_ID, DEFAULT_BARRIER_ID));

        dialog.findViewById(R.id.dlgBasicSave).setOnClickListener(b -> {
            boolean checked = timestampSw.isChecked();
            String camId = cameraIdInput.getText().toString().trim();
            String barId = barrierIdDlg.getText().toString().trim();
            if (camId.isEmpty()) camId = DEFAULT_CAMERA_ID;
            if (barId.isEmpty()) barId = DEFAULT_BARRIER_ID;
            prefs.edit().putBoolean(KEY_TIMESTAMP_VISIBLE, checked).putString(KEY_CAMERA_ID, camId).putString(KEY_BARRIER_ID, barId).apply();
            timeVisible = checked;
            timeDisplay.setVisibility(checked ? View.VISIBLE : View.GONE);
            bottomCameraId.setText(camId);
            bottomBarrierId.setText(barId);
            updateBasicInfoSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    // ───── Signaling server ─────

    /**
     * 设置面板里信令服务器行的摘要：显示信令地址，未配置则显示"未配置"。
     */
    private void updateSignalingServerSummary() {
        updateSignalingStatusBar();
        String server = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_SIGNALING_SERVER, "");
        signalingServerSummary.setText(server.isEmpty() ? "未配置" : server);
    }

    /**
     * 信令服务器地址设置弹窗，保存后写回 SharedPreferences。
     */
    private void showSignalingServerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_signaling_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText serverInput = dialog.findViewById(R.id.dlgSignalingServerInput);
        serverInput.setText(prefs.getString(KEY_SIGNALING_SERVER, DEFAULT_SIGNALING_SERVER));
        dialog.findViewById(R.id.dlgSignalingSave).setOnClickListener(b -> {
            prefs.edit().putString(KEY_SIGNALING_SERVER, serverInput.getText().toString().trim()).apply();
            updateSignalingServerSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * 从右侧滑入设置面板 + 渐显遮罩，并启用返回键拦截（再按返回收起面板）。
     */
    private void openSettingsPanel() {
        if (isPanelOpen) return;
        isPanelOpen = true;
        backPressedCallback.setEnabled(true);

        // Make overlay and panel visible (panel starts off-screen via translationX in XML)
        settingsOverlay.setVisibility(View.VISIBLE);
        settingsPanel.setVisibility(View.VISIBLE);

        // Animate overlay fade-in
        settingsOverlay.setAlpha(0f);
        settingsOverlay.animate().alpha(1f).setDuration(250).start();

        // Animate panel slide-in from right
        settingsPanel.setTranslationX(settingsPanel.getWidth());
        settingsPanel.animate().translationX(0f).setDuration(300).start();
    }

    /**
     * 滑出设置面板 + 渐隐遮罩，并关闭返回键拦截（再按返回退出界面）。
     */
    private void closeSettingsPanel() {
        if (!isPanelOpen) return;
        isPanelOpen = false;
        backPressedCallback.setEnabled(false);

        // Animate overlay fade-out
        settingsOverlay.animate().alpha(0f).setDuration(200).withEndAction(() -> settingsOverlay.setVisibility(View.GONE)).start();

        // Animate panel slide-out to right
        settingsPanel.animate().translationX(settingsPanel.getWidth()).setDuration(250).withEndAction(() -> {
            settingsPanel.setVisibility(View.GONE);
            settingsPanel.setTranslationX(settingsPanel.getWidth());
        }).start();
    }

    /**
     * 每秒刷新时间显示（可见时），自递归 postDelayed 驱动。
     */
    private void startTimeUpdater() {
        final Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (timeVisible) {
                    timeDisplay.setText(timeFormat.format(new Date()));
                }
                timeHandler.postDelayed(this, 1000);
            }
        };
        tick.run();
    }

    /**
     * 界面可见时建立 MQTT 连接（仅前台保持连接）。
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mqtt != null) mqtt.connect();
    }

    /**
     * 界面不可见时主动断开 MQTT，避免后台占用连接。
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mqtt != null) mqtt.disconnect();
    }

    /**
     * 回到前台时开启相机预览。
     */
    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    /**
     * 切到后台时关闭相机，释放会话与 ImageReader。
     */
    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    /**
     * 销毁：移除 Surface 回调、关相机、断 MQTT、关 LPR 引擎与后台线程。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSurface.getHolder().removeCallback(surfaceCallback);
        closeCamera();
        if (mqtt != null) {
            mqtt.disconnect();
            mqtt = null;
        }
        if (lpr != null) {
            lpr.close();
            lpr = null;
        }
        if (lprThread != null) {
            lprThread.quitSafely();
            lprThread = null;
        }
    }

    /**
     * 相机权限授予结果：通过则开相机，否则提示。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                toast("Camera permission denied");
            }
        }
    }

    /**
     * 优先选后置摄像头，找不到则回退到第一个可用摄像头。
     */
    private String pickBackCameraId() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics cs = cameraManager.getCameraCharacteristics(id);
                Integer facing = cs.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
            String[] ids = cameraManager.getCameraIdList();
            return ids.length > 0 ? ids[0] : null;
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * Pick the supported preview size whose aspect ratio best matches the (landscape) screen.
     */
    private Size choosePreviewSize() {
        if (cameraId == null) {
            return null;
        }
        try {
            CameraCharacteristics cs = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return null;
            }
            Size[] raw = map.getOutputSizes(SurfaceHolder.class);
            if (raw == null || raw.length == 0) {
                raw = map.getOutputSizes(ImageFormat.PRIVATE);
            }
            if (raw == null || raw.length == 0) {
                return null;
            }

            WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            int sw = Math.max(bounds.width(), bounds.height());
            int sh = Math.min(bounds.width(), bounds.height());
            double targetRatio = (double) sw / sh;

            // Compare aspect ratios in landscape-normalized space, but keep the
            // ORIGINAL Size object: setFixedSize() must receive a size the camera
            // actually advertises as a supported output, or session config fails.
            Size best = null;
            double bestDiff = 0;
            long bestArea = 0;
            for (Size s : raw) {
                int ow = s.getWidth();
                int oh = s.getHeight();
                int cw = Math.max(ow, oh); // landscape-normalized for comparison
                int ch = Math.min(ow, oh);
                if (cw > MAX_PREVIEW_WIDTH || ch > MAX_PREVIEW_HEIGHT) {
                    continue;
                }
                double ratio = (double) cw / ch;
                double diff = Math.abs(ratio - targetRatio);
                long area = (long) cw * ch;
                if (best == null || diff < bestDiff - 1e-9 || (Math.abs(diff - bestDiff) < 1e-9 && area > bestArea)) {
                    bestDiff = diff;
                    bestArea = area;
                    best = s; // original, guaranteed-supported size
                }
            }
            return best != null ? best : raw[0];
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /**
     * Apply the chosen preview size to the surface (once). Letterboxes the view to match.
     */
    private void configureSurface(@NonNull SurfaceHolder holder) {
        if (surfaceConfigured) {
            return;
        }
        if (previewSize == null) {
            previewSize = choosePreviewSize();
        }
        if (previewSize != null) {
            cameraSurface.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            holder.setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        }
        surfaceConfigured = true;
    }

    /**
     * 打开相机：需 cameraId 已选、Surface 已就绪、未在打开中、权限已授予；
     * 任一不满足则直接返回或申请权限。打开结果由 {@link #cameraCallback} 回调。
     */
    private void openCamera() {
        if (cameraId == null) {
            toast("No camera available");
            return;
        }
        if (!surfaceReady || cameraDevice != null || opening) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        try {
            opening = true;
            cameraManager.openCamera(cameraId, cameraCallback, null);
        } catch (CameraAccessException | SecurityException e) {
            opening = false;
            toast("Cannot open camera: " + e.getMessage());
        }
    }

    /**
     * （重建）预览会话：把 Surface 与 LPR ImageReader 作为输出目标，设连续视频对焦，
     * 启动 repeating 请求。Surface 尺寸变化或相机切换后都会重新调用。
     */
    private void startPreview() {
        if (cameraDevice == null || !surfaceReady) {
            return;
        }
        Surface surface = cameraSurface.getHolder().getSurface();
        if (surface == null || !surface.isValid()) {
            return;
        }
        try {
            closePreviewSession();
            List<Surface> targets = new ArrayList<>();
            targets.add(surface);
            if (lprReader == null && previewSize != null) {
                lprReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
                lprReader.setOnImageAvailableListener(lprFrameListener, lprHandler);
            }
            if (lprReader != null) {
                targets.add(lprReader.getSurface());
            }
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface t : targets) {
                builder.addTarget(t);
            }
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            cameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        session.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        toast("Preview start failed");
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    toast("Camera preview setup failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            toast("Camera session error");
        }
    }

    /**
     * 停止重复请求并关闭当前会话，置空引用。
     */
    private void closePreviewSession() {
        if (captureSession != null) {
            try {
                captureSession.stopRepeating();
            } catch (CameraAccessException ignored) {
            }
            captureSession.close();
            captureSession = null;
        }
    }

    /**
     * 关闭预览会话、LPR ImageReader、相机设备，重置打开状态。
     */
    private void closeCamera() {
        closePreviewSession();
        opening = false;
        if (lprReader != null) {
            lprReader.close();
            lprReader = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // ───── License-plate recognition ─────

    /**
     * 启动 LPR 后台线程 + Handler，异步初始化 HyperLPR3 引擎（加载 onnx 模型）。
     */
    private void startLprEngine() {
        lprThread = new HandlerThread("lpr-recognizer");
        lprThread.start();
        lprHandler = new Handler(lprThread.getLooper());
        lprHandler.post(() -> {
            try {
                lpr = new LprRecognizer(MainActivity.this);
                Log.i("Lpr", "HyperLPR3 engine ready");
            } catch (Exception e) {
                Log.e("Lpr", "init failed", e);
                runOnUiThread(() -> toast("车牌识别引擎初始化失败"));
            }
        });
    }

    /**
     * 预览帧到达：节流到 {@link #LPR_MAX_FPS}，丢上一帧仍在推理时的新帧；
     * YUV→Bitmap→识别→展示→上报，全程在 LPR 后台线程。
     */
    private final ImageReader.OnImageAvailableListener lprFrameListener = reader -> {
        if (lprBusy || lpr == null) {
            return; // 上一帧还在推理，直接丢帧
        }
        long now = System.currentTimeMillis();
        if (now - lastLprAt < 1000 / LPR_MAX_FPS) {
            return;
        }
        lastLprAt = now;
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }
        lprBusy = true;
        try {
            Bitmap bmp = ImageUtils.yuvToBitmap(image);
            if (bmp == null) {
                return;
            }
            List<LprRecognizer.PlateResult> results = lpr.recognize(bmp);
            showLprResults(results);
            reportPlateIfNeeded(results, bmp);   // 在 bmp recycle 前完成图片裁剪与编码
            bmp.recycle();
        } catch (Exception e) {
            Log.e("Lpr", "recognize failed", e);
        } finally {
            image.close();
            lprBusy = false;
        }
    };

    /**
     * 在 UI 线程展示识别结果（取置信度最高者），并画框。
     */
    private void showLprResults(java.util.List<LprRecognizer.PlateResult> results) {
        runOnUiThread(() -> {
            if (results == null || results.isEmpty()) {
                plateResult.setText("未识别到车牌");
                plateOverlay.clear();
                return;
            }
            LprRecognizer.PlateResult best = null;
            for (LprRecognizer.PlateResult r : results) {
                if (best == null || r.confidence > best.confidence) {
                    best = r;
                }
            }
            if (best == null) {
                return;
            }
            String typeName = LprRecognizer.plateTypeName(best.plateType);
            plateResult.setText(String.format(Locale.getDefault(), "%s  %.2f  %s", best.plate, best.confidence, typeName));

            // 帧坐标 -> 屏幕坐标：SurfaceView 按预览比例居中，换算偏移+缩放。
            // 识别帧与 SurfaceView 显示的是同一路 buffer（同为 previewSize），方向一致，无需旋转。
            int vw = cameraSurface.getWidth();
            int vh = cameraSurface.getHeight();
            int fw = previewSize != null ? previewSize.getWidth() : vw;
            int fh = previewSize != null ? previewSize.getHeight() : vh;
            float sx = (float) vw / fw;
            float sy = (float) vh / fh;
            plateOverlay.setResults(results, cameraSurface.getLeft(), cameraSurface.getTop(), sx, sy);
        });
    }

    /**
     * 把识别到的最高置信度车牌上报（已在 LPR 后台线程调用）。
     * HTTP 回调与 MQTT 发布为两条独立通道，任一可用即上报；置信度不足 / 同一车牌
     * 冷却期内都直接跳过。去重以「至少一条通道成功」为准。
     *
     * <p>载荷附带两张 JPEG base64 图：原图（整帧）与车牌图（按检测框从原图裁出），
     * 在 {@code frame} 回收前同步编码完成。
     */
    private void reportPlateIfNeeded(java.util.List<LprRecognizer.PlateResult> results, Bitmap frame) {
        if (results == null || results.isEmpty()) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String callback = prefs.getString(KEY_LPR_CALLBACK, "");
        boolean hasHttp = !callback.trim().isEmpty();
        boolean hasMqtt = (mqtt != null && mqtt.isConnected());
        if (!hasHttp && !hasMqtt) {
            return; // 两条通道都不可用
        }

        LprRecognizer.PlateResult best = null;
        for (LprRecognizer.PlateResult r : results) {
            if (best == null || r.confidence > best.confidence) {
                best = r;
            }
        }
        if (best == null || best.confidence < LPR_REPORT_MIN_CONF) {
            return;
        }
        long now = System.currentTimeMillis();
        if (best.plate.equals(lastReportedPlate) && now - lastReportAt < LPR_REPORT_COOLDOWN_MS) {
            return; // 同一车牌冷却期内
        }
        final String plate = best.plate;
        final String cameraId = prefs.getString(KEY_CAMERA_ID, "CAM-001");
        final String payload;
        try {
            PlateRecognitionRequest request = new PlateRecognitionRequest(cameraId, plate, LprRecognizer.plateTypeName(best.plateType), ImageUtils.bitmapToJpegBase64(frame),        // 原图 JPEG base64
                    ImageUtils.cropPlateJpegBase64(frame, best.box)  // 车牌图 JPEG base64
            );
            payload = request.toJson();
        } catch (Exception e) {
            Log.e("Lpr", "build payload failed", e);
            return;
        }

        final String httpUrl = callback.trim();
        final String plateTopic = (mqtt != null) ? TOPIC_PLATE_PREFIX + mqtt.getClientId() + TOPIC_PLATE_SUFFIX : null;
        final boolean doHttp = hasHttp;
        final boolean doMqtt = hasMqtt;

        lprHandler.post(() -> {
            boolean httpOk = false;
            if (doHttp) {
                httpOk = postJson(httpUrl, payload);
            }
            boolean mqttOk = false;
            if (doMqtt) {
                // mqttOk = mqtt.publish(plateTopic, payload.getBytes(StandardCharsets.UTF_8), 1);
            }
            if (httpOk || mqttOk) {
                lastReportedPlate = plate;
                lastReportAt = System.currentTimeMillis();
                Log.i("Lpr", "reported plate " + plate + " http=" + httpOk + " mqtt=" + mqttOk);
            } else {
                Log.w("Lpr", "report plate failed: http=" + httpOk + " mqtt=" + mqttOk);
            }
        });
    }

    /**
     * 发送 JSON 到回调地址，返回是否成功。
     */
    private boolean postJson(String urlStr, String json) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(body.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            Log.w("Lpr", "postJson error", e);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 弹一个短 Toast。
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
