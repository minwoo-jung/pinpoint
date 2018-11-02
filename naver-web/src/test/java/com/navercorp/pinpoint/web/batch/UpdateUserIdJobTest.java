/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class UpdateUserIdJobTest {

    /**
     * 준비 방법
     * 1. BatchNameSpaceInfoHolder의 클래스의 생성자를 내용을 아래로 변경함.
     *    public BatchNameSpaceInfoHolder(StepExecution stepExecution) {
     *    batchNameSpaceInfo = new NameSpaceInfo("BATCH", "pinpoint", "default");
     *    }
     * 2. jtbc.properties 내의 db 주소를 dev, real로 변경하여 batch 실행
     * 3. UpdateUserIdTasklet의 createUserInfoList 메소드에서 이관 대상 직원 정보 추가해서 테스트 alpha 환경부터
     */
    public static void main(String[] args) throws Exception{
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext(
            "/applicationContext-test.xml",
            "/batch/applicationContext-batch-test.xml",
            "/batch/updateUserIdJob.xml");
        JobLauncherTestUtils testLauncher = applicationContext.getBean(JobLauncherTestUtils.class);

        JobExecution jobExecution = testLauncher.launchJob(getParameters());
        BatchStatus status = jobExecution.getStatus();
        assertEquals(BatchStatus.COMPLETED, status);

        applicationContext.close();
    }

    private static JobParameters getParameters() {
        Map<String, JobParameter> parameters = new HashMap<>();
        parameters.put("schedule.scheduledFireTime", new JobParameter(new Date()));
        return new JobParameters(parameters);
    }
}
