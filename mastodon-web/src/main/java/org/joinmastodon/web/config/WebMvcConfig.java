package org.joinmastodon.web.config;

import org.joinmastodon.web.auth.AdminRoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminRoleInterceptor adminRoleInterceptor;

    @Autowired
    public WebMvcConfig(AdminRoleInterceptor adminRoleInterceptor) {
        this.adminRoleInterceptor = adminRoleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminRoleInterceptor)
                .addPathPatterns("/api/v1/admin/**");
    }
}
