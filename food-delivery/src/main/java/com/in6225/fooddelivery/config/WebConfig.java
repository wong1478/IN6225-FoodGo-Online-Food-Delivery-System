package com.in6225.fooddelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Pages that need a login. Browsing restaurants does not.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/cart", "/cart/**", "/orders", "/orders/**",
                        "/owner", "/owner/**", "/admin", "/admin/**");
    }
}
