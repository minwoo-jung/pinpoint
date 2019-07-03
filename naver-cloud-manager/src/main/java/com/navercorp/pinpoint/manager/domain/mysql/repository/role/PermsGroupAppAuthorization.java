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

package com.navercorp.pinpoint.manager.domain.mysql.repository.role;

/**
 * Copied from com.navercorp.pinpoint.web.vo.role.PermsGroupAdministration
 *
 * @author HyunGil Jeong
 */
// TODO refactor web commons
public class PermsGroupAppAuthorization {

    public static final PermsGroupAppAuthorization ADMIN = new PermsGroupAppAuthorization(true, true, true, true);
    public static final PermsGroupAppAuthorization DEFAULT = new PermsGroupAppAuthorization(false, false, false, false);
    public static final String APP_AUTHORIZATION = "appAuthorization";
    public static final String PREOCCUPANCY = "preoccupancy";
    public static final String EDIT_AUTHOR_FOR_EVERYTHING = "editAuthorForEverything";
    public static final String EDIT_AUTHOR_ONLY_MANAGER = "editAuthorOnlyManager";
    public static final String OBTAIN_ALL_AUTHORIZATION = "obtainAllAuthorization";

    private boolean preoccupancy; //First of all, you can assign userGroup to manager for application.

    private boolean editAuthorForEverything; // can edit authorization configue.
    private boolean editAuthorOnlyManager; // can edit only manager

    private boolean obtainAllAuthorization;

    public PermsGroupAppAuthorization(boolean preoccupancy, boolean editAuthorForEverything, boolean editAuthorOnlyManager, boolean obtainAllAuthorization) {
        this.preoccupancy = preoccupancy;
        this.editAuthorForEverything = editAuthorForEverything;
        this.editAuthorOnlyManager = editAuthorOnlyManager;
        this.obtainAllAuthorization = obtainAllAuthorization;
    }

    public PermsGroupAppAuthorization() {
    }

    public boolean getPreoccupancy() {
        return preoccupancy;
    }

    public boolean getEditAuthorForEverything() {
        return editAuthorForEverything;
    }

    public boolean getEditAuthorOnlyManager() {
        return editAuthorOnlyManager;
    }

    public void setPreoccupancy(boolean preoccupancy) {
        this.preoccupancy = preoccupancy;
    }

    public void setEditAuthorForEverything(boolean editAuthorForEverything) {
        this.editAuthorForEverything = editAuthorForEverything;
    }

    public void setEditAuthorOnlyManager(boolean editAuthorOnlyManager) {
        this.editAuthorOnlyManager = editAuthorOnlyManager;
    }

    public boolean getObtainAllAuthorization() {
        return obtainAllAuthorization;
    }

    public void setObtainAllAuthorization(boolean obtainAllAuthorization) {
        this.obtainAllAuthorization = obtainAllAuthorization;
    }

    public static PermsGroupAppAuthorization merge(PermsGroupAppAuthorization permsGroupAppAuthorization1, PermsGroupAppAuthorization permsGroupAppAuthorization2) {
        final boolean mergedPreoccupancy = permsGroupAppAuthorization1.getPreoccupancy() || permsGroupAppAuthorization2.getPreoccupancy();
        final boolean mergedEditAuthorForEverything = permsGroupAppAuthorization1.getEditAuthorForEverything() || permsGroupAppAuthorization2.getEditAuthorForEverything();
        final boolean mergedEditAuthorOnlyManager = permsGroupAppAuthorization1.getEditAuthorOnlyManager() || permsGroupAppAuthorization2.getEditAuthorOnlyManager();
        final boolean mergedObtainAllAuthorization = permsGroupAppAuthorization1.getObtainAllAuthorization() || permsGroupAppAuthorization2.getObtainAllAuthorization();
        return new PermsGroupAppAuthorization(mergedPreoccupancy, mergedEditAuthorForEverything, mergedEditAuthorOnlyManager, mergedObtainAllAuthorization);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupAppAuthorization{");
        sb.append("preoccupancy=").append(preoccupancy);
        sb.append(", EditAuthorForEverything=").append(editAuthorForEverything);
        sb.append(", EditAuthorOnlyManager=").append(editAuthorOnlyManager);
        sb.append(", ObtainAllAuthorization=").append(obtainAllAuthorization);
        sb.append('}');
        return sb.toString();
    }
}
