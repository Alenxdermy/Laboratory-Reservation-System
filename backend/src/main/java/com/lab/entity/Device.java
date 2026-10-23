package com.lab.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
public class Device {

    private Integer id;
    private Integer labId;
    private String name;
    private String model;

    /** available / occupied / maintenance */
    private String status;

    /** 所属实验室名称（联表查询展示用，非数据库字段） */
    private String labName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
