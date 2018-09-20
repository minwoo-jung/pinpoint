import { Injectable, EventEmitter } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

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
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError),
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
        return this.http.post<IApplicationAuthData>(this.authenticationURL, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    update(params: IApplicationAuthData): Observable<IAuthenticationCreated> {
        return this.http.put<IApplicationAuthData>(this.authenticationURL, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    remove(userGroupId: string, applicationId: string): Observable<IAuthenticationResponse> {
        return this.http.request('delete', this.authenticationURL, {
            body: { applicationId, userGroupId }
        }).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    private checkError(data: any) {
        if (data.errorCode) {
            throw data.errorMessage;
        } else if (data.result !== 'SUCCESS') {
            throw data;
        }
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
    private makeRequestOptionsArgs(applicationId: string): object {
        return applicationId ? {
            params: {
                applicationId
            }
        } : {};
    }
}
