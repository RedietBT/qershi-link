package com.kab.qershi.hub;

import org.springdoc.core.customizers.SwaggerUiConfigProperties;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerResourceResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Swagger UI Customizer injecting dark theme CSS and floating Bulb Light/Dark Mode toggle JS script into Centralized API Hub.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class SwaggerUiConfig {

    @Bean
    public SwaggerIndexTransformer indexPageTransformer(SwaggerUiConfigProperties swaggerUiConfig,
                                                        SwaggerResourceResolver swaggerResourceResolver) {
        return new SwaggerIndexPageTransformer(swaggerUiConfig, swaggerResourceResolver) {
            @Override
            public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) {
                try {
                    Resource transformed = super.transform(request, resource, transformerChain);
                    String html;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(transformed.getInputStream(), StandardCharsets.UTF_8))) {
                        html = reader.lines().collect(Collectors.joining("\n"));
                    }

                    String customTags = """
                        <link rel="stylesheet" type="text/css" href="/swagger-ui/swagger-dark-theme.css" />
                        <script src="/swagger-ui/swagger-theme-toggle.js" defer></script>
                        </head>
                        """;

                    html = html.replace("</head>", customTags);
                    return new TransformedResource(transformed, html.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    return resource;
                }
            }
        };
    }

    private static class TransformedResource extends ByteArrayResource {
        private final Resource original;

        public TransformedResource(Resource original, byte[] byteArray) {
            super(byteArray);
            this.original = original;
        }

        @Override
        public String getFilename() {
            return original.getFilename();
        }
    }
}
