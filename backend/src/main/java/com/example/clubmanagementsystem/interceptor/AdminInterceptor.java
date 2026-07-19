package com.example.clubmanagementsystem.interceptor;

import com.example.clubmanagementsystem.common.Result;
import com.example.clubmanagementsystem.entity.SysUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(401, "未登录")
            ));
            return false;
        }

        SysUser currentUser = (SysUser) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != 1) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(403, "权限不足，需要管理员身份")
            ));
            return false;
        }
        return true;
    }
}
