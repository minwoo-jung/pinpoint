/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.manager.vo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author minwoo.jung
 */
public class SpanCount {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private long timestamp;
    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestampString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date parsedDate = format.parse(timestampString);
        this.timestamp = parsedDate.getTime();

    }
}
