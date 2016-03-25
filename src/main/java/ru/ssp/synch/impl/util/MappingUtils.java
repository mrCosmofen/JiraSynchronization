package ru.ssp.synch.impl.util;

import net.rcarz.jiraclient.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PakAI on 23.03.2016.
 */
public class MappingUtils {
    private final static Logger LOG = LoggerFactory.getLogger(MappingUtils.class);
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static Date getUpdatedDate(Issue issue) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        try {
            return formatter.parse((String) issue.getField("updated"));
        } catch (ParseException e) {
            LOG.error("Date parse Exception: ", e);
            return null;
        }
    }
}
