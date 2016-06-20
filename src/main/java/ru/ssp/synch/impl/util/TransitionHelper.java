package ru.ssp.synch.impl.util;

import net.rcarz.jiraclient.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ssp.synch.impl.config.LocalJiraConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by PakAI on 24.03.2016.
 */
@Component
public class TransitionHelper {

    private final static Logger LOG = LoggerFactory.getLogger(TransitionHelper.class);

    @Autowired
    private LocalJiraConfiguration localJiraConfig;

    @Autowired
    private ConnectionUtils connectionUtils;

    public void flowToReadyState(String jiraKey, boolean local) throws JiraException {
        JiraClient jiraClient = local ? connectionUtils.getLocalJiraClient() : connectionUtils.getExtJiraClient();
        Issue issue = jiraClient.getIssue(jiraKey);
        String statusName = issue.getStatus().getName();

        //todo к сожалению хардкод
        if (!statusName.equals("Закрыт") && !statusName.equals("Сборка выложена")) {
            return;
        }
        RestClient restClient = jiraClient.getRestClient();
        Object[] availableTransitions = getAvailableTransitions(jiraKey, restClient);
        String readyTransition = getReadyTransition(availableTransitions);
        if (StringUtils.isNotEmpty(readyTransition)) {
            issue.transition()
                    .execute(readyTransition);
            issue.transition().execute("Готова к работе");
        }

    }

    private Object[] getAvailableTransitions(String jiraKey, RestClient restClient) throws JiraException {
        try {
            URI transuri = restClient.buildURI(
                    getRestUri(jiraKey) + "/transitions",
                    new HashMap<String, String>() {{
                        put("expand", "transitions.fields");
                    }});
            JSONObject jo = (JSONObject) restClient.get(transuri);
            return (Object[]) JSONArray.toArray((JSONArray) jo.get("transitions"));
        } catch (RestException | IOException | URISyntaxException e) {
            String msg = "Unable to transit issue: ";
            LOG.error(msg, e);
            throw new JiraException(msg, e);
        }
    }

    private String getRestUri(String jiraKey) {
        return String.format("/rest/api/latest/issue/%s", jiraKey);
    }

    public String getReadyTransition(Object[] availableTransitions) {
        List<Object> transitions = Arrays.asList(availableTransitions);
        Optional<Object> transition = transitions.stream()
                .filter(o -> ((DynaBean) ((DynaBean) o).get("to")).get("name").equals("Открыт"))
                .findFirst();
        if (transition.isPresent()) {
            DynaBean bean = (DynaBean) transition.get();
            return (String) bean.get("name");
        }
        return null;
    }
}
