/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.ngrinder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Jongho Moon
 *
 */
public class CollectorPerfTestRunner {
    private final NGrinder ngrinder;
    private final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-test.xml");
    
    public CollectorPerfTestRunner(String userId, String password) {
        this.ngrinder = new NGrinder(userId, password);
    }
    
    public void run(int agent, int process, int thread, long duration) {
        String id = ngrinder.test(agent, process, thread, duration);
        
        NGrinderTestVerifier verifier = context.getBean(NGrinderTestVerifier.class);
        verifier.verify(id);
    }
    
    public static void main(String args[]) {
        new CollectorPerfTestRunner("id", "pw").run(2, 1, 10, 30 * 1000);
    }

}
