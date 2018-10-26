package com.bootapp;

import com.bootapp.config.SpringBootStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.net.InetAddress;

/**
 * @Author: mrcheyg
 * @Date: Created in 2017/12/16 15:54
 */
@SpringBootApplication(scanBasePackages = {"com.bootapp"})
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        Environment environment = new SpringApplicationBuilder(Application.class)
                .banner(new Banner() {
                    @Override
                    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
                        String banner = "####################################\n" +
                                        "#             BOOTAPP              #\n" +
                                        "####################################\n";
                        out.append(banner);
                        out.flush();
                    }
                })
                .listeners(new SpringBootStartupListener())
                .run(args).getEnvironment();

        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n----------------------------------------------------------",
                environment.getProperty("spring.application.name"),
                environment.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                environment.getProperty("server.port"));
    }

}
