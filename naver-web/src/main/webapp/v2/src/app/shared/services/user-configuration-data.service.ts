import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, retry } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { AppState, Actions } from 'app/shared/store';
import { Application } from 'app/core/models';
@Injectable()
export class UserConfigurationDataService {
    private url = 'userConfiguration.pinpoint';
    // private url = 'users/user/permissionAndConfiguration.pinpoint';
    private defaultUserConfiguration: any = {
        favoriteApplications: [],
        userId: ''
    };
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
                editAlarmOnlyGroupMember: true
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
    getUserConfiguration(): Observable<any> {
        return this.http.get<any>(this.url).pipe(
            retry(3),
            map((res: any) => {
                const config = res || this.defaultUserConfiguration;
                this.store.dispatch(new Actions.AddFavoriteApplication(
                    config.favoriteApplications.map(({applicationName, serviceType, code}) => {
                        return new Application(applicationName, serviceType, code);
                    }))
                );
                return config;
            })
        );
    }
    // getUserConfiguration(): Observable<IUserConfiguration> {
    //     return this.http.get<IUserConfiguration>(this.url).pipe(
    //         retry(3),
    //         map((res: IUserConfiguration) => {
    //             const config = Object.assign(res, { permission: this.overrideAdminPermissionConfiguration });
    //             this.store.dispatch(new Actions.AddFavoriteApplication(
    //                 config.configuration.favoriteApplications.map(({applicationName, serviceType, code}) => {
    //                     return new Application(applicationName, serviceType, code);
    //                 }))
    //             );
    //             this.store.dispatch(new Actions.UpdatePermissions(config.permission));
    //             return config;
    //         })
    //     );
    // }
    saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[]): Observable<any> {
        return this.http.put<{ favoriteApplications: IFavoriteApplication[] }>(this.url, {
            favoriteApplications: newFavoriateApplicationList
        });
    }
}
