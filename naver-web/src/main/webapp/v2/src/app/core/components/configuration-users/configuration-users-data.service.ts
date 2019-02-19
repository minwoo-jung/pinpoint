import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable()
export class ConfigurationUsersDataService {
    private url = 'users/users.pinpoint';

    constructor(
        private http: HttpClient
    ) { }

    selectUser(userId: string): Observable<any | IServerErrorShortFormat> {
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
                roleList: ['Admin', 'User']
            }
        });

        // return of({
        //     errorCode: 'asd',
        //     errorMessage: 'Error!'
        // });
    }
}
