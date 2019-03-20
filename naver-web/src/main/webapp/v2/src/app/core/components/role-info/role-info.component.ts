import { Component, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';

export interface IPermissionData {
    isChanged: boolean;
    viewAdminMenu: boolean;
    editUser: boolean;
    editRole: boolean;
    callAdminApi: boolean;
    preoccupancy: boolean;
    editAuthorForEverything: boolean;
    editAuthorOnlyManager: boolean;
    obtainAllAuthorization: boolean;
    editAlarmForEverything: boolean;
    editAlarmOnlyManager: boolean;
    editGroupForEverything: boolean;
    editGroupOnlyGroupMember: boolean;
}
@Component({
    selector: 'pp-role-info',
    templateUrl: './role-info.component.html',
    styleUrls: ['./role-info.component.css']
})
export class RoleInfoComponent implements OnInit, OnChanges {
    @Input() hasRoleEditPerm: boolean;
    @Input() roleInfo: IPermissions;
    @Input() i18nText: {[key: string]: string};
    @Output() outChanged: EventEmitter<IPermissionData> = new EventEmitter();
    viewAdminMenu: boolean;
    editUser: boolean;
    editRole: boolean;
    callAdminApi: boolean;
    preoccupancy: boolean;
    editAuthorForEverything: boolean;
    editAuthorOnlyManager: boolean;
    obtainAllAuthorization: boolean;
    editAlarmForEverything: boolean;
    editAlarmOnlyManager: boolean;
    editGroupForEverything: boolean;
    editGroupOnlyGroupMember: boolean;
    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes.roleInfo.currentValue) {
            this.viewAdminMenu = this.roleInfo.permissionCollection.permsGroupAdministration.viewAdminMenu;
            this.editUser = this.roleInfo.permissionCollection.permsGroupAdministration.editUser;
            this.editRole = this.roleInfo.permissionCollection.permsGroupAdministration.editRole;
            this.callAdminApi = this.roleInfo.permissionCollection.permsGroupAdministration.callAdminApi;
            this.preoccupancy = this.roleInfo.permissionCollection.permsGroupAppAuthorization.preoccupancy;
            this.editAuthorForEverything = this.roleInfo.permissionCollection.permsGroupAppAuthorization.editAuthorForEverything;
            this.editAuthorOnlyManager = this.roleInfo.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager;
            this.obtainAllAuthorization = this.roleInfo.permissionCollection.permsGroupAppAuthorization.obtainAllAuthorization;
            this.editAlarmForEverything = this.roleInfo.permissionCollection.permsGroupAlarm.editAlarmForEverything;
            this.editAlarmOnlyManager = this.roleInfo.permissionCollection.permsGroupAlarm.editAlarmOnlyManager;
            this.editGroupForEverything = this.roleInfo.permissionCollection.permsGroupUserGroup.editGroupForEverything;
            this.editGroupOnlyGroupMember = this.roleInfo.permissionCollection.permsGroupUserGroup.editGroupOnlyGroupMember;
        }
    }
    onChange(): void {
        this.outChanged.next({
            isChanged: this.isChangedLastRoleInfo(),
            viewAdminMenu: this.viewAdminMenu,
            editUser: this.editUser,
            editRole: this.editRole,
            callAdminApi: this.callAdminApi,
            preoccupancy: this.preoccupancy,
            editAuthorForEverything: this.editAuthorForEverything,
            editAuthorOnlyManager: this.editAuthorOnlyManager,
            obtainAllAuthorization: this.obtainAllAuthorization,
            editAlarmForEverything: this.editAlarmForEverything,
            editAlarmOnlyManager: this.editAlarmOnlyManager,
            editGroupForEverything: this.editGroupForEverything,
            editGroupOnlyGroupMember: this.editGroupOnlyGroupMember
        });
    }
    private isChangedLastRoleInfo(): boolean {
        return this.viewAdminMenu !== this.roleInfo.permissionCollection.permsGroupAdministration.viewAdminMenu ||
            this.editUser !== this.roleInfo.permissionCollection.permsGroupAdministration.editUser ||
            this.editRole !== this.roleInfo.permissionCollection.permsGroupAdministration.editRole ||
            this.callAdminApi !== this.roleInfo.permissionCollection.permsGroupAdministration.callAdminApi ||
            this.preoccupancy !== this.roleInfo.permissionCollection.permsGroupAppAuthorization.preoccupancy ||
            this.editAuthorForEverything !== this.roleInfo.permissionCollection.permsGroupAppAuthorization.editAuthorForEverything ||
            this.editAuthorOnlyManager !== this.roleInfo.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager ||
            this.obtainAllAuthorization !== this.roleInfo.permissionCollection.permsGroupAppAuthorization.obtainAllAuthorization ||
            this.editAlarmForEverything !== this.roleInfo.permissionCollection.permsGroupAlarm.editAlarmForEverything ||
            this.editAlarmOnlyManager !== this.roleInfo.permissionCollection.permsGroupAlarm.editAlarmOnlyManager ||
            this.editGroupForEverything !== this.roleInfo.permissionCollection.permsGroupUserGroup.editGroupForEverything ||
            this.editGroupOnlyGroupMember !== this.roleInfo.permissionCollection.permsGroupUserGroup.editGroupOnlyGroupMember;
    }
}
