package com.southwind.springboottest.charge.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "charge_pile")
public class ChargePile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 桩编号，如 A01 */
    @Column(nullable = false, unique = true, length = 50)
    private String pileNo;

    /** 站点ID */
    private Long siteId;

    /** IDLE / CHARGING / FAULT / OFFLINE */
    @Column(nullable = false, length = 20)
    private String status = "IDLE";

    /** 额定功率(kW) */
    private BigDecimal powerKw;

    /** 枪口数量 */
    private Integer gunCount = 1;

    /** 最近心跳时间 */
    private Date lastHeartbeat;

    /** 当前温度(℃) */
    private BigDecimal temperature;

    private Date createdAt = new Date();

    private Date updatedAt = new Date();
}
