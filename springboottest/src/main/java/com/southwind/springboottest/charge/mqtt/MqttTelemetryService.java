package com.southwind.springboottest.charge.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.southwind.springboottest.charge.entity.AlarmRecord;
import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.AlarmRecordRepository;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * MQTT 遥测数据接入（可选）：订阅 topic，接收模拟数据，实时更新桩状态，并触发告警。
 *
 * 约定 payload（JSON 示例）：
 * {
 *   "pileNo": "A01",
 *   "status": "IDLE|CHARGING|FAULT|OFFLINE",
 *   "temperature": 38.5,
 *   "powerKw": 1.2
 * }
 */
@Component
@ConditionalOnProperty(prefix = "mqtt", name = "enabled", havingValue = "true")
public class MqttTelemetryService implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(MqttTelemetryService.class);

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Autowired
    private ChargePileRepository pileRepository;

    @Autowired
    private AlarmRecordRepository alarmRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            if (username != null && !username.trim().isEmpty()) {
                options.setUserName(username);
            }
            if (password != null && !password.trim().isEmpty()) {
                options.setPassword(password.toCharArray());
            }
            client.setCallback(this);
            client.connect(options);
            client.subscribe(topic);
            log.info("MQTT已连接: broker={}, topic={}", broker, topic);
        } catch (Exception e) {
            log.error("MQTT初始化失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT连接断开: {}", cause == null ? "" : cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        JsonNode node = mapper.readTree(payload);

        String pileNo = node.path("pileNo").asText(null);
        if (pileNo == null || pileNo.trim().isEmpty()) {
            return;
        }

        ChargePile pile = pileRepository.findByPileNo(pileNo).orElse(null);
        if (pile == null) {
            // 没有就自动创建一个桩（方便你演示）
            pile = new ChargePile();
            pile.setPileNo(pileNo);
        }

        String status = node.path("status").asText(null);
        if (status != null && !status.trim().isEmpty()) {
            pile.setStatus(status.trim().toUpperCase());
        }

        if (node.hasNonNull("temperature")) {
            pile.setTemperature(new BigDecimal(node.get("temperature").asText()));
        }
        if (node.hasNonNull("powerKw")) {
            pile.setPowerKw(new BigDecimal(node.get("powerKw").asText()));
        }

        pile.setLastHeartbeat(new Date());
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        // 简单异常告警：温度 >= 60 触发 OVER_TEMP；status=FAULT/OFFLINE 触发对应告警
        if (pile.getTemperature() != null && pile.getTemperature().compareTo(new BigDecimal("60")) >= 0) {
            createAlarmIfNeeded(pile.getId(), "OVER_TEMP", "温度过高: " + pile.getTemperature() + "℃");
        }
        if ("FAULT".equalsIgnoreCase(pile.getStatus())) {
            createAlarmIfNeeded(pile.getId(), "FAULT", "设备故障");
        }
        if ("OFFLINE".equalsIgnoreCase(pile.getStatus())) {
            createAlarmIfNeeded(pile.getId(), "OFFLINE", "设备离线");
        }
    }

    private void createAlarmIfNeeded(Long pileId, String type, String msg) {
        AlarmRecord ar = new AlarmRecord();
        ar.setPileId(pileId);
        ar.setAlarmType(type);
        ar.setMessage(msg);
        ar.setAlarmTime(new Date());
        ar.setHandled(false);
        alarmRepository.save(ar);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // subscriber only
    }
}
