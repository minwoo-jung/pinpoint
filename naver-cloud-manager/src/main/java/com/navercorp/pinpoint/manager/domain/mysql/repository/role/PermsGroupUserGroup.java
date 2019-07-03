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
 * Copied from com.navercorp.pinpoint.web.vo.role.PermsGroupUserGroup
 *
 * @author HyunGil Jeong
 */
// TODO refactor web commons
public class PermsGroupUserGroup {

    public static final PermsGroupUserGroup ADMIN = new PermsGroupUserGroup(true, true);
    public static final PermsGroupUserGroup DEFAULT = new PermsGroupUserGroup(false, false);
    public static final String USER_GROUP = "userGroup";
    public static final String EDIT_GROUP_FOR_EVERYTHING = "editGroupForEverything";
    public static final String EDIT_GROUP_ONLY_GROUPMEMBER = "editGroupOnlyGroupMember";

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
