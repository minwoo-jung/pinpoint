/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.RoleService;
import com.navercorp.pinpoint.web.util.AdditionValueValidator;
import com.navercorp.pinpoint.web.vo.role.RoleInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value = "roles")
public class RoleController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ROLE_ID = "roleId";

    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<String> getRoleList() {
        return roleService.selectRoleList();
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_ROLE)")
    @RequestMapping(value = "/role", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertRole(@RequestBody RoleInformation roleInformation) {
        if (AdditionValueValidator.validateRoleId(roleInformation.getRoleId()) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "roleId pattern is invalid");
            return result;
        }

        roleService.insertRoleInformation(roleInformation);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @RequestMapping(value = "/role", method = RequestMethod.GET)
    @ResponseBody
    public Object selectRole(@RequestParam(ROLE_ID) String roleId) {
        RoleInformation roleInformation = roleService.selectRoleInformation(roleId);
        return roleInformation;
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_ROLE)")
    @RequestMapping(value = "/role", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteRole(@RequestBody RoleInformation roleInformation) {
        roleService.deleteRoleInformation(roleInformation.getRoleId());

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    @PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_EDIT_ROLE)")
    @RequestMapping(value = "/role", method = RequestMethod.PUT)
    @ResponseBody
    public Object updateRole(@RequestBody RoleInformation roleInformation) {
        if (AdditionValueValidator.validateRoleId(roleInformation.getRoleId()) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "roleId pattern is invalid");
            return result;
        }

        roleService.updateRoleInformation(roleInformation);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }
}
