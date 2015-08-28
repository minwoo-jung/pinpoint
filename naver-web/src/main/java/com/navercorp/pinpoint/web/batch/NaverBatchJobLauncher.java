package com.navercorp.pinpoint.web.batch;

import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

public class NaverBatchJobLauncher extends JobLaunchSupport {
    public void innerSystemSyncJob() {
        JobParameters params = createTimeParameter();
        run("innerSystemSyncJob", params);
    }

    private JobParameters createTimeParameter() {
        JobParametersBuilder builder = new JobParametersBuilder();
        Date now = new Date();
        builder.addDate("schedule.date", now);
        return builder.toJobParameters();
    }
}
