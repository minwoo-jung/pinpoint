import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable()
export class RoleInfoDataService {
    private roleInfoURL = 'roles/role.pinpoint';
    constructor(private http: HttpClient) {}
    getRoleInfo(role: string): Observable<IPermissions> {
        // return this.http.get<IPermissions>(this.roleInfoURL, this.makeRequestOptionsArgs(role)).pipe(
        //     retry(3)
        // );
        return of({
            roleId : role,
            permissionCollection: {
                permsGroupAdministration: {
                    viewAdminMenu: true,
                    editUser: true,
                    editRole: true
                },
                permsGroupAppAuthorization: {
                    preoccupancy: true,
                    editAuthorForEverything: true,
                    editAuthorOnlyManager: false
                },
                permsGroupAlarm: {
                    editAlarmForEverything: true,
                    editAuthorForEverything: false
                },
                permsGroupUserGroup: {
                    editGroupForEverything: true,
                    editGroupOnlyGroupMember: false
                }
            }
        });
    }
    private makeRequestOptionsArgs(role: string): object {
        return {
            params: new HttpParams().set('roleId', role)
        };
    }
}
