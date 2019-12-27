import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { UrlPath } from 'app/shared/models';
import { UserConfigurationDataService } from './user-configuration-data.service';
import { UserPermissionCheckService } from './user-permission-check.service';

@Injectable()
export class UserConfigurationResolverService implements Resolve<IUserConfiguration> {
    constructor(
        private router: Router,
        private userConfigurationDataService: UserConfigurationDataService
    ) {}
    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IUserConfiguration> {
        return this.userConfigurationDataService.getUserConfiguration().pipe(
            tap((userConfiguration: IUserConfiguration) => {
                if (userConfiguration.permission.permissionCollection.permsGroupAdministration.viewAdminMenu === false) {
                    if (UserPermissionCheckService.isRestrictedURL(state.url)) {
                        this.router.navigate(['/' + UrlPath.MAIN]);
                    }
                }
            }),
            catchError((error: any) => {
                this.router.navigate(['/' + UrlPath.ERROR]);
                return EMPTY;
            })
        );
    }
}
