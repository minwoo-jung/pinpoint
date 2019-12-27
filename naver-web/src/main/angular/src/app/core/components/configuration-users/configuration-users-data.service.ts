import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
    private url = 'users/user.pinpoint';

    constructor(
        private http: HttpClient
    ) { }

    selectUser(userId: string): Observable<IUserInfo | IServerErrorShortFormat> {
        return this.http.get<IUserInfo | IServerErrorShortFormat>(this.url, { params: { userId } });
    }
}
