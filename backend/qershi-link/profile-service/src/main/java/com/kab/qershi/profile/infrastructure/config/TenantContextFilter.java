package com.kab.qershi.profile.infrastructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet Filter intercepting incoming HTTP API requests to extract the tenant schema identifier (X-Tenant-ID)
 * and populate the TenantContext ThreadLocal container.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
@Order(1)
public class TenantContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String tenantId = httpRequest.getHeader(TENANT_HEADER);

            if (tenantId != null && !tenantId.isBlank()) {
                String sanitizedTenantSchema = tenantId.trim().toLowerCase().replaceAll("[^a_z0-9_]", "");
                TenantContext.setTenantSchema(sanitizedTenantSchema);
                log.debug("Tenant context bound to schema: {}", sanitizedTenantSchema);
            } else {
                TenantContext.setTenantSchema(TenantContext.DEFAULT_TENANT);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
