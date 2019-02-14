import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';

import { UserPermissionCheckService, RouteInfoCollectorService, UrlRouteManagerService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css']
})
export class ConfigPageComponent implements OnInit {
    canViewAdminMenu: boolean;
    constructor(
        private routeInfoCollectorService: RouteInfoCollectorService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}

    ngOnInit() {
        this.canViewAdminMenu = this.userPermissionCheckService.canViewAdminMenu();
    }
    onClickExit(): void {
        this.storeHelperService.getURLPath().pipe(
            take(1),
            map((urlPath: string) => {
                return urlPath.split('/').slice(1).map((path: string) => decodeURIComponent(path));
            })
        ).subscribe((url: string[]) => {
            this.urlRouteManagerService.moveOnPage({ url });
        });
    }
}
