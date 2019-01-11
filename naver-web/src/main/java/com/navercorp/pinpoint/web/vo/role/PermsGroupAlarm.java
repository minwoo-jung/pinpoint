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

    private boolean editAlarmForEverything;
    private boolean editAlarmOnlyGroupMember;

    public PermsGroupAlarm(boolean editAlarmForEverything, boolean editAlarmOnlyGroupMember) {
        this.editAlarmForEverything = editAlarmForEverything;
        this.editAlarmOnlyGroupMember = editAlarmOnlyGroupMember;
    }

    public PermsGroupAlarm() {
    }

    public boolean getEditAlarmForEverything() {
        return editAlarmForEverything;
    }

    public boolean getEditAlarmOnlyGroupMember() {
        return editAlarmOnlyGroupMember;
    }

    public void setEditAlarmForEverything(boolean editAlarmForEverything) {
        this.editAlarmForEverything = editAlarmForEverything;
    }

    public void setEditAlarmOnlyGroupMember(boolean editAlarmOnlyGroupMember) {
        this.editAlarmOnlyGroupMember = editAlarmOnlyGroupMember;
    }

    public static PermsGroupAlarm merge(PermsGroupAlarm permsGroupAlarm1, PermsGroupAlarm permsGroupAlarm2) {
        final boolean mergedEditAlarmForEverything = permsGroupAlarm1.getEditAlarmForEverything() || permsGroupAlarm2.getEditAlarmForEverything();
        final boolean mergedEditAlarmOnlyGroupMember = permsGroupAlarm1.getEditAlarmOnlyGroupMember() || permsGroupAlarm2.getEditAlarmOnlyGroupMember();
        return new PermsGroupAlarm(mergedEditAlarmForEverything, mergedEditAlarmOnlyGroupMember);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupAlarm{");
        sb.append("editAlarmForEverything=").append(editAlarmForEverything);
        sb.append(", editAlarmOnlyGroupMember=").append(editAlarmOnlyGroupMember);
        sb.append('}');
        return sb.toString();
    }
}
