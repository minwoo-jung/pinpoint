import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class ConfirmRemoveUserDataService {
    private url = 'users/user.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    remove(userId: string): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        return this.http.request<IUserRequestSuccessResponse | IServerErrorShortFormat>('delete', this.url, {
            body: { userId }
        });
    }
}
