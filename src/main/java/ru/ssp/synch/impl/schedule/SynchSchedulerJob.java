package ru.ssp.synch.impl.schedule;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssp.synch.impl.service.SynchronizationService;

import java.util.List;

/**
 * Created by PakAI on 22.03.2016.
 */
public class SynchSchedulerJob implements Job {

    private final static Logger LOG = LoggerFactory.getLogger(SynchSchedulerJob.class);

    private SynchronizationService synchronizationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDetail existingJobDetail = context.getJobDetail();
            if (existingJobDetail != null) {
                String jobName = existingJobDetail.getKey().getName();
                LOG.info("Job {} has started.", jobName);
                checkExistingJobs(context, existingJobDetail);
                JobDataMap jobDataMap = context.getMergedJobDataMap();
                synchronizationService = (SynchronizationService) jobDataMap.get(SynchScheduler.SYNC_SERVICE_KEY);
                synchronizationService.doSynch();
            }
        } catch (Exception e) {
            LOG.error("Ошибка синхронизации: ", e);
        }
    }

    private void checkExistingJobs(JobExecutionContext context, JobDetail existingJobDetail) throws SchedulerException {
        Scheduler scheduler = context.getScheduler();
        String jobName = existingJobDetail.getKey().getName();
        List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
        if(currentlyExecutingJobs.size() > 1) {
            for (JobExecutionContext jec : currentlyExecutingJobs) {
                if (existingJobDetail.equals(jec.getJobDetail()) && !jec.getFireInstanceId().equals(context.getFireInstanceId())) {
                    String message = jobName + " is already running.";
                    LOG.info(message);
                    throw new JobExecutionException(message, false);
                }
            }
        }
    }
}
