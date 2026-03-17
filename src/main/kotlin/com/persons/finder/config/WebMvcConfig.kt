package com.persons.finder.config

import com.persons.finder.service.RateLimiterService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val rateLimiterService: RateLimiterService,
    @Value("\${app.rate-limit.trust-forwarded-headers:false}")
    private val trustForwardedHeaders: Boolean,
    @Value("\${app.rate-limit.trusted-proxies:}")
    trustedProxiesRaw: String
) : WebMvcConfigurer {

    private val trustedProxies = trustedProxiesRaw
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

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

                val routeTemplate = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
                    ?.toString()
                    ?: request.requestURI
                val routeKey = "${request.method}:$routeTemplate"
                val clientIp = resolveClientIp(request)

                rateLimiterService.checkLimit(routeKey, clientIp)
                return true
            }
        }).addPathPatterns("/persons/**")
    }

    private fun resolveClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr ?: "unknown"

        if (!trustForwardedHeaders) {
            return remoteAddr
        }
        if (trustedProxies.isEmpty() || remoteAddr !in trustedProxies) {
            return remoteAddr
        }

        val forwardedIp = request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        return forwardedIp ?: remoteAddr
    }
}
