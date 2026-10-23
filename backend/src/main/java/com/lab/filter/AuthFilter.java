package com.lab.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.common.Result;
import com.lab.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Token 鉴权过滤器：拦截 /api/**，校验 JWT 并将用户信息写入请求属性
 * 白名单：登录、注册、上传静态资源、错误页、OPTIONS 预检
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/error"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || WHITE_LIST.contains(path)
                || path.startsWith("/uploads/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        // 非接口路径（静态资源等）直接放行
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            writeUnauthorized(response, "未登录或 Token 缺失");
            return;
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();

        try {
            Claims claims = jwtUtil.parseToken(token);
            Integer uid = claims.get("uid", Integer.class);
            String role = claims.get("role", String.class);
            request.setAttribute("uid", uid);
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", role);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorized(response, "Token 无效或已过期，请重新登录");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), Result.fail(401, msg));
    }
}
