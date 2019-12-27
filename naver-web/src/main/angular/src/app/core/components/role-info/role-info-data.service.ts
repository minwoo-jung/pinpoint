import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable()
export class RoleInfoDataService {
    private roleInfoURL = 'roles/role.pinpoint';
    constructor(private http: HttpClient) {}
    get(role: string): Observable<IPermissions> {
        return this.http.get<IPermissions>(this.roleInfoURL, this.makeRequestOptionsArgs(role)).pipe(
            retry(3)
        );
    }
    create(params: IPermissions): Observable<IUserRequestSuccessResponse> {
        return this.http.post<any>(this.roleInfoURL, params).pipe(
            retry(3)
        );
    }
    update(params: IPermissions): Observable<IUserRequestSuccessResponse> {
        return this.http.put<any>(this.roleInfoURL, params).pipe(
            retry(3)
        );
    }
    remove(roleId: string): Observable<IUserRequestSuccessResponse> {
        return this.http.request<IUserRequestSuccessResponse>('delete', this.roleInfoURL, {
            body: { roleId }
        }).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(role: string): object {
        return {
            params: new HttpParams().set('roleId', role)
        };
    }
}
