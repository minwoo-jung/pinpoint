import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IUserPassword {
    currentPassword?: string;
    password: string;
}

@Injectable()
export class UserPasswordDataService {
    private url = 'users/user/account.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    update(userId: string, { currentPassword = null, password }: IUserPassword): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        return this.http.put<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, {
            userId,
            currentPassword,
            newPassword: password
        });
    }
}
