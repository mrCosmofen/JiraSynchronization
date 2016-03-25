package ru.ssp.synch.impl.schedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssp.synch.impl.config.SchedulerConfiguration;
import ru.ssp.synch.impl.service.SynchronizationService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by PakAI on 22.03.2016.
 */
@Service
public class SynchScheduler {

    private final static Logger LOG = LoggerFactory.getLogger(SynchScheduler.class);

    public static final String SYNC_SERVICE_KEY = "SYNC_SERVICE";
    private static final String DEFAULT_SCHEDULER_INSTANCE_NAME = "DefaultQuartzScheduler";
    private static final String GROUP = "SYNCH";
    private static final String DEFAULT_JOB_NAME = "Синхронизация двух инстансов Jira";


    @Autowired
    private SchedulerConfiguration schedulerConfiguration;

    @Autowired
    private SynchronizationService synchronizationService;

    @PostConstruct
    public void start() {
        try {

            synchronizationService.synchDB();

            Trigger trigger = createTrigger();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(SYNC_SERVICE_KEY, synchronizationService);
            JobDetail jobDetail = JobBuilder.newJob(SynchSchedulerJob.class)
                    .withIdentity(DEFAULT_JOB_NAME, GROUP)
                    .setJobData(jobDataMap)
                    .build();

            org.quartz.Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.scheduleJob(jobDetail, trigger);

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (Exception e) {
            LOG.error("Ошибка старта таймера: ", e);
        }

    }

    public void reload() {
        try {
            LOG.info("Метод reload() запущен.");
            org.quartz.Scheduler scheduler = new StdSchedulerFactory().getScheduler(DEFAULT_SCHEDULER_INSTANCE_NAME);
            if (scheduler != null)
                scheduler.clear();
        } catch (SchedulerException e) {
            LOG.error("Ошибка oчистки таймера: ", e);
        }

        start();
    }

    @PreDestroy
    public void destroy() {
        try {
            org.quartz.Scheduler scheduler = new StdSchedulerFactory().getScheduler(DEFAULT_SCHEDULER_INSTANCE_NAME);
            if (scheduler != null)
                scheduler.shutdown();
        } catch (SchedulerException e) {
            LOG.error("Ошибка отключения таймера: ", e);
        }
    }

    private Trigger createTrigger() {
        return TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(schedulerConfiguration.getCronExpression()))
                .build();
    }

}
