package ru.ssp.synch.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by PakAI on 22.03.2016.
 */
@Component
@ConfigurationProperties(prefix = "synch.external.jira")
public class ExternalJiraConfiguration extends JiraConfiguration {

    private String jql;

    /**
     * Gets jql.
     *
     * @return Value of jql.
     */
    public String getJql() {
        return jql;
    }

    /**
     * Sets new jql.
     *
     * @param jql New value of jql.
     */
    public void setJql(String jql) {
        this.jql = jql;
    }
}
