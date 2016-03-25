package ru.ssp.synch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
}
