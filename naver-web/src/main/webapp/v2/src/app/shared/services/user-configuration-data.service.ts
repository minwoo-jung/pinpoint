import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class UserConfigurationDataService {
    private url = 'userConfiguration.pinpoint';
    private defaultUserConfiguration: IUserConfiguration = {
        favoriteApplications: [],
        userId: ''
    };
    constructor(private http: HttpClient) {}
    getUserConfiguration(): Observable<IUserConfiguration> {
        return this.http.get<IUserConfiguration>(this.url).pipe(
            map(res => {
                if (res) {
                    return res;
                } else {
                    return this.defaultUserConfiguration;
                }
            })
        );
    }
}
