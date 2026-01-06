package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import com.southwind.springboottest.charge.repository.ChargeOrderRepository;
import com.southwind.springboottest.charge.service.OrderService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
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

    @PostMapping("/book")
    public ChargeOrder book(@RequestBody BookReq req) {
        return orderService.book(req.pileId, req.userId, req.scheduleTime);
    }

    @PostMapping("/stop")
    public ChargeOrder stop(@RequestBody StopReq req) {
        if (req.getOrderId() != null) {
            return orderService.stopCharge(req.getOrderId(), req.getEnergyKwh());
        }
        if (req.getPileId() != null) {
            return orderService.stopChargeByPileId(req.getPileId(), req.getEnergyKwh());
        }
        throw new IllegalArgumentException("orderId 或 pileId 必须提供");
    }

    @Data
    public static class StartReq {
        private Long pileId;
        private Long userId;
    }

    @Data
    public static class BookReq {
        private Long pileId;
        private Long userId;
        private Date scheduleTime;
    }

    @Data
    public static class StopReq {
        private Long orderId;
        private BigDecimal energyKwh;
        private Long pileId;
    }
}
