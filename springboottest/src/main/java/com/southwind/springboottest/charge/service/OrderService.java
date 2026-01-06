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

    /** 简化版：开始充电 -> 创建订单，并把桩状态改为 CHARGING */
    public ChargeOrder startCharge(Long pileId, Long userId) {
        ChargePile pile = pileRepository.findById(pileId)
                .orElseThrow(() -> new IllegalArgumentException("pileId 不存在"));

        pile.setStatus("CHARGING");
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        ChargeOrder order = new ChargeOrder();
        order.setOrderNo("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setPileId(pileId);
        order.setUserId(userId);
        order.setStatus("STARTED");
        order.setStartTime(new Date());
        return orderRepository.save(order);
    }

    /** 简化版：结束充电 -> 补齐电量/费用，并把桩状态改为 IDLE */
    public ChargeOrder stopCharge(Long orderId, BigDecimal energyKwh) {
        ChargeOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("orderId 不存在"));

        if (!"STARTED".equals(order.getStatus())) {
            return order;
        }

        order.setEndTime(new Date());
        order.setEnergyKwh(energyKwh);

        // 简化计费：1.5元/度
        BigDecimal pricePerKwh = new BigDecimal("1.50");
        BigDecimal amount = Optional.ofNullable(energyKwh).orElse(BigDecimal.ZERO)
                .multiply(pricePerKwh)
                .setScale(2, RoundingMode.HALF_UP);
        order.setAmount(amount);
        order.setStatus("FINISHED");
        ChargeOrder saved = orderRepository.save(order);

        pileRepository.findById(order.getPileId()).ifPresent(pile -> {
            pile.setStatus("IDLE");
            pile.setUpdatedAt(new Date());
            pileRepository.save(pile);
        });

        return saved;
    }
}
