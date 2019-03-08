import { Component, OnInit } from '@angular/core';

import { UserPermissionCheckService, NewUrlStateNotificationService, UrlRouteManagerService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css']
})
export class ConfigPageComponent implements OnInit {
    canViewAdminMenu: boolean;
    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}

    ngOnInit() {
        this.canViewAdminMenu = this.userPermissionCheckService.canViewAdminMenu();
    }
    onClickExit(): void {
        const { startPath, pathParams, queryParams } = this.newUrlStateNotificationService.getPrevPageUrlInfo();
        const url = startPath === UrlPath.CONFIG ? [UrlPath.MAIN] : [startPath, ...[ ...pathParams.values() ]];
        const queryParam = [ ...queryParams.entries() ].reduce((acc: object, [key, value]: string[]) => {
            return { ...acc, [key]: value };
        }, {});

        this.urlRouteManagerService.moveOnPage({ url, queryParam });
    }
}
