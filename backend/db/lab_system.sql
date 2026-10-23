-- =============================================================
-- 实验室预约与设备管理系统 - 数据库初始化脚本（增量，不影响原有库）
-- 适用：MySQL 8.0+
-- =============================================================
CREATE DATABASE IF NOT EXISTS lab_system
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_system;

-- -------------------------------------------------------------
-- 用户表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（课程实验按明文存储，生产环境需加密）',
    `role`        ENUM('student','teacher','admin') DEFAULT 'student' COMMENT '角色',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名（新增字段，用于页面展示）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- -------------------------------------------------------------
-- 实验室表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `lab` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100) NOT NULL COMMENT '实验室名称',
    `capacity`    INT NOT NULL DEFAULT 30 COMMENT '可容纳人数',
    `status`      ENUM('available','maintenance') DEFAULT 'available' COMMENT '状态：空闲/维护中',
    `description` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实验室表';

-- -------------------------------------------------------------
-- 设备表（需求文档 ER 中有定义，此处补充建表语句）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `device` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `lab_id`      INT NOT NULL COMMENT '所属实验室',
    `name`        VARCHAR(100) NOT NULL COMMENT '设备名称',
    `model`       VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
    `status`      ENUM('available','occupied','maintenance') DEFAULT 'available' COMMENT '状态：空闲/占用/维修',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_device_lab` (`lab_id`),
    CONSTRAINT `fk_device_lab` FOREIGN KEY (`lab_id`) REFERENCES `lab`(`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '设备表';

-- -------------------------------------------------------------
-- 预约表（新增 people 字段，用于防超员容量校验）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `reservation` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     INT NOT NULL COMMENT '预约人',
    `lab_id`      INT NOT NULL COMMENT '预约实验室',
    `start_time`  DATETIME NOT NULL,
    `end_time`    DATETIME NOT NULL,
    `people`      INT NOT NULL DEFAULT 1 COMMENT '使用人数（用于防超员校验，新增字段）',
    `purpose`     VARCHAR(255) DEFAULT NULL COMMENT '预约用途（新增字段）',
    `status`      ENUM('pending','approved','rejected') DEFAULT 'pending',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_res_lab_time` (`lab_id`, `start_time`, `end_time`),
    KEY `idx_res_user` (`user_id`),
    CONSTRAINT `fk_res_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    CONSTRAINT `fk_res_lab` FOREIGN KEY (`lab_id`) REFERENCES `lab`(`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约表';

-- -------------------------------------------------------------
-- 操作日志表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `log` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     INT DEFAULT NULL COMMENT '操作人（登录失败等场景可为空）',
    `username`    VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名（新增字段，便于列表展示）',
    `action`      VARCHAR(100) DEFAULT NULL COMMENT '操作类型',
    `detail`      TEXT COMMENT '操作详情',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_log_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

-- -------------------------------------------------------------
-- 初始数据
-- -------------------------------------------------------------
INSERT INTO `user` (`username`, `password`, `role`, `real_name`)
SELECT 'admin', '123456', 'admin', '系统管理员'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

INSERT INTO `user` (`username`, `password`, `role`, `real_name`)
SELECT 'teacher01', '123456', 'teacher', '张老师'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher01');

INSERT INTO `user` (`username`, `password`, `role`, `real_name`)
SELECT 'student01', '123456', 'student', '李同学'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'student01');

INSERT INTO `lab` (`name`, `capacity`, `status`, `description`)
SELECT '计算机实验室A', 40, 'available', '配备 40 台高性能工作站'
WHERE NOT EXISTS (SELECT 1 FROM `lab` WHERE `name` = '计算机实验室A');

INSERT INTO `lab` (`name`, `capacity`, `status`, `description`)
SELECT '物理实验室B', 30, 'available', '基础物理实验设备齐全'
WHERE NOT EXISTS (SELECT 1 FROM `lab` WHERE `name` = '物理实验室B');

INSERT INTO `device` (`lab_id`, `name`, `model`, `status`)
SELECT l.id, '图形工作站', 'HP Z4 G5', 'available'
FROM `lab` l WHERE l.name = '计算机实验室A'
  AND NOT EXISTS (SELECT 1 FROM `device` d WHERE d.name = '图形工作站');

INSERT INTO `device` (`lab_id`, `name`, `model`, `status`)
SELECT l.id, '示波器', 'Tektronix TBS2000', 'available'
FROM `lab` l WHERE l.name = '物理实验室B'
  AND NOT EXISTS (SELECT 1 FROM `device` d WHERE d.name = '示波器');
