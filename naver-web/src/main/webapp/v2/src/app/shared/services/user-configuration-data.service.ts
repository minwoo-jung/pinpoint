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
    private defaultUserConfiguration: IUserConfiguration = {
        favoriteApplications: [],
        userId: '',
        permissions: {
            roleId: '',
            permissionCollection: {
                permsGroupAministration: {
                    viewAdminMenu: false,
                    editUser: false,
                    editRole: false
                },
                permsGroupAppAuthorization: {
                    preoccupancy: false,
                    editAuthorForEverything: false,
                    editAuthorOnlyManager: false
                },
                permsGroupAlarm: {
                    editAlarmForEverything: false,
                    editAuthorForEverything: false
                },
                permsGroupUserGroup: {
                    editGroupForEverything: false,
                    editGroupOnlyGroupMember: false
                }
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
                const config = res || this.defaultUserConfiguration;
                this.store.dispatch(new Actions.AddFavoriteApplication(
                    config.favoriteApplications.map(({applicationName, serviceType, code}) => {
                        return new Application(applicationName, serviceType, code);
                    }))
                );
                this.store.dispatch(new Actions.UpdatePermissions(config.permissions || this.defaultUserConfiguration.permissions));
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
