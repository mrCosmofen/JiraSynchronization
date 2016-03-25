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
public class UserConverter extends AbstractConverter {

    private static final Map<String, String> USER_LOGINS = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String[] strings : localJiraConfig.getLogins()) {
            USER_LOGINS.put(strings[1], strings[0]);
        }
    }

    public void convertFromLocal() {
        //todo implement if needed
    }

    /**
     * Конвертит пользователя из внешней жиры, в пользователя внутренней
     * на основе конфигурации приложения
     * Если пользователь не найден - по-умолчанию user з конфигурации
     */
    public String convertFromExternal(String extLogin) {
        String localLogin = USER_LOGINS.get(extLogin);
        return StringUtils.isEmpty(localLogin) ? localJiraConfig.getUser() : localLogin;
    }
}
