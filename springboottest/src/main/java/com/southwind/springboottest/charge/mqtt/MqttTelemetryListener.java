package com.southwind.springboottest.charge.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.southwind.springboottest.charge.entity.AlarmRecord;
import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.AlarmRecordRepository;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class MqttTelemetryListener {
    private static final Logger log = LoggerFactory.getLogger(MqttTelemetryListener.class);

    @Value("${mqtt.enabled:false}")
    private boolean enabled;

    @Value("${mqtt.broker:tcp://127.0.0.1:1883}")
    private String broker;

    @Value("${mqtt.clientId:campus-charge-backend}")
    private String clientId;

    @Value("${mqtt.topic:campus/pile/+/telemetry}")
    private String topic;

    @Autowired
    private ChargePileRepository pileRepository;

    @Autowired
    private AlarmRecordRepository alarmRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("MQTT disabled (mqtt.enabled=false)");
            return;
        }
        try {
            MqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);
            client.subscribe(topic, this::onMessage);
            log.info("MQTT connected: {} subscribe: {}", broker, topic);
        } catch (Exception e) {
            log.error("MQTT init failed: {}", e.getMessage(), e);
        }
    }

    private void onMessage(String t, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            Telemetry telemetry = mapper.readValue(payload, Telemetry.class);
            if (telemetry.pileNo == null || telemetry.pileNo.trim().isEmpty()) return;

            ChargePile pile = pileRepository.findByPileNo(telemetry.pileNo).orElse(null);
            if (pile == null) {
                // 未登记的桩，忽略或自动创建（这里选择忽略，便于管理）
                log.warn("Telemetry for unknown pileNo={}, ignored", telemetry.pileNo);
                return;
            }

            if (telemetry.status != null && !telemetry.status.trim().isEmpty()) {
                pile.setStatus(telemetry.status.trim().toUpperCase());
            }
            if (telemetry.power != null) {
                pile.setPowerKw(telemetry.power);
            }
            if (telemetry.temperature != null) {
                pile.setTemperature(telemetry.temperature);
            }
            pile.setLastHeartbeat(new Date());
            pileRepository.save(pile);

            // 简单告警规则
            if (telemetry.temperature != null && telemetry.temperature.compareTo(new BigDecimal("70")) > 0) {
                createAlarm(pile.getId(), "OVER_TEMP", "温度过高: " + telemetry.temperature + "℃");
            }
            if ("FAULT".equalsIgnoreCase(telemetry.status)) {
                createAlarm(pile.getId(), "FAULT", "设备故障上报");
            }
        } catch (Exception e) {
            log.warn("MQTT message parse failed: {}", e.getMessage());
        }
    }

    private void createAlarm(Long pileId, String type, String msg) {
        AlarmRecord a = new AlarmRecord();
        a.setPileId(pileId);
        a.setAlarmType(type);
        a.setMessage(msg);
        a.setAlarmTime(new Date());
        alarmRepository.save(a);
    }

    @Data
    public static class Telemetry {
        public String pileNo;
        public BigDecimal temperature;
        public BigDecimal power;
        public String status;
        public Long ts;
    }
}
