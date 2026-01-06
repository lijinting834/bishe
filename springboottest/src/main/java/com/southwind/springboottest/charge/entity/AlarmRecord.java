package com.southwind.springboottest.charge.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "alarm_record")
public class AlarmRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pileId;

    /** OVER_TEMP / OFFLINE / FAULT */
    @Column(nullable = false, length = 30)
    private String alarmType;

    @Column(length = 255)
    private String message;

    private Date alarmTime = new Date();

    /** 是否已处理 */
    private Boolean handled = false;
}
