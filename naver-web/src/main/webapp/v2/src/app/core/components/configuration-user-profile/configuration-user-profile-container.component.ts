import { Component, OnInit } from '@angular/core';

import { UserPermissionCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-user-profile-container',
    templateUrl: './configuration-user-profile-container.component.html',
    styleUrls: ['./configuration-user-profile-container.component.css'],
})
export class ConfigurationUserProfileContainerComponent implements OnInit {
    showUserInfo: boolean;

    constructor(
        private userPermissionCheckService: UserPermissionCheckService
    ) {}

    ngOnInit() {
        this.showUserInfo = this.userPermissionCheckService.canViewAdminMenu();
    }
}
