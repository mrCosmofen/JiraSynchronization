package ru.ssp.synch.impl.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PakAI on 23.03.2016.
 */
@Component
public class IssueTypeConverter extends AbstractConverter {

    private static final Map<String, String> ISSUE_TYPES = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String[] strings : localJiraConfig.getIssueTypes()) {
            ISSUE_TYPES.put(strings[1], strings[0]);
        }
    }

    @Override
    public void convertFromLocal() {
        //todo implement if needed
    }

    /**
     * Конвертит тип задачи из типа внешней жиры, в тип внутренней
     * на основе конфигурации приложения
     * Если тип не найден - по-умолчанию Story
     */
    @Override
    public String convertFromExternal(String extType) {
        String localType = ISSUE_TYPES.get(extType);
        return StringUtils.isEmpty(localType) ? "Story" : localType;
    }
}
