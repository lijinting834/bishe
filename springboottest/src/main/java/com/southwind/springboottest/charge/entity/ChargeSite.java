package com.southwind.springboottest.charge.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "charge_site")
public class ChargeSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** e.g. "A区学生宿舍楼下" */
    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String description;

    private Date createdAt = new Date();
}
