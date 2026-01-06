package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import com.southwind.springboottest.charge.repository.ChargeOrderRepository;
import com.southwind.springboottest.charge.service.OrderService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class ChargeOrderController {

    private final ChargeOrderRepository orderRepository;
    private final OrderService orderService;

    public ChargeOrderController(ChargeOrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public List<ChargeOrder> list() {
        return orderRepository.findAll();
    }

    // ✅ 前端要的：GET /api/order/findAll
    @GetMapping("/findAll")
    public List<ChargeOrder> findAll() {
        return orderRepository.findAll();
    }

    @GetMapping("/listByUser/{userId}")
    public List<ChargeOrder> listByUser(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    @PostMapping("/start")
    public ChargeOrder start(@RequestBody StartReq req) {
        return orderService.startCharge(req.pileId, req.userId);
    }

    @PostMapping("/stop")
    public ChargeOrder stop(@RequestBody StopReq req) {
        if (req.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        return orderService.stopCharge(req.getOrderId(), req.getEnergyKwh());
    }

    @Data
    public static class StartReq {
        private Long pileId;
        private Long userId;
    }

    @Data
    public static class StopReq {
        private Long orderId;
        private BigDecimal energyKwh;
    }
}
