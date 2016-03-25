package ru.ssp.synch.impl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.cfg.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import ru.ssp.synch.Application;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by PakAI on 22.03.2016.
 */
@Configuration
@EnableTransactionManagement
@ConfigurationProperties(prefix = "hibernate")
@EnableJpaRepositories(basePackageClasses = Application.class)
public class HibernateConfig implements TransactionManagementConfigurer {

    private String driver;

    private String url;

    private String username;

    private String password;

    private String dialect;

    private String hbm2ddlAuto;

    private Boolean showSql;

    @Bean
    public DataSource configureDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(configureDataSource());
        entityManagerFactoryBean.setPackagesToScan("ru.ssp.synch.model");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
        jpaProperties.put(Environment.SHOW_SQL, showSql);
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new JpaTransactionManager();
    }

    /**
     * Sets new password.
     *
     * @param password New value of password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets new driver.
     *
     * @param driver New value of driver.
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Sets new dialect.
     *
     * @param dialect New value of dialect.
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    /**
     * Gets hbm2ddlAuto.
     *
     * @return Value of hbm2ddlAuto.
     */
    public String getHbm2ddlAuto() {
        return hbm2ddlAuto;
    }

    /**
     * Gets username.
     *
     * @return Value of username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets new username.
     *
     * @param username New value of username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets url.
     *
     * @return Value of url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets driver.
     *
     * @return Value of driver.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Sets new url.
     *
     * @param url New value of url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Sets new hbm2ddlAuto.
     *
     * @param hbm2ddlAuto New value of hbm2ddlAuto.
     */
    public void setHbm2ddlAuto(String hbm2ddlAuto) {
        this.hbm2ddlAuto = hbm2ddlAuto;
    }

    /**
     * Gets password.
     *
     * @return Value of password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets dialect.
     *
     * @return Value of dialect.
     */
    public String getDialect() {
        return dialect;
    }

    /**
     * Sets new showSql.
     *
     * @param showSql New value of showSql.
     */
    public void setShowSql(Boolean showSql) {
        this.showSql = showSql;
    }

    /**
     * Gets showSql.
     *
     * @return Value of showSql.
     */
    public Boolean getShowSql() {
        return showSql;
    }

}
