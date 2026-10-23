package com.lab.common;

/**
 * 权限校验工具：统一抛出 403 业务异常
 */
public final class Guard {

    private Guard() {
    }

    /** 管理员或教师可操作 */
    public static void requireManager(String role) {
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            throw new BizException(403, "无权限执行该操作");
        }
    }

    /** 仅管理员可操作 */
    public static void requireAdmin(String role) {
        if (!"admin".equals(role)) {
            throw new BizException(403, "仅管理员可执行该操作");
        }
    }
}
