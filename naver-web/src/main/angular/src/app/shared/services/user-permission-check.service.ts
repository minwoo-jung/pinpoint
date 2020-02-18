import { Injectable } from '@angular/core';
import { Store, select } from '@ngrx/store';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { AppState, STORE_KEY } from 'app/shared/store';

@Injectable()
export class UserPermissionCheckService {
    static RESTRICTED_URL = [
        `/${UrlPath.CONFIG}/${UrlPathId.USERS}`,
        `/${UrlPath.CONFIG}/${UrlPathId.ROLE}`,
        `/${UrlPath.CONFIG}/${UrlPathId.AGENT_STATISTIC}`,
        `/${UrlPath.CONFIG}/${UrlPathId.AGENT_MANAGEMENT}`,
    ];
    private userPermissions: IPermissions;

    constructor(
        private store: Store<AppState>
    ) {
        this.store.pipe(
            select(STORE_KEY.USER_PERMISSIONS),
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

    canCallAdminApi(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAdministration.callAdminApi;
    }

    canAddAuth(isManager: boolean): boolean {
        if (this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorForEverything) {
            return true;
        } else {
            if (this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager) {
                return isManager;
            }
        }
        return false;
    }

    canUpdateAndRemoveAuth(isManager: boolean): boolean {
        if (this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorForEverything) {
            return true;
        } else {
            if (this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager) {
                return isManager;
            }
        }
        return false;
    }

    canEditAllAuth(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorForEverything;
    }

    canEditMyAuth(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAppAuthorization.editAuthorOnlyManager;
    }

    canPreoccupancy(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAppAuthorization.preoccupancy;
    }

    canAddAlarm(isManager: boolean): boolean {
        if (this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmForEverything) {
            return true;
        } else {
            if (this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmOnlyManager) {
                return isManager;
            }
        }
        return false;
    }

    canUpdateAndRemoveAlarm(isManager: boolean): boolean {
        if (this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmForEverything) {
            return true;
        } else {
            if (this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmOnlyManager) {
                return isManager;
            }
        }
        return false;
    }

    canEditAllAlarm(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmForEverything;
    }

    canEditMyAlarm(): boolean {
        return this.userPermissions.permissionCollection.permsGroupAlarm.editAlarmOnlyManager;
    }

    canEditAllUserGroup(): boolean {
        return this.userPermissions.permissionCollection.permsGroupUserGroup.editGroupForEverything;
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