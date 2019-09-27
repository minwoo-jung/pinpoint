/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.manager.controller;

import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.domain.mysql.repository.user.User;
import com.navercorp.pinpoint.manager.jdbc.RepositoryDatabaseDetails;
import com.navercorp.pinpoint.manager.jdbc.RepositoryDatabaseDetailsContextHolder;
import com.navercorp.pinpoint.manager.service.RepositoryService;
import com.navercorp.pinpoint.manager.service.UserService;
import com.navercorp.pinpoint.manager.vo.database.DatabaseInfo;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;
import com.navercorp.pinpoint.manager.vo.user.AdminCreateForm;
import com.navercorp.pinpoint.manager.vo.user.AdminInfo;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryCreateForm;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryUpdateForm;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoBasic;
import com.navercorp.pinpoint.manager.vo.repository.RepositoryInfoDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Void> createRepository(@Valid @RequestBody RepositoryCreateForm form) {
        String organizationName = form.getOrganizationName();
        repositoryService.createRepository(organizationName);
        final URI location = ServletUriComponentsBuilder.fromCurrentServletMapping()
                .path("/repositories/{organizationName}")
                .build()
                .expand(organizationName).toUri();
        return ResponseEntity.created(location).build();
    }

    // TODO consider merging this with /repositories/{organizationName}/full and splitting response by role
    @GetMapping(value = "/{organizationName}")
    public RepositoryInfoBasic getRepository(@PathVariable("organizationName") String organizationName) {
        return repositoryService.getBasicRepositoryInfo(organizationName);
    }

    @PatchMapping(value = "/{organizationName}")
    public ResponseEntity<Void> updateRepository(@PathVariable("organizationName") String organizationName,
                                                 @RequestBody RepositoryUpdateForm repositoryUpdateForm) {
        Boolean isEnabled = repositoryUpdateForm.getEnabled();
        Boolean isDeleted = repositoryUpdateForm.getDeleted();
        if (isEnabled == null && isDeleted == null) {
            return ResponseEntity.badRequest().build();
        }
        repositoryService.updateRepository(organizationName, isEnabled, isDeleted);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{organizationName}/applications")
    public List<String> getApplications(@PathVariable("organizationName") String organizationName) {
        return repositoryService.getApplicationNames(organizationName);
    }

    @GetMapping(value = "/{organizationName}/admins")
    public List<AdminInfo> getAdmins(@PathVariable("organizationName") String organizationName) {
        RepositoryInfoDetail repositoryInfo = repositoryService.getDetailedRepositoryInfo(organizationName);
        String databaseName = repositoryInfo.getDatabaseName();
        // TODO try AOP instead
        RepositoryDatabaseDetailsContextHolder.setRepositoryDatabaseDetails(new RepositoryDatabaseDetails(databaseName));
        try {
            return userService.getAdmins();
        } finally {
            RepositoryDatabaseDetailsContextHolder.resetRepositoryDatabaseDetails();
        }
    }

    @PostMapping(value = "/{organizationName}/admins")
    public ResponseEntity<Void> addAdmin(@PathVariable("organizationName") String organizationName,
                                         @Valid @RequestBody AdminCreateForm adminCreateForm) {
        RepositoryInfoDetail repositoryInfo = repositoryService.getDetailedRepositoryInfo(organizationName);
        String databaseName = repositoryInfo.getDatabaseName();
        // TODO try AOP instead
        RepositoryDatabaseDetailsContextHolder.setRepositoryDatabaseDetails(new RepositoryDatabaseDetails(databaseName));
        try {
            final User user = adminCreateForm.toUser();
            final String password = adminCreateForm.getPassword();
            userService.addAdmin(user, password);
            return ResponseEntity.noContent().build();
        } finally {
            RepositoryDatabaseDetailsContextHolder.resetRepositoryDatabaseDetails();
        }
    }

    @GetMapping(value = "/{organizationName}/database/status")
    public StorageStatus getDatabaseStatus(@PathVariable("organizationName") String organizationName) {
        DatabaseInfo databaseInfo = repositoryService.getDatabaseInfo(organizationName);
        return databaseInfo.getDatabaseStatus();
    }

    @PutMapping(value = "/{organizationName}/database")
    public ResponseEntity<Void> createDatabase(@PathVariable("organizationName") String organizationName) {
        repositoryService.createDatabase(organizationName);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/{organizationName}/hbase/status")
    public StorageStatus getHbaseStatusForOrganization(@PathVariable("organizationName") String organizationName) {
        HbaseInfo hbaseInfo = repositoryService.getHbaseInfo(organizationName);
        return hbaseInfo.getHbaseStatus();
    }

    @PutMapping(value = "/{organizationName}/hbase")
    public ResponseEntity<Void> createHbase(@PathVariable("organizationName") String organizationName) {
        repositoryService.createHbase(organizationName);
        return ResponseEntity.accepted().build();
    }
}
