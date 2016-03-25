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
public class PrioritiesConverter extends AbstractConverter{

    private static final Map<String, String> PRIORITIES = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String[] strings : localJiraConfig.getPriorities()) {
            PRIORITIES.put(strings[1], strings[0]);
        }
    }

    public void convertFromLocal() {
        //todo implement if needed
    }

    /**
     * Конвертит тип задачи из типа внешней жиры, в тип внутренней
     * на основе конфигурации приложения
     * Если тип не найден - по-умолчанию "Нормальный"
     */
    public String convertFromExternal(String extPriority) {
        String localPriority = PRIORITIES.get(extPriority);
        return StringUtils.isEmpty(localPriority) ? "Нормальный" : localPriority;
    }

}
