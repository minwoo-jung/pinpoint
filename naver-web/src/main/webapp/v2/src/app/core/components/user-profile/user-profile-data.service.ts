import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

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
        // return this.http.put<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, userProfile);
        return of({
            result: 'success',
            userId: userProfile.userId
        });
        // return of({
        //     errorCode: 'ErrorCode',
        //     errorMessage: 'ErrorMessage!'
        // });
    }
}
