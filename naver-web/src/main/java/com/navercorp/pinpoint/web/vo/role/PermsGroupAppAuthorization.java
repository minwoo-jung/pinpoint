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
package com.navercorp.pinpoint.web.vo.role;

/**
 * @author minwoo.jung
 */
public class PermsGroupAppAuthorization {

    public final static PermsGroupAppAuthorization DEFAULT = new PermsGroupAppAuthorization(false, false, false);
    public final static String APP_AUTHORIZATION = "appAuthorization";
    public final static String PREOCCUPANCY = "preoccupancy";
    public final static String EDIT_AUTHOR_FOR_EVERYTHING = "editAuthorForEverything";
    public final static String EDIT_AUTHOR_ONLY_MANAGER = "editAuthorOnlyManager";

    private boolean preoccupancy; //First of all, you can assign userGroup to manager for application.

    private boolean editAuthorForEverything; // can edit authorization configue.
    private boolean editAuthorOnlyManager; // can edit only manager

    public PermsGroupAppAuthorization(boolean preoccupancy, boolean editAuthorForEverything, boolean editAuthorOnlyManager) {
        this.preoccupancy = preoccupancy;
        this.editAuthorForEverything = editAuthorForEverything;
        this.editAuthorOnlyManager = editAuthorOnlyManager;
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

    public static PermsGroupAppAuthorization merge(PermsGroupAppAuthorization permsGroupAppAuthorization1, PermsGroupAppAuthorization permsGroupAppAuthorization2) {
        final boolean mergedPreoccupancy = permsGroupAppAuthorization1.getPreoccupancy() || permsGroupAppAuthorization2.getPreoccupancy();
        final boolean mergedEditAuthorForEverything = permsGroupAppAuthorization1.getEditAuthorForEverything() || permsGroupAppAuthorization2.getEditAuthorForEverything();
        final boolean mergedEditAuthorOnlyManager = permsGroupAppAuthorization1.getEditAuthorOnlyManager() || permsGroupAppAuthorization2.getEditAuthorOnlyManager();
        return new PermsGroupAppAuthorization(mergedPreoccupancy, mergedEditAuthorForEverything, mergedEditAuthorOnlyManager);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupAppAuthorization{");
        sb.append("preoccupancy=").append(preoccupancy);
        sb.append(", EditAuthorForEverything=").append(editAuthorForEverything);
        sb.append(", EditAuthorOnlyManager=").append(editAuthorOnlyManager);
        sb.append('}');
        return sb.toString();
    }
}
