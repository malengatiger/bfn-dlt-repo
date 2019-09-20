package com.bfn.webserver;

import com.bfn.flows.admin.ShareAccountInfoFlow;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(BFNWebApi.class);

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
    @Autowired
    private Environment env;
    private Dotenv getEnv() {
        // Load configuration from the environment or a $projectRoot/.env file, if present
        // See .env.sample for an example of what it is looking for
        return Dotenv.load();
    }
    @Bean
    public FirebaseApp firebaseBean() throws IOException {
        logger.info("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  BFNWebApi:  setting up Firebase ...." +
                " \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06");

        try {
            String envPath = env.getProperty("firebasePath");
//            String path = "/Users/aubs/WORK/CORDA/bfn-dlt-repo/bfn.json";
            logger.info("\uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06 .env PATH for Firebase Service Account: \uD83D\uDC99  ".concat(envPath));
            FileInputStream serviceAccount =
                    new FileInputStream(envPath);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://bfn-mobile-backend.firebaseio.com")
                    .build();

            FirebaseApp app =FirebaseApp.initializeApp(options);
            logger.info(" \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 " +
                    "Firebase Admin Setup OK:  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 name: "
            .concat(app.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Firebase Admin setup failed");
        }
        return null;
    }
}
