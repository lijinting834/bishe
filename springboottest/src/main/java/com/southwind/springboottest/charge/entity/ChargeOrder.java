package com.southwind.springboottest.charge.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "charge_order")
public class ChargeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(nullable = false)
    private Long pileId;

    @Column(nullable = false)
    private Long userId;

    /** BOOKED / CHARGING / FINISHED / ABNORMAL */
    @Column(nullable = false, length = 20)
    private String status = "CHARGING";

    private Date startTime = new Date();

    private Date endTime;

    /** 预约时间（可选） */
    private Date scheduleTime;

    /** 电量(kWh) */
    private BigDecimal energyKwh;

    /** 金额(元) */
    private BigDecimal amount;
}
