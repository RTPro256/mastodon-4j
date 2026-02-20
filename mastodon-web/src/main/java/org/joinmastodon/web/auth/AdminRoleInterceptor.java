package org.joinmastodon.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joinmastodon.core.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);
        if (adminOnly == null) {
            adminOnly = handlerMethod.getBeanType().getAnnotation(AdminOnly.class);
        }
        
        if (adminOnly == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedPrincipal authPrincipal)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return false;
        }

        User.Role role = authPrincipal.role();
        if (role == null) {
            role = User.Role.USER;
        }

        boolean allowModerator = adminOnly.moderator();
        
        if (role == User.Role.ADMIN) {
            return true;
        }
        
        if (allowModerator && role == User.Role.MODERATOR) {
            return true;
        }

        response.sendError(HttpStatus.FORBIDDEN.value(), "Insufficient permissions");
        return false;
    }
}
