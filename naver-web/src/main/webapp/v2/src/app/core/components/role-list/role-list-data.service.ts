import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

export interface IUserRole {
    roleList: string[];
}

@Injectable()
export class RoleListDataService {
    private getRoleListUrl = 'roles.pinpoint';
    private updateRoleListUrl = 'users/user/role.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getRoleList(): Observable<string[]> {
        return this.http.get<string[]>(this.getRoleListUrl).pipe(
            retry(3),
        );
    }

    update(userId: string, roleList: string[]): Observable<IUserRequestSuccessResponse | IServerErrorShortFormat> {
        return this.http.put<IUserRequestSuccessResponse | IServerErrorShortFormat>(this.updateRoleListUrl, {
            userId,
            roleList
        });
    }
}
