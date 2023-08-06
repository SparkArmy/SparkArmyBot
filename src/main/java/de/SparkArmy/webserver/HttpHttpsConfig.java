package de.SparkArmy.webserver;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpHttpsConfig {

    @Value(value = "${server.ssl.key-store:}")
    private String sslKeyStore;

    // Defined in application.properties file
    // (User-defined Property)
    @Value(value = "${server.http.port:80}")
    private int httpPort;

    // Defined in application.properties file
    @Value("${server.port:80}")
    int httpsPort;

    @Bean
    public ServletWebServerFactory servletContainer() {
        boolean needRedirectToHttps = sslKeyStore != null && !sslKeyStore.isEmpty();

        TomcatServletWebServerFactory tomcat;

        if (!needRedirectToHttps) {
            tomcat = new TomcatServletWebServerFactory();
            return tomcat;
        }

        tomcat = new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    private @NotNull Connector redirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
