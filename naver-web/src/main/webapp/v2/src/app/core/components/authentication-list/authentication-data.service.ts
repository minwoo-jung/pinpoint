import { Injectable, EventEmitter } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry, tap } from 'rxjs/operators';

export enum ROLE {
    USER = 'user',
    GUEST = 'guest',
    MANAGER = 'manager'
}

export interface IApplicationAuthData {
    applicationId: string;
    configuration: {
        apiMetaData: boolean;
        paramMetaData: boolean;
        serverMapData: boolean;
        sqlMetaData: boolean;
    };
    role: string;
    userGroupId: string;
}

export interface IAuthentication {
    myRole: string;
    userGroupAuthList: IApplicationAuthData[];
}
export interface IAuthenticationCreated {
    myRole: string;
    result: string;
}
export interface IAuthenticationResponse {
    result: string;
}
export interface IRole {
    applicationId: string;
    isManager: boolean;
    isGuest: boolean;
    isUser: boolean;
}
@Injectable()
export class AuthenticationDataService {
    private authenticationURL = 'application/userGroupAuth.pinpoint';
    public outRole: EventEmitter<IRole> = new EventEmitter();
    constructor(private http: HttpClient) {}
    retrieve(applicationId: string): Observable<IAuthentication> {
        return this.http.get<IAuthentication>(this.authenticationURL, this.makeRequestOptionsArgs(applicationId)).pipe(
            retry(3),
            tap((auth: IAuthentication) => {
                this.outRole.emit({
                    applicationId: applicationId,
                    isManager: auth.myRole === ROLE.MANAGER,
                    isGuest: auth.myRole === ROLE.GUEST,
                    isUser: auth.myRole === ROLE.USER
                });
            })
        );
    }
    create(params: IApplicationAuthData): Observable<IAuthenticationCreated> {
        return this.http.post<IAuthenticationCreated>(this.authenticationURL, params).pipe(
            retry(3)
        );
    }
    update(params: IApplicationAuthData): Observable<IAuthenticationCreated> {
        return this.http.put<IAuthenticationCreated>(this.authenticationURL, params).pipe(
            retry(3)
        );
    }
    remove(userGroupId: string, applicationId: string): Observable<IAuthenticationResponse> {
        return this.http.request<IAuthenticationCreated>('delete', this.authenticationURL, {
            body: { applicationId, userGroupId }
        }).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(applicationId: string): object {
        return applicationId ? {
            params: new HttpParams().set('applicationId', applicationId)
        } : {};
    }
}
