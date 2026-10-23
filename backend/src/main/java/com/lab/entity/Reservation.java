package com.lab.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约实体
 */
@Data
public class Reservation {

    private Integer id;
    private Integer userId;
    private Integer labId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 使用人数，用于防超员校验 */
    private Integer people;

    private String purpose;

    /** pending / approved / rejected */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 预约人用户名（联表查询展示用） */
    private String username;

    /** 实验室名称（联表查询展示用） */
    private String labName;

    /** 实验室容量（联表查询展示用） */
    private Integer labCapacity;
}
