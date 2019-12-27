import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IUserProfile {
    userId: string;
    name: string;
    department?: string;
    phoneNumber?: string;
    email?: string;
}

@Injectable()
export class UserProfileDataService {
    private url = 'users/user/profile.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    update(userProfile: IUserProfile): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        return this.http.put<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, userProfile);
    }
}
