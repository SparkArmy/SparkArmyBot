package de.SparkArmy.springBoot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringApp {

    @Bean
    public ServletWebServerFactory factory(){
        return new TomcatServletWebServerFactory();
    }
}
