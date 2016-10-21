/*
 * Copyright 2014 NAVER Corp.
 *
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
package com.navercorp.pinpoint.flink.workcount;

/**
 * @author minwoo.jung
 */

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * ==목표==
 *    stat 정보 조회시 시간을 길게하면 1주 이상 시간이 오래걸리는데
 *    적당히 데이터를 함축해서 1달 단위로 검색이 쉽게 되도록 하자.
 */
public class WorkCountStreamingJob {
    // *************************************************************************
    // PROGRAM
    // *************************************************************************

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();// 로컬 뭐로 변경해야 됨
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // make parameters available in the web interface
        final ParameterTool params = ParameterTool.fromArgs(args);
        env.getConfig().setGlobalJobParameters(params);

        // get input data
        DataStream<String> text;
        if (params.has("input")) {
            // read the text file from given input path
            text = env.readTextFile(params.get("input"));
        } else {
            System.out.println("Executing WordCount example with default input data set.");
            System.out.println("Use --input to specify file input.");
            // get default test text data
            text = env.fromElements(WordCountData.WORDS);
        }

//        DataStream<Tuple2<String, Integer>> counts =
//                // split up the lines in pairs (2-tuples) containing: (word,1)
//                text.flatMap(new Tokenizer()).keyBy(0).window(TumblingEventTimeWindows.of(Time.seconds(1000))).sum(1);

        DataStream<Tuple2<String, Integer>> counts =  text.flatMap(new Tokenizer()).assignTimestampsAndWatermarks(new LinearTimestamp())
                                                            .keyBy(0)
                                                            .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                                                            .sum(1);

        // emit result
        if (params.has("output")) {
            counts.writeAsText("F:\\workspace_intellij\\pinpointMinwoo\\output\\resul1");
        } else {
            counts.print();
        }

        // execute program
        env.execute("Streaming WordCount");
    }

    /**
     * Implements the string tokenizer that splits sentences into words as a
     * user-defined FlatMapFunction. The function takes a line (String) and
     * splits it into multiple pairs in the form of "(word,1)" ({@code Tuple2<String,
     * Integer>}).
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {
        private static final long serialVersionUID = 1L;

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out)
                throws Exception {
            // normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<String, Integer>(token, 1));
                }
            }
        }
    }

    public static class LinearTimestamp implements AssignerWithPunctuatedWatermarks<Tuple2<String, Integer>> {
        private static final long serialVersionUID = 1L;

        private long counter = 0L;

        @Override
        public long extractTimestamp(Tuple2<String, Integer> element, long previousElementTimestamp) {
            return counter += 1000L;
        }

        @Override
        public Watermark checkAndGetNextWatermark(Tuple2<String, Integer> lastElement, long extractedTimestamp) {
            return new Watermark(counter - 1);
        }
    }
}
