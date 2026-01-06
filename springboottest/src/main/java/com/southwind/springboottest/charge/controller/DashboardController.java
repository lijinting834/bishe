package com.southwind.springboottest.charge.controller;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import com.southwind.springboottest.charge.entity.ChargePile;
import com.southwind.springboottest.charge.repository.AlarmRecordRepository;
import com.southwind.springboottest.charge.repository.ChargeOrderRepository;
import com.southwind.springboottest.charge.repository.ChargePileRepository;
import com.southwind.springboottest.charge.repository.ChargeSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private ChargePileRepository pileRepository;
    @Autowired private ChargeSiteRepository siteRepository;
    @Autowired private ChargeOrderRepository orderRepository;
    @Autowired private AlarmRecordRepository alarmRepository;

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<ChargePile> piles = pileRepository.findAll();
        List<ChargeOrder> orders = orderRepository.findAll();

        long online = piles.stream().filter(p -> p.getStatus() != null && !"OFFLINE".equalsIgnoreCase(p.getStatus())).count();
        long charging = piles.stream().filter(p -> "CHARGING".equalsIgnoreCase(p.getStatus())).count();
        long fault = piles.stream().filter(p -> "FAULT".equalsIgnoreCase(p.getStatus())).count();

        BigDecimal totalAmount = orders.stream()
                .map(ChargeOrder::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> statusCount = piles.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus() == null ? "UNKNOWN" : p.getStatus(), Collectors.counting()));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("siteCount", siteRepository.count());
        res.put("pileCount", pileRepository.count());
        res.put("onlinePileCount", online);
        res.put("chargingPileCount", charging);
        res.put("faultPileCount", fault);
        res.put("orderCount", orderRepository.count());
        res.put("unhandledAlarmCount", alarmRepository.findByHandled(false).size());
        res.put("totalAmount", totalAmount);
        res.put("pileStatusCount", statusCount);
        return res;
    }
}
