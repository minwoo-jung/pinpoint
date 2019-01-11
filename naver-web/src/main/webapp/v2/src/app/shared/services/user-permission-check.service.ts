import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';
import { Store, select } from '@ngrx/store';

import { AppState, STORE_KEY } from 'app/shared/store';

@Injectable()
export class UserPermissionCheckService {
    private unsubscribe: Subject<null> = new Subject();
    constructor(private store: Store<AppState>) {}

    isAllowedAdminMenuView(): Observable<boolean> {
        return this.getUserPermission().pipe(
            map((userPermission: IPermissions) => {
                return userPermission.permissionCollection.permsGroupAministration.viewAdminMenu || false;
            })
        );
    }
    private getUserPermission(): Observable<IPermissions> {
        return this.store.pipe(
            select(STORE_KEY.USER_PERMISSIONS),
            takeUntil(this.unsubscribe)
        );
    }
}
