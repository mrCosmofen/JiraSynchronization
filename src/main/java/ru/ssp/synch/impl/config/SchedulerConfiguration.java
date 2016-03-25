package ru.ssp.synch.impl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by PakAI on 22.03.2016.
 */
@Component
@ConfigurationProperties(prefix="synch.scheduler")
public class SchedulerConfiguration {

   private String cronExpression;

   /**
    * Gets cronExpression.
    *
    * @return Value of cronExpression.
    */
   public String getCronExpression() {
      return cronExpression;
   }

   /**
    * Sets new cronExpression.
    *
    * @param cronExpression New value of cronExpression.
    */
   public void setCronExpression(String cronExpression) {
      this.cronExpression = cronExpression;
   }
}
