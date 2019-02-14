import { Component, OnInit } from '@angular/core';
import { UserPermissionCheckService } from 'app/shared/services';
@Component({
    selector: 'pp-configuration-user-group-container',
    templateUrl: './configuration-user-group-container.component.html',
    styleUrls: ['./configuration-user-group-container.component.css']
})
export class ConfigurationUserGroupContainerComponent implements OnInit {
    viewAppConfigurationTemp: boolean;
    constructor(private userPermissionCheckService: UserPermissionCheckService) {}
    ngOnInit() {
        this.viewAppConfigurationTemp = this.userPermissionCheckService.canViewAdminMenu();
    }
}
