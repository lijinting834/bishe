package com.southwind.springboottest.charge.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** ADMIN / STUDENT / TEACHER */
    @Column(nullable = false, length = 20)
    private String role;

    private Date createdAt = new Date();
}
