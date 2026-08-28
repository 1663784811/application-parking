package com.cyyaw.application.parking.gate.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.ClientManager;
import org.springframework.integration.mqtt.core.Mqttv3ClientManager;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

import java.util.List;
import java.util.UUID;

/**
 * MQTT 配置（参考 saas-mqtt）。
 * <p>架构：
 * <ul>
 *   <li>Mqttv3ClientManager：统一管理连接生命周期（自动重连）</li>
 *   <li>inbound：订阅主题、接收消息 → mqttInputChannel → handler</li>
 *   <li>outbound：mqttOutputChannel → 发布消息到 broker</li>
 * </ul>
 * <p>仅当 cyyaw.mqtt.enabled=true 时装配，避免无 broker 时启动失败。</p>
 * <p>注：Spring Boot 4 / Spring Integration 7 下，@ServiceActivator 标注在 @Bean 方法上
 * 不再自动把返回的 MessageHandler 订阅到通道（缺少 MessagingAnnotationPostProcessor 接线），
 * 故这里改为在创建通道后显式 subscribe，保证入站/出站通道都有订阅者，行为与 saas-mqtt 一致。</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "cyaw.mqtt", name = "enabled", havingValue = "true")
public class MqttConfig {

    /**
     * MQTT 客户端管理器，统一管理连接、会话、重连。
     */
    @Bean
    public Mqttv3ClientManager mqttv3ClientManager(MqttProperties mqttProperties, MqttStatusStore mqttStatusStore) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setUserName(mqttProperties.getUserName());
        // 心跳间隔 60 秒，保持连接活跃
        options.setKeepAliveInterval(60);
        // 清理会话（离线不保留订阅）
        options.setCleanSession(true);
        options.setServerURIs(mqttProperties.getTcp().toArray(new String[0]));
        // 自动重连
        options.setAutomaticReconnect(true);
        options.setExecutorServiceTimeout(mqttProperties.getTimeOut());
        // 连接超时（秒）：CONNECT 握手最长等待，超时即判定连接失败
        options.setConnectionTimeout(mqttProperties.getConnectTimeout());

        String clientId = mqttProperties.getClientId();
        if (clientId == null || clientId.isBlank()) {
            clientId = "parking-gate-" + UUID.randomUUID();
        }
        final String resolvedClientId = clientId;
        Mqttv3ClientManager manager = new Mqttv3ClientManager(options, resolvedClientId) {
            /**
             * Paho 在连接失败（含 CONNECT 握手超时、断线、自动重连失败）时回调此方法。
             * <p>注意：manager 开启了自动重连（{@code setAutomaticReconnect(true)}），
             * 此时 {@code Mqttv3ClientManager#start()} 的初始连接失败不会抛异常、也不会发布
             * MqttConnectionFailedEvent，而是直接走 Paho 的异步重连并回调到这里，
             * 所以重连失败/断线/超时都通过此回调统一打印日志。</p>
             */
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT 连接失败/断开（含连接超时）, clientId={}, serverURIs={}, cause={}", resolvedClientId, mqttProperties.getTcp(), cause == null ? "未知原因" : cause.toString());
                mqttStatusStore.markDisconnected(cause == null ? "连接断开" : cause.toString());
                super.connectionLost(cause);
            }


        };
        manager.addCallback(new ClientManager.ConnectCallback() {
            @Override
            public void connectComplete(boolean isReconnect) {
                log.info("MQTT 连接成功, isReconnect={}", isReconnect);
                mqttStatusStore.markConnected();
            }
        });
        log.info("MQTT 客户端管理器已创建，clientId={}, serverURIs={}, connectTimeout={}s", clientId, mqttProperties.getTcp(), mqttProperties.getConnectTimeout());
        return manager;
    }

    /**
     * 入站消息通道。
     */
    @Bean
    public DirectChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 出站消息通道。
     */
    @Bean
    public DirectChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    /**
     * 默认入站消息处理器：打印收到的消息，可按主题在此分发。
     * <p>入站消息的主题在 {@link MqttHeaders#RECEIVED_TOPIC}（mqtt_receivedTopic）头里，
     * 不是出站的 mqtt_topic。</p>
     */
    @Bean
    public MessageHandler handler() {
        return message -> log.info("收到 MQTT 消息, topic: {}, payload: {}", message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC), message.getPayload());
    }

    /**
     * 入站适配器：按 cyyaw.mqtt.topics 订阅并路由到 mqttInputChannel，显式订阅 handler。
     */
    @Bean
    public MessageProducer inbound(MqttProperties mqttProperties, Mqttv3ClientManager mqttClientManager, MessageHandler handler) {
        List<String> topics = mqttProperties.getTopics();
        String[] topicArray = topics.toArray(new String[0]);
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttClientManager, topicArray);
        adapter.setCompletionTimeout(mqttProperties.getTimeOut());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttProperties.getDefaultQos());
        // 显式订阅：入站通道 → handler（见类注释）
        SubscribableChannel inputChannel = mqttInputChannel();
        inputChannel.subscribe(handler);
        adapter.setOutputChannel(inputChannel);
        log.info("MQTT 入站适配器已创建，订阅主题: {}", topics);
        return adapter;
    }

    /**
     * 出站处理器：mqttOutputChannel → 发布到 broker，异步发送。
     */
    @Bean
    public MessageHandler outbound(MqttProperties mqttProperties, Mqttv3ClientManager mqttClientManager) {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(mqttClientManager);
        messageHandler.setAsync(true);
        messageHandler.setConverter(new DefaultPahoMessageConverter());
        // 显式订阅：出站通道 → messageHandler（见类注释）
        SubscribableChannel outputChannel = mqttOutputChannel();
        outputChannel.subscribe(messageHandler);
        log.info("MQTT 出站处理器已创建，clientId={}", mqttProperties.getClientId());
        return messageHandler;
    }
}
