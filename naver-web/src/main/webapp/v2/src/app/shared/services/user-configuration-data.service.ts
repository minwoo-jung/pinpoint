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
    private cudUrl = 'userConfiguration.pinpoint';
    private userId: string;
    constructor(
        private http: HttpClient,
        private store: Store<AppState>,
    ) {}
    getUserConfiguration(): Observable<IUserConfiguration> {
        return this.http.get<IUserConfiguration>(this.url).pipe(
            retry(3),
            map((res: IUserConfiguration) => {
                this.userId = res.configuration.userId;
                this.store.dispatch(new Actions.AddFavoriteApplication(
                    res.configuration.favoriteApplications.map(({applicationName, serviceType, code}) => {
                        return new Application(applicationName, serviceType, code);
                    }))
                );
                this.store.dispatch(new Actions.UpdatePermissions(res.permission));
                return res;
            })
        );
    }
    getFavoriteList(): Observable<any> {
        return this.http.get<{favoriteApplications: IFavoriteApplication[]}>(this.cudUrl);
    }
    saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[]): Observable<any> {
        return this.http.put<{ favoriteApplications: IFavoriteApplication[] }>(this.cudUrl, {
            favoriteApplications: newFavoriateApplicationList
        });
    }
    getUserId(): string {
        return this.userId;
    }
}
