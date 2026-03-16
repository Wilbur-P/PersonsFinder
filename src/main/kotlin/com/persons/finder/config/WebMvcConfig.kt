package com.persons.finder.config

import com.persons.finder.service.RateLimiterService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val rateLimiterService: RateLimiterService
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : HandlerInterceptor {
            override fun preHandle(
                request: HttpServletRequest,
                response: HttpServletResponse,
                handler: Any
            ): Boolean {
                if (handler !is HandlerMethod) {
                    return true
                }

                val forwardedFor = request.getHeader("X-Forwarded-For")
                val clientIp = forwardedFor
                    ?.split(",")
                    ?.firstOrNull()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: request.remoteAddr

                rateLimiterService.checkLimit(request.requestURI, clientIp)
                return true
            }
        }).addPathPatterns("/persons/**")
    }
}
