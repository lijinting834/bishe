package com.southwind.springboottest.charge.service;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.ChargeOrderRepository;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final ChargeOrderRepository orderRepository;
    private final ChargePileRepository pileRepository;

    public OrderService(ChargeOrderRepository orderRepository, ChargePileRepository pileRepository) {
        this.orderRepository = orderRepository;
        this.pileRepository = pileRepository;
    }

    /** 预约充电：仅登记订单，不占用桩功率 */
    public ChargeOrder book(Long pileId, Long userId, Date scheduleTime) {
        ChargePile pile = pileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("pileId 不存在"));

        if ("CHARGING".equalsIgnoreCase(pile.getStatus())) {
            throw new IllegalStateException("该充电桩正在充电中");
        }

        ChargeOrder order = new ChargeOrder();
        order.setOrderNo("BOOK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        order.setPileId(pileId);
        order.setUserId(userId);
        order.setStatus("BOOKED");
        order.setScheduleTime(scheduleTime);
        order.setStartTime(scheduleTime == null ? new Date() : scheduleTime);
        return orderRepository.save(order);
    }

    /** 简化版：开始充电 -> 创建订单，并把桩状态改为 CHARGING */
    public ChargeOrder startCharge(Long pileId, Long userId) {
        ChargePile pile = pileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("pileId 不存在"));

        if ("CHARGING".equalsIgnoreCase(pile.getStatus())) {
            throw new IllegalStateException("该充电桩正在充电中");
        }

        pile.setStatus("CHARGING");
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        ChargeOrder order = new ChargeOrder();
        order.setOrderNo("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setPileId(pileId);
        order.setUserId(userId);
        order.setStatus("CHARGING");
        order.setStartTime(new Date());
        return orderRepository.save(order);
    }

    /** 简化版：结束充电 -> 补齐电量/费用，并把桩状态改为 IDLE */
    public ChargeOrder stopCharge(Long orderId, BigDecimal energyKwh) {
        ChargeOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("orderId 不存在"));

        return stopCharge(order, energyKwh);
    }

    /** 兼容前端：通过 pileId 结束最近一条 CHARGING 订单 */
    public ChargeOrder stopChargeByPileId(Long pileId, BigDecimal energyKwh) {
        ChargeOrder order = orderRepository.findFirstByPileIdAndStatusOrderByStartTimeDesc(pileId, "CHARGING")
    /** 兼容前端：通过 pileId 结束最近一条 STARTED 订单 */
    public ChargeOrder stopChargeByPileId(Long pileId, BigDecimal energyKwh) {
        ChargeOrder order = orderRepository.findFirstByPileIdAndStatusOrderByStartTimeDesc(pileId, "STARTED")
                .orElseThrow(() -> new IllegalArgumentException("未找到该桩正在充电的订单"));

        return stopCharge(order, energyKwh);
    }

    private ChargeOrder stopCharge(ChargeOrder order, BigDecimal energyKwh) {
        if (!"CHARGING".equalsIgnoreCase(order.getStatus())) {
        
        if (!"STARTED".equals(order.getStatus())) {
            return order;
        }

        Date endTime = new Date();
        order.setEndTime(endTime);

        ChargePile pile = pileRepository.findById(order.getPileId())
                .orElseThrow(() -> new IllegalStateException("充电桩不存在"));

        BigDecimal energy = energyKwh;
        if (energy == null) {
            long startMillis = Optional.ofNullable(order.getStartTime()).map(Date::getTime).orElse(endTime.getTime());
            long minutes = Math.max(1, (endTime.getTime() - startMillis) / 60_000);
            BigDecimal power = Optional.ofNullable(pile.getPowerKw()).orElse(new BigDecimal("1.0"));
            energy = power.multiply(new BigDecimal(minutes)).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
        }
        order.setEnergyKwh(energy);

        BigDecimal pricePerKwh = new BigDecimal("1.20");
        BigDecimal amount = energy.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);
        order.setAmount(amount);
        order.setStatus("FINISHED");

        ChargeOrder saved = orderRepository.save(order);

        pile.setStatus("IDLE");
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        return saved;
    }
}
