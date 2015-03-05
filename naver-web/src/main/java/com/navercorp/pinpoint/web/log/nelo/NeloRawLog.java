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

package com.navercorp.pinpoint.web.log.nelo;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class NeloRawLog {
    
    private Map<String, String> _source;

    public Map<String, String> get_source() {
        return _source;
    }

    public void set_source(Map<String, String> _source) {
        this._source = _source;
    }


}
