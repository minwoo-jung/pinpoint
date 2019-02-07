import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable()
export class ApplicationAuthAndAlarmDataService {
    private url = 'roles/role.pinpoint';
    constructor(private http: HttpClient) {}
    getInfo(userGroupId: string): Observable<any> {
        // return this.http.get<IPermissions>(this.roleInfoURL, this.makeRequestOptionsArgs(userGroupId)).pipe(
        //     retry(3)
        // );
        return of({
        });
    }
    private makeRequestOptionsArgs(userGroupId: string): object {
        return {
            params: new HttpParams().set('userGroupId', userGroupId)
        };
    }
}
