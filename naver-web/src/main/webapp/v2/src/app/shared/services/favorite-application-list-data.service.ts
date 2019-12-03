import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class FavoriteApplicationListDataService {
    private url = 'userConfiguration/favoriteApplications.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getFavoriteList(): Observable<any> {
        return this.http.get<{favoriteApplications: IFavoriteApplication[]}>(this.url);
    }

    saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[]): Observable<any> {
        return this.http.put<{favoriteApplications: IFavoriteApplication[]}>(this.url, {
            favoriteApplications: newFavoriateApplicationList
        });
    }
}
