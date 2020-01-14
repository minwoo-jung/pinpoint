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
package com.navercorp.pinpoint.manager.controller;

import com.navercorp.pinpoint.manager.service.SpanCountStatService;
import com.navercorp.pinpoint.manager.vo.Range;
import com.navercorp.pinpoint.manager.vo.SpanCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping("/spanCountStat")
public class SpanStatCountController {

    private SpanCountStatService spanCountStatService;

    @Autowired
    public SpanStatCountController(SpanCountStatService spanCountStatService) {
        this.spanCountStatService = Objects.requireNonNull(spanCountStatService, "spanCountStatService");
    }

    @GetMapping(value = "/organization")
    public List<SpanCount> getOrganizationSpanStat(
        @RequestParam(value = "organizationName") String organizationName,
        @RequestParam(value = "from") long from,
        @RequestParam(value = "to") long to) {
        final Range range = new Range(from, to);
        List<SpanCount> SpanCountList = spanCountStatService.getOrganizationSpanCount(organizationName, range);
        return SpanCountList;
    }

    @GetMapping(value = "/organization/application")
    public List<SpanCount> getApplicationStat(
        @RequestParam(value = "organizationName") String organizationName,
        @RequestParam(value = "applicationId") String applicationId,
        @RequestParam(value = "from") long from,
        @RequestParam(value = "to") long to) {
        final Range range = new Range(from, to);
        List<SpanCount> SpanCountList = spanCountStatService.getApplicationSpanCount(organizationName, applicationId, range);
        return SpanCountList;
    }

    @GetMapping(value = "/organization/application/agent")
    public List<SpanCount> getAgentSpanStat(
        @RequestParam(value = "organizationName") String organizationName,
        @RequestParam(value = "applicationId") String applicationId,
        @RequestParam(value = "agentId") String agentId,
        @RequestParam(value = "from") long from,
        @RequestParam(value = "to") long to) {
        final Range range = new Range(from, to);
        List<SpanCount> SpanCountList = spanCountStatService.getAgentSpanCount(organizationName, applicationId, agentId, range);
        return SpanCountList;
    }
}
