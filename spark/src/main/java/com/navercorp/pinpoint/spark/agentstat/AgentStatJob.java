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
package com.navercorp.pinpoint.spark.agentstat;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.navercorp.pinpoint.spark.SparkHBaseTables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.navercorp.pinpoint.common.hbase.HBaseTables;

import scala.Tuple2;

/**
 * @author Jongho Moon
 *
 */
public class AgentStatJob {
    private static final String LOCAL_OUTPUT_DIRECTORY = "agentStat";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Needs arguments date(yyMMdd), period(in minutes)");
        }

        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
        Date from = format.parse(args[0]);
        Date to = new Date(from.getTime() + 24L * 60 * 60 * 1000);
        
        int period = Integer.valueOf(args[1]) * 60 * 1000;
        boolean local = args.length >= 3 && "local".equals(args[2]);
        
        SparkConf sparkConf = new SparkConf().setAppName("Agent Stat Aggregation [" + args[0] + ", " + args[1] + "min]");
        
        if (local) {
            sparkConf.setMaster("local[4]");
            deleteOutputDirectory(LOCAL_OUTPUT_DIRECTORY);
        }
        
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        
        Configuration hbaseConf = new Configuration();
        hbaseConf.addResource("hbase.xml");
        JavaHBaseContext hbaseContext = new JavaHBaseContext(sparkContext, hbaseConf);


        
        Scan scan = getAgentIdScan();
        
        JavaRDD<String> agentIdRDD = hbaseContext.hbaseRDD(HBaseTables.APPLICATION_INDEX, scan,
                (t) -> Arrays.stream(t._2.rawCells())
                            .map((cell) -> Bytes.toString(CellUtil.cloneQualifier(cell)))
                            .collect(Collectors.toList()) 
        ).flatMap((l) -> l).distinct();
        
        List<String> agentIds = agentIdRDD.collect();
        
        
        
        Stream<Tuple2<String, List<Scan>>> scanLists = agentIds.stream().map((agentId) -> {
            List<Scan> list = null;
            try {
                list = Arrays.asList(AgentStatHBaseUtils.getAgentStatScans(agentId, from.getTime(), to.getTime()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            return new Tuple2<String, List<Scan>>(agentId, list);
        });
        
        
        
        AgentStatBucketFactory bucketFactory = new AgentStatBucketFactory(period);
        scanLists.parallel().forEach((scans) -> {
            JavaRDD<List<AgentStat>>[] rdds = scans._2.stream().map((s) -> hbaseContext.hbaseRDD(HBaseTables.AGENT_STAT, s, (t) -> AgentStatHBaseUtils.toAgentStat(t._2))).toArray(JavaRDD[]::new);
            JavaRDD<AgentStat> statRDD = sparkContext.union(rdds).flatMap((list) -> list);
            
            JavaRDD<AgentStat> aggregatedRDD = statRDD.keyBy((stat) -> bucketFactory.getBucketOf(stat))
                    .reduceByKey(AgentStat::add)
                    .values()
                    .map((v) -> {v.setCollectInterval(period); return v;});
            
            hbaseContext.bulkPut(aggregatedRDD, SparkHBaseTables.AGENT_STAT_AGGR, AgentStatHBaseUtils::createPut);
            
            if (local && !aggregatedRDD.isEmpty()) {
                aggregatedRDD.sortBy(AgentStat::getTimestamp, true, 1).saveAsTextFile(LOCAL_OUTPUT_DIRECTORY + "/" + scans._1 + "/" + period);
            }
            
        });
        
        
        System.out.println(">>> Agent num: " + agentIds.size());
    }

    private static Scan getAgentIdScan() {
        Scan scan = new Scan();
        scan.addFamily(HBaseTables.APPLICATION_INDEX_CF_AGENTS);
        scan.setCaching(500);
        return scan;
    }
    
    private static void deleteOutputDirectory(String dir) throws IOException {
        Path output = Paths.get(dir);
        if (Files.exists(output)) {
            Files.walkFileTree(output, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return null;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;

                }
                
            });
        }
    }    
}
