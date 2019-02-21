import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

export interface IUserPassword {
    currentPassword?: string;
    // newPassword: string;
    password: string;
}

@Injectable()
export class UserPasswordDataService {
    private url = 'users/user/account.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    update(userId: string, { currentPassword = null, password }: IUserPassword): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        // return this.http.put<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, {
        //     params: {
        //         userId,
        //         currentPassword,
        //         newPassword: password
        //     }
        // });
        return of({
            result: 'success',
            userId
        });
        // return of({
        //     errorCode: 'ErrorCode',
        //     errorMessage: 'ErrorMessage!'
        // });
    }
}
