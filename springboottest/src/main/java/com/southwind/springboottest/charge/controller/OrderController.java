package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.ChargeOrderRepository;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//@RestController
//@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private ChargeOrderRepository orderRepository;
    @Autowired
    private ChargePileRepository pileRepository;

    @GetMapping("/findAll")
    public List<ChargeOrder> findAll() {
        return orderRepository.findAll();
    }

    @GetMapping("/findByUserId/{userId}")
    public List<ChargeOrder> findByUserId(@PathVariable Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Data
    public static class StartReq {
        private Long pileId;
        private Long userId;
    }

    /** 简化版：开始充电 -> 生成 CHARGING 订单，桩状态置为 CHARGING */
    @PostMapping("/start")
    public ChargeOrder start(@RequestBody StartReq req) {
        if (req.getPileId() == null || req.getUserId() == null) {
            throw new IllegalArgumentException("pileId/userId不能为空");
        }

        ChargePile pile = pileRepository.findById(req.getPileId()).orElseThrow(() -> new IllegalArgumentException("充电桩不存在"));
        if ("CHARGING".equalsIgnoreCase(pile.getStatus())) {
            throw new IllegalStateException("该充电桩正在充电中");
        }

        ChargeOrder order = new ChargeOrder();
        order.setOrderNo(genOrderNo());
        order.setPileId(pile.getId());
        order.setUserId(req.getUserId());
        order.setStatus("CHARGING");
        order.setStartTime(new Date());

        pile.setStatus("CHARGING");
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        return orderRepository.save(order);
    }

    @Data
    public static class StopReq {
        private Long pileId;
        private BigDecimal energyKwh; // 可选：前端传模拟电量
        private BigDecimal amount;    // 可选：前端传模拟费用
    }

    /** 简化版：结束充电 -> 将最近一笔 CHARGING 订单置为 FINISHED，并回写电量/费用 */
    @PostMapping("/stop")
    public ChargeOrder stop(@RequestBody StopReq req) {
        if (req.getPileId() == null) {
            throw new IllegalArgumentException("pileId不能为空");
        }

        ChargePile pile = pileRepository.findById(req.getPileId()).orElseThrow(() -> new IllegalArgumentException("充电桩不存在"));
        ChargeOrder order = orderRepository.findFirstByPileIdAndStatusOrderByStartTimeDesc(pile.getId(), "CHARGING")
                .orElseThrow(() -> new IllegalStateException("未找到该桩正在进行中的订单"));

        order.setStatus("FINISHED");
        order.setEndTime(new Date());

        // 如果前端不传，就按“时长*功率*单价”给个合理模拟
        BigDecimal energy = req.getEnergyKwh();
        if (energy == null) {
            long minutes = Math.max(1, (order.getEndTime().getTime() - order.getStartTime().getTime()) / (60_000));
            BigDecimal power = pile.getPowerKw() == null ? new BigDecimal("1.0") : pile.getPowerKw();
            energy = power.multiply(new BigDecimal(minutes)).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
        }
        order.setEnergyKwh(energy);

        BigDecimal amount = req.getAmount();
        if (amount == null) {
            BigDecimal pricePerKwh = new BigDecimal("1.20");
            amount = energy.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);
        }
        order.setAmount(amount);

        pile.setStatus("IDLE");
        pile.setUpdatedAt(new Date());
        pileRepository.save(pile);

        return orderRepository.save(order);
    }

    private String genOrderNo() {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        return "OD" + ts + (int)(Math.random() * 900 + 100);
    }
}
