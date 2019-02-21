import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';
import { IUserRole } from 'app/core/components/role-list/role-list-data.service';

export interface IUserInfo {
    profile: IUserProfile;
    account?: IUserPassword;
    role: IUserRole;
}

@Injectable()
export class ConfigurationUsersDataService {
    private url = 'users/users.pinpoint';

    constructor(
        private http: HttpClient
    ) { }

    selectUser(userId: string): Observable<IUserInfo | IServerErrorShortFormat> {
        // return this.http.get<any | IServerErrorShortFormat>(this.url, { params: { userId } });
        return of({
            profile: {
                department: 'PaaS',
                email: 'bindong.kim@navercorp.com',
                name: '김동빈',
                number: '13739',
                phoneNumber: 'd20f0a06d31607572e6856c79cc89b69',
                userId
            },
            role: {
                roleList: ['admin', 'user']
            }
        });

        // return of({
        //     errorCode: 'asd',
        //     errorMessage: 'Error!'
        // });
    }
}
