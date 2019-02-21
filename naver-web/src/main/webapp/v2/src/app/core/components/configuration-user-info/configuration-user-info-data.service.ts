import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { IUserInfo } from 'app/core/components/configuration-users/configuration-users-data.service';

@Injectable()
export class ConfigurationUserInfoDataService {
    private url = 'users/user.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    insertUser(user: IUserInfo): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        console.log(user);
        // return this.http.post<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, user);
        return of({
            result: 'success',
            userId: user.profile.userId
        });
        // return of({
        //     errorCode: 'ErrorCode',
        //     errorMessage: 'ErrorMessage!'
        // });
    }
}
