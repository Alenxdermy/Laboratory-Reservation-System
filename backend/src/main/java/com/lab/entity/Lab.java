package com.lab.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验室实体
 */
@Data
public class Lab {

    private Integer id;
    private String name;
    private Integer capacity;

    /** available / maintenance */
    private String status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
