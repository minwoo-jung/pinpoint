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
public class PermsGroupAlarm {

    public final static PermsGroupAlarm DEFAULT = new PermsGroupAlarm(false, false);
    public final static String ALARM = "alarm";
    public final static String EDIT_ALARM_FOR_EVERYTHING = "editAlarmForEverything";
    public final static String EDIT_ALARM_ONLY_MANAGER = "editAlarmOnlyManager";

    private boolean editAlarmForEverything;
    private boolean editAlarmOnlyManager;

    public PermsGroupAlarm(boolean editAlarmForEverything, boolean editAlarmOnlyManager) {
        this.editAlarmForEverything = editAlarmForEverything;
        this.editAlarmOnlyManager = editAlarmOnlyManager;
    }

    public PermsGroupAlarm() {
    }

    public boolean getEditAlarmForEverything() {
        return editAlarmForEverything;
    }

    public boolean getEditAlarmOnlyManager() {
        return editAlarmOnlyManager;
    }

    public void setEditAlarmForEverything(boolean editAlarmForEverything) {
        this.editAlarmForEverything = editAlarmForEverything;
    }

    public void setEditAlarmOnlyManager(boolean editAlarmOnlyManager) {
        this.editAlarmOnlyManager = editAlarmOnlyManager;
    }

    public static PermsGroupAlarm merge(PermsGroupAlarm permsGroupAlarm1, PermsGroupAlarm permsGroupAlarm2) {
        final boolean mergedEditAlarmForEverything = permsGroupAlarm1.getEditAlarmForEverything() || permsGroupAlarm2.getEditAlarmForEverything();
        final boolean mergedEditAlarmOnlyGroupMember = permsGroupAlarm1.getEditAlarmOnlyManager() || permsGroupAlarm2.getEditAlarmOnlyManager();
        return new PermsGroupAlarm(mergedEditAlarmForEverything, mergedEditAlarmOnlyGroupMember);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupAlarm{");
        sb.append("editAlarmForEverything=").append(editAlarmForEverything);
        sb.append(", editAlarmOnlyManager=").append(editAlarmOnlyManager);
        sb.append('}');
        return sb.toString();
    }
}