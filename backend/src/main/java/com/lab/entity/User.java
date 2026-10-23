package com.lab.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {

    private Integer id;
    private String username;

    /** 密码仅用于服务端校验，序列化时忽略 */
    @JsonIgnore
    private String password;

    /** student / teacher / admin */
    private String role;

    private String realName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
