package ru.ssp.synch.impl.util;

import net.rcarz.jiraclient.Version;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PakAI on 23.03.2016.
 */
@Component
public class VersionConverter extends AbstractConverter {

    private static final Map<String, String> FIX_VERSIONS = new HashMap<>();

    @PostConstruct
    private void init() {
        for (String[] strings : localJiraConfig.getFixVersions()) {
            FIX_VERSIONS.put(strings[1], strings[0]);
        }
    }

    public void convertFromLocal() {
        //todo implement if needed
    }

    /**
     * Конвертит версию из внешней жиры, в версию внутренней
     * на основе конфигурации приложения
     * Если версия не найдена - по-умолчанию null
     */
    public String convertFromExternal(List<Version> extVersion) {
        if (CollectionUtils.isEmpty(extVersion) || extVersion.size() > 1) {
            return null;
        }
        String localVersion = FIX_VERSIONS.get(extVersion.get(0).getName());
        return StringUtils.isEmpty(localVersion) ? null : localVersion;
    }
}
