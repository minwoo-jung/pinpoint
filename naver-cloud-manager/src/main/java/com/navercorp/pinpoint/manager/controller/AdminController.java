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

import com.navercorp.pinpoint.manager.service.MetadataService;
import com.navercorp.pinpoint.manager.service.RepositoryService;
import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping
public class AdminController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RepositoryService repositoryService;

    private final MetadataService metadataService;

    @Autowired
    public AdminController(RepositoryService repositoryService, MetadataService metadataService) {
        this.repositoryService = Objects.requireNonNull(repositoryService, "repositoryService must not be null");
        this.metadataService = Objects.requireNonNull(metadataService, "metadataService must not be null");
    }

    @GetMapping(value = "/organizations")
    public List<String> getAllOrganizations() {
        return metadataService.getAllOrganizationNames();
    }

    @GetMapping(value = "/repositories")
    public List<RepositoryInfoDetail> getAllRepositoryInfoDetails() {
        return repositoryService.getDetailedRepositoryInfos();
    }

    // TODO consider merging this with /repositories/{organizationName} and splitting response by role
    @GetMapping(value = "/repositories/{organizationName}/full")
    public RepositoryInfoDetail getRepositoryInfo(@PathVariable("organizationName") String organizationName) {
        return repositoryService.getDetailedRepositoryInfo(organizationName);
    }

    @DeleteMapping(value = "/repositories/{organizationName}")
    public ResponseEntity<Void> deleteRepository(@PathVariable("organizationName") String organizationName) {
        repositoryService.deleteRepository(organizationName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/repositories/{organizationName}/database")
    public DatabaseInfo getDatabaseInfo(@PathVariable("organizationName") String organizationName) {
        return repositoryService.getDatabaseInfo(organizationName);
    }

    @PatchMapping(value = "/repositories/{organizationName}/database")
    public ResponseEntity<Void> updateDatabaseSchema(@PathVariable("organizationName") String organizationName) {
        repositoryService.updateDatabase(organizationName);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/repositories/{organizationName}/database")
    public ResponseEntity<Void> deleteDatabase(@PathVariable("organizationName") String organizationName) {
        repositoryService.deleteDatabase(organizationName);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/repositories/{organizationName}/hbase")
    public HbaseInfo getHbaseInfo(@PathVariable("organizationName") String organizationName) {
        return repositoryService.getHbaseInfo(organizationName);
    }

    @PatchMapping(value = "/repositories/{organizationName}/hbase")
    public ResponseEntity<Void> updateHbaseSchema(@PathVariable("organizationName") String organizationName) {
        repositoryService.updateHbase(organizationName);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(value = "/repositories/{organizationName}/hbase")
    public ResponseEntity<Void> deleteHbase(@PathVariable("organizationName") String organizationName) {
        repositoryService.deleteHbase(organizationName);
        return ResponseEntity.accepted().build();
    }
}
