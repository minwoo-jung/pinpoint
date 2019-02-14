import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
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
    private userPermissions: IPermissions;
    constructor(private store: Store<AppState>) {
        this.store.pipe(
            select(STORE_KEY.USER_PERMISSIONS),
            takeUntil(this.unsubscribe)
        ).subscribe((userPermissions: IPermissions) => {
            this.userPermissions = userPermissions;
        });
    }

    canViewAdminMenu(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAdministration.viewAdminMenu;
    }
    canEditUser(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAdministration.editUser;
    }
    canEditRole(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAdministration.editRole;
    }
    canAddUserGroup(): boolean {
        return this.userPermissions.permissionCollection.permsGroupUserGroup.editGroupForEverything ||
            this.userPermissions.permissionCollection.permsGroupUserGroup.editGroupOnlyGroupMember;
    }
    canRemoveAllGroupMember(): boolean {
        return this.userPermissions.permissionCollection.permsGroupUserGroup.editGroupForEverything;
    }
    canRemoveAllGroupMemberExceptMe(): boolean {
        return this.userPermissions.permissionCollection.permsGroupUserGroup.editGroupOnlyGroupMember;
    }
    static isRestrictedURL(url: string): boolean {
        return UserPermissionCheckService.RESTRICTED_URL.reduce((prev: boolean, retrictedUrl: string) => {
            return prev || url.startsWith(retrictedUrl);
        }, false);
    }
}
