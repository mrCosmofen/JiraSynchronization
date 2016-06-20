package ru.ssp.synch.impl.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PakAI on 28.03.2016.
 */
@Component
public class ComponentsConverter extends AbstractConverter {

    private static final Map<String, String> COMPONENTS = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String[] strings : localJiraConfig.getComponents()) {
            COMPONENTS.put(strings[1], strings[0]);
        }
    }

    @Override
    public void convertFromLocal() {
        //todo implement if needed
    }

    /**
     * Конвертит тип задачи из типа внешней жиры, в тип внутренней
     * на основе конфигурации приложения
     * Если тип не найден - по-умолчанию null
     */
    @Override
    public String convertFromExternal(String extComponent) {
        String localComponent = COMPONENTS.get(extComponent);
        return StringUtils.isEmpty(localComponent) ? null: localComponent;
    }

}
