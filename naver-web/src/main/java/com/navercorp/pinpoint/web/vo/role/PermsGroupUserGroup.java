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
public class PermsGroupUserGroup {

    public final static PermsGroupUserGroup DEFAULT = new PermsGroupUserGroup(false, false);

    private boolean editGroupForEverything;
    private boolean editGroupOnlyGroupMember;

    public PermsGroupUserGroup(boolean editGroupForEverything, boolean editGroupOnlyGroupMember) {
        this.editGroupForEverything = editGroupForEverything;
        this.editGroupOnlyGroupMember = editGroupOnlyGroupMember;
    }

    public PermsGroupUserGroup() {
    }

    public static PermsGroupUserGroup merge(PermsGroupUserGroup permsGroupUserGroup1, PermsGroupUserGroup permsGroupUserGroup2) {
        final boolean mergedEditGroupForEverything = permsGroupUserGroup1.getEditGroupForEverything() || permsGroupUserGroup2.getEditGroupForEverything();
        final boolean mergedEditGroupOnlyGroupMember = permsGroupUserGroup1.getEditGroupOnlyGroupMember() || permsGroupUserGroup2.getEditGroupOnlyGroupMember();
        return new PermsGroupUserGroup(mergedEditGroupForEverything, mergedEditGroupOnlyGroupMember);
    }

    public boolean getEditGroupForEverything() {
        return editGroupForEverything;
    }

    public boolean getEditGroupOnlyGroupMember() {
        return editGroupOnlyGroupMember;
    }

    public void setEditGroupForEverything(boolean editGroupForEverything) {
        this.editGroupForEverything = editGroupForEverything;
    }

    public void setEditGroupOnlyGroupMember(boolean editGroupOnlyGroupMember) {
        this.editGroupOnlyGroupMember = editGroupOnlyGroupMember;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PermsGroupUserGroup{");
        sb.append("editGroupForEverything=").append(editGroupForEverything);
        sb.append(", editGroupOnlyGroupMember=").append(editGroupOnlyGroupMember);
        sb.append('}');
        return sb.toString();
    }
}
