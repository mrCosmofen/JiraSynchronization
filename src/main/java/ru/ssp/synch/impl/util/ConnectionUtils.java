package ru.ssp.synch.impl.util;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ssp.synch.impl.config.ExternalJiraConfiguration;
import ru.ssp.synch.impl.config.JiraConfiguration;
import ru.ssp.synch.impl.config.LocalJiraConfiguration;

/**
 * Created by PakAI on 23.03.2016.
 */
@Component
public class ConnectionUtils {

    @Autowired
    private ExternalJiraConfiguration extJiraConfig;

    @Autowired
    private LocalJiraConfiguration localJiraConfig;

    public JiraClient getExtJiraClient() {
        return getJiraClient(extJiraConfig);
    }

    public JiraClient getLocalJiraClient() {
        return getJiraClient(localJiraConfig);
    }

    public JiraClient getJiraClient(JiraConfiguration config) {
        final String extJiraUrl = config.getUrl();
        final String jiraUser = config.getUser();
        final String jiraPassword = config.getPassword();
        BasicCredentials creds = new BasicCredentials(jiraUser, jiraPassword);
        return new JiraClient(extJiraUrl, creds);
    }
}
