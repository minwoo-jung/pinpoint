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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.profiler.util.NameValueList;
import com.navercorp.pinpoint.web.dao.hbase.HbaseApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;

/**
 * @author Jongho Moon
 *
 */
public class NGrinderTestVerifier {
    private final List<String> agents = new ArrayList<String>();
    private final NameValueList<MissingSequenceChecker> checkers = new NameValueList<MissingSequenceChecker>();
    
    @Autowired
    private HbaseApplicationTraceIndexDao dao;
    
    private static final int FETCH_SIZE = 50000;
    
    public void verify(String testId) {
        Range range = new Range(0, Long.MAX_VALUE);
        
        while (true) { 
            LimitedScanResult<List<TransactionId>> result = dao.scanTraceIndex(testId, range, FETCH_SIZE);
            List<TransactionId> list = result.getScanData();
            
            System.out.println("TRACES[" + list.size() + "]");
            
            for (TransactionId id : list) {
                
                String agentId = id.getAgentId() + ":" + id.getAgentStartTime();
                MissingSequenceChecker checker = checkers.get(agentId);
                
                if (checker == null) {
                    agents.add(agentId);
                    checker = new MissingSequenceChecker();
                    checkers.add(agentId, checker);
                }
                
                checker.add(id.getTransactionSequence());
            }
            
            if (result.getLimitedTime() == range.getFrom()) {
                break;
            } else {
                range = new Range(0, result.getLimitedTime());
            }
        }
        
        System.out.println("SEQUENCE CHECK RESULT:");
        
        long totalSent = 0;
        long totalMissing = 0;
                
        for (String agent : agents) {
            MissingSequenceChecker checker = checkers.get(agent);
            
            long sent = checker.getLast();
            long missing = checker.getMissingCount();
            double percentage = 100.0 * missing / sent; 
            
            System.out.println(agent + ": " + percentage + "% (" + missing + " / " + sent + "): " + checker);

            totalSent += sent;
            totalMissing += missing;
        }
        
        double percentage = 100.0 * totalMissing / totalSent;
        
        System.out.println("TOTAL: " + percentage + "% (" + totalMissing + " / " + totalSent + ")");
    }
    
    
}
