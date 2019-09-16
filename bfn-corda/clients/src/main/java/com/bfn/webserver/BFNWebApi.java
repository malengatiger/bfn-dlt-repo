package com.bfn.webserver;

import com.bfn.flows.admin.ShareAccountInfoFlow;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.boot.WebApplicationType.SERVLET;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
public class BFNWebApi {
    /**
     * Starts our Spring Boot application.
     */
    private static final Logger LOGGER = Logger.getLogger(BFNWebApi.class.getSimpleName());
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ShareAccountInfoFlow.class);

    public static void main(String[] args) {
        logger.info(" \uD83D\uDD06  \uD83D\uDD06 BFNWebApi starting   \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06️");
        SpringApplication app = new SpringApplication(BFNWebApi.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(SERVLET);
        app.run(args);

        logger.info(" \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  BFNWebApi:  started ....  \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                new Date().toString() + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A");
    }

    @Bean
    public CorsFilter corsFilter() {
        logger.info("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  BFNWebApi:  corsFilter started ??? .... \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        logger.info(" \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  corsFilter: config.getAllowCredentials: "
                + config.getAllowCredentials().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedMethods: "
                + config.getAllowedMethods().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedOrigins: "
                + config.getAllowedOrigins().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");
        logger.info(" \uD83D\uDC9A \uD83D\uDC99  \uD83D\uDC9A \uD83D\uDC99  corsFilter: config.getAllowedHeaders: "
                + config.getAllowedHeaders().toString() + " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");

        return new CorsFilter(source);
    }
}
