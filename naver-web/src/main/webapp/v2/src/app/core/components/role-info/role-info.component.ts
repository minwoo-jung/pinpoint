import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-role-info',
    templateUrl: './role-info.component.html',
    styleUrls: ['./role-info.component.css']
})
export class RoleInfoComponent implements OnInit, OnChanges {
    @Input() roleInfo: IPermissions;
    @Input() i18nText: {[key: string]: string};
    disableFormControl = true;
    permsGroupAppAuthorization = 'false';
    permsGroupAlarm = 'false';
    permsGroupUserGroup = 'false';

    constructor() {}
    ngOnInit() {
        this.setPermission();
    }
    ngOnChanges(changes: SimpleChanges) {
        this.setPermission();
    }
    private setPermission() {
        this.permsGroupAppAuthorization = this.roleInfo.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager + '';
        this.permsGroupAlarm = this.roleInfo.permissionCollection.permsGroupAlarm.editAuthorForEverything + '';
        this.permsGroupUserGroup = this.roleInfo.permissionCollection.permsGroupUserGroup.editGroupOnlyGroupMember + '';
    }
}
