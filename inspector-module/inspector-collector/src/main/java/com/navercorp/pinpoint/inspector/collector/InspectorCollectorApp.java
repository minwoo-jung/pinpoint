/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.collector;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * @author minwoo.jung
 */
@ComponentScan({"com.navercorp.pinpoint.inspector.collector"})
@PropertySource({"classpath:inspector/collector/kafka-topic.properties", "classpath:inspector/collector/kafka-producer-factory.properties"})
// TODO : (minwoo) 환경별로 properties 파일 뽑아주기
@ImportResource({"classpath:inspector/collector/applicationContext-inspector-pinot-kafka.xml"})
public class InspectorCollectorApp {
}
