import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
