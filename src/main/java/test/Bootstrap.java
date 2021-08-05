package test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Console bootstrap.
 */

@SpringBootApplication
public class Bootstrap {

    /**
     * Startup RESTful server.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Bootstrap.class, args);

    }

}
