package ru.ssp.synch.impl.service;

import net.rcarz.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.ssp.synch.impl.util.ConnectionUtils;

import javax.annotation.PostConstruct;

/**
 * Created by PakAI on 23.03.2016.
 */
@Component
public abstract class AbstractJiraService {

    @PostConstruct
    private void initJiraClient() {
        jiraClient = getJiraClient();
    }

    @Autowired
    protected ConnectionUtils connectionUtils;

    protected JiraClient jiraClient;

    protected abstract JiraClient getJiraClient();

}
