package com.attendance_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true, length = 20)
    private String mssv;

    @Column(nullable = false, length = 100)
    private String name;


    @Column(name = "group_number", nullable = false)
    private Integer groupNumber;
}
