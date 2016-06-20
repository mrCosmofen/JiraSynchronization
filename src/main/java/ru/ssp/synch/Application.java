package ru.ssp.synch;

import org.eclipse.jetty.jndi.ContextFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import ru.ssp.synch.impl.config.HibernateConfig;
import ru.ssp.synch.impl.config.LocalJiraConfiguration;

import java.util.List;

/**
 * Created by PakAI on 22.03.2016.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private LocalJiraConfiguration configuration;

    public static void main(String[] args) {
        System.setProperty("jsse.enableSNIExtension", "false");
        ConfigurableApplicationContext run = SpringApplication.run(new Class<?>[]{Application.class, HibernateConfig.class}, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<String[]> logins1 = configuration.getLogins();
    }

    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() throws Exception {
        JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        factory.setPort(9889);
        return factory;
    }
}
