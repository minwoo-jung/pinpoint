/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.heatmap.service;

import com.navercorp.pinpoint.web.heatmap.dao.HeatmapChartDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
@ExtendWith(MockitoExtension.class)
class HeatmapChartServiceImplTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    private HeatmapChartDao heatmapChartDao;

    private HeatmapChartServiceImpl heatmapChartService;

    @BeforeEach
    public void setUp() {
        heatmapChartService = new HeatmapChartServiceImpl(heatmapChartDao);
    }

    @Test
    public void calculateTimeInterval() {

        assertEquals(200, heatmapChartService.calculateTimeInterval(0, 9000));
        assertEquals(300, heatmapChartService.calculateTimeInterval(0, 15000));
        assertEquals(200, heatmapChartService.calculateTimeInterval(10000, 15000));
        assertEquals(6020, heatmapChartService.calculateTimeInterval(10000, 305000));

        int min = 0;
        int max = 133333;
        int timeInterval = heatmapChartService.calculateTimeInterval(min, max);

        int yAxis = 0;
        yAxis += timeInterval;
        int i = 0;
        while (yAxis < max) {
            logger.info("index " + i + " : " + yAxis);
            yAxis += timeInterval;
            i++;
        }
        logger.info("index " + i + " : " + max);
        logger.info("=========end=========");

        logger.info("max - (yAxis - timeInterval)  : " + (max - (yAxis - timeInterval)));
        logger.info("timeInterval : " + timeInterval);
        logger.info("max : " + max);
        logger.info("yAxis : " + yAxis);
        logger.info("(yAxis - timeInterval)  : " + (yAxis - timeInterval));
    }

    @Test
    public void bucketElapsedTimeTest() {
        int timeInterval = 200;
        int elapsedTime = 0;
        assertEquals(200, (((elapsedTime / timeInterval) + 1) * timeInterval));
        elapsedTime = 100;
        assertEquals(200, (((elapsedTime / timeInterval) + 1) * timeInterval));

        timeInterval = 300;
        elapsedTime = 500;
        assertEquals(600, (((elapsedTime / timeInterval) + 1) * timeInterval));
        elapsedTime = 666;
        assertEquals(900, (((elapsedTime / timeInterval) + 1) * timeInterval));
    }

    @Test
    public void findLargestMultipleBelowTest() {
        assertEquals(9800, heatmapChartService.findLargestMultipleBelow(10000, 0, 200));
        assertEquals(9800, heatmapChartService.findLargestMultipleBelow(10000, 200, 200));
        assertEquals(19600, heatmapChartService.findLargestMultipleBelow(20000, 0, 400));
        assertEquals(19800, heatmapChartService.findLargestMultipleBelow(20000, 200, 400));
        assertEquals(29925, heatmapChartService.findLargestMultipleBelow(30000, 0, 315));
        assertEquals(29710, heatmapChartService.findLargestMultipleBelow(30000, 100, 315));
    }

    @Test
    public void mapToNextIntervalBucketTest() {
        assertEquals(20000, mapToNextIntervalBucket(2000000, 20000, 0, 200));
        assertEquals(50, mapToNextIntervalBucket(10, 20000,50, 200));
        assertEquals(50, mapToNextIntervalBucket(50, 20000,50, 200));


        assertEquals(10000, mapToNextIntervalBucket(9999, 20000,0, 200));
        assertEquals(10000, mapToNextIntervalBucket(10000, 20000,0, 200));
        assertEquals(10000, mapToNextIntervalBucket(10000, 20000,200, 200));
        assertEquals(20000, mapToNextIntervalBucket(20000, 20000,0, 400));
        assertEquals(20200, mapToNextIntervalBucket(20000, 20000,200, 400));
        assertEquals(20200, mapToNextIntervalBucket(20200, 30000,200, 400));
        assertEquals(20200, mapToNextIntervalBucket(20199, 30000,200, 400));

        assertEquals(30240, mapToNextIntervalBucket(30000, 60000,0, 315));
        assertEquals(630, mapToNextIntervalBucket(316, 60000,0, 315));
        assertEquals(30025, mapToNextIntervalBucket(30000, 60000,100, 315));
        assertEquals(415, mapToNextIntervalBucket(101, 60000,100, 315));
        assertEquals(30025, mapToNextIntervalBucket(30025, 60000,100, 315));
        assertEquals(30025, mapToNextIntervalBucket(30024, 60000,100, 315));

        assertEquals(400, mapToNextIntervalBucket(400, 1000,200, 200));
    }

    protected int mapToNextIntervalBucket(int value, int max, int startValue, int interval) {
        if (value > max) {
            return max;
        } else if (value <= startValue) {
            return startValue;
        } else {
            return (((((value - startValue - 1) / interval) + 1) * interval) + startValue);
        }
    }
}
