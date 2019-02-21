import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable()
export class ConfirmRemoveUserDataService {
    private url = 'users/user.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    removeUser(userId: string): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        // return this.http.delete<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.url, { params: { userId } });
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
