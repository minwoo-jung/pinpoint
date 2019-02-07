import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';
import { Store, select } from '@ngrx/store';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { AppState, STORE_KEY } from 'app/shared/store';

@Injectable()
export class UserPermissionCheckService {
    static RESTRICTED_URL = [
        '/' + UrlPath.CONFIG + '/' + UrlPathId.USERS,
        '/' + UrlPath.CONFIG + '/' + UrlPathId.ROLE
    ];
    private unsubscribe: Subject<null> = new Subject();
    constructor(private store: Store<AppState>) {}

    isAllowedAdminMenuView(): Observable<boolean> {
        return this.getUserPermission().pipe(
            map((userPermission: IPermissions) => {
                return false;
                // return userPermission.permissionCollection.permsGroupAdministration.viewAdminMenu || false;
            })
        );
    }
    private getUserPermission(): Observable<IPermissions> {
        return this.store.pipe(
            select(STORE_KEY.USER_PERMISSIONS),
            takeUntil(this.unsubscribe)
        );
    }

    static isRestrictedURL(url: string): boolean {
        return UserPermissionCheckService.RESTRICTED_URL.reduce((prev: boolean, retrictedUrl: string) => {
            return prev || url.startsWith(retrictedUrl);
        }, false);
    }
}
