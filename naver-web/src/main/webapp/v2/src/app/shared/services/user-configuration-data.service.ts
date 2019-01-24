import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, retry } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { AppState, Actions } from 'app/shared/store';
import { Application } from 'app/core/models';
@Injectable()
export class UserConfigurationDataService {
    private url = 'users/user/permissionAndConfiguration.pinpoint';
    private overrideAdminPermissionConfiguration: IPermissions = {
        roleId: '',
        permissionCollection: {
            permsGroupAdministration: {
                viewAdminMenu: true,
                editUser: true,
                editRole: true
            },
            permsGroupAppAuthorization: {
                preoccupancy: true,
                editAuthorForEverything: true,
                editAuthorOnlyManager: true
            },
            permsGroupAlarm: {
                editAlarmForEverything: true,
                editAuthorForEverything: true
            },
            permsGroupUserGroup: {
                editGroupForEverything: true,
                editGroupOnlyGroupMember: true
            }
        }
    };
    constructor(
        private http: HttpClient,
        private store: Store<AppState>,
    ) {}
    getUserConfiguration(): Observable<IUserConfiguration> {
        return this.http.get<IUserConfiguration>(this.url).pipe(
            retry(3),
            map((res: IUserConfiguration) => {
                // const config = Object.assign(res, { permission: this.overrideAdminPermissionConfiguration });
                const config = res;
                this.store.dispatch(new Actions.AddFavoriteApplication(
                    config.configuration.favoriteApplications.map(({applicationName, serviceType, code}) => {
                        return new Application(applicationName, serviceType, code);
                    }))
                );
                this.store.dispatch(new Actions.UpdatePermissions(config.permission));
                return config;
            })
        );
    }
    saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[]): Observable<any> {
        return this.http.put<{ favoriteApplications: IFavoriteApplication[] }>(this.url, {
            favoriteApplications: newFavoriateApplicationList
        });
    }
}
