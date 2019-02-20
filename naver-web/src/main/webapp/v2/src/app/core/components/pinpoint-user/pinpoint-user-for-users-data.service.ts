import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';

@Injectable()
export class PinpointUserForUsersDataService {
    // private url = 'users.pinpoint';
    private url = 'user.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    retrieve(query?: string): Observable<IUserProfile[] | IServerErrorShortFormat> {
        return this.http.get<IUserProfile[] | IServerErrorShortFormat>(this.url, this.makeRequestOptionsArgs(query)).pipe(
            retry(3)
        );
    }

    private makeRequestOptionsArgs(query?: string): object {
        return query
            ? { params: { searchKey: query } }
            : {};
    }
}
