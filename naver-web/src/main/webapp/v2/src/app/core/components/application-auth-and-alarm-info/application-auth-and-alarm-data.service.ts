import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable()
export class ApplicationAuthAndAlarmDataService {
    private url = 'userGroup/applicationAuth.pinpoint';
    constructor(private http: HttpClient) {}
    getData(userGroupId: string): Observable<IApplicationAuthInfo[]> {
        return this.http.get<IApplicationAuthInfo[]>(this.url, this.makeRequestOptionsArgs(userGroupId)).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(userGroupId: string): object {
        return {
            params: new HttpParams().set('userGroupId', userGroupId)
        };
    }
}
