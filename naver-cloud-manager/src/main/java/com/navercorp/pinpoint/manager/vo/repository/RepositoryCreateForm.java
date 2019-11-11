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

package com.navercorp.pinpoint.manager.vo.repository;

import com.navercorp.pinpoint.manager.validator.constraint.OrganizationNameConstraint;
import com.navercorp.pinpoint.manager.vo.user.AdminCreateForm;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;

/**
 * @author HyunGil Jeong
 */
public class RepositoryCreateForm {

    @NotBlank
    @OrganizationNameConstraint
    private String organizationName;

    @Valid
    private AdminCreateForm admin;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public AdminCreateForm getAdmin() {
        return admin;
    }

    public void setAdmin(AdminCreateForm admin) {
        this.admin = admin;
    }
}
