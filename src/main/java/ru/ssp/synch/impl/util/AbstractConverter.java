package ru.ssp.synch.impl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ssp.synch.impl.config.LocalJiraConfiguration;

/**
 * Created by PakAI on 23.03.2016.
 */
@Component
public abstract class AbstractConverter {

    @Autowired
    protected LocalJiraConfiguration localJiraConfig;
}
