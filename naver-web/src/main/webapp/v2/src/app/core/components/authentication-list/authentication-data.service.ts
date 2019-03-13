import { Injectable, EventEmitter } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry, tap } from 'rxjs/operators';

export enum POSITION {
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
    role?: string;
    position?: string;
    userGroupId: string;
}

export interface IAuthentication {
    myPosition: string;
    userGroupAuthList: IApplicationAuthData[];
}
export interface IAuthenticationCreated {
    myPosition: string;
    result: string;
}
export interface IAuthenticationResponse {
    result: string;
}
export interface IPosition {
    applicationId: string;
    isManager: boolean;
    isGuest: boolean;
    isUser: boolean;
}
@Injectable()
export class AuthenticationDataService {
    private authenticationURL = 'application/userGroupAuth.pinpoint';
    public outPosition: EventEmitter<IPosition> = new EventEmitter();
    constructor(private http: HttpClient) {}
    retrieve(applicationId: string): Observable<IAuthentication> {
        return this.http.get<IAuthentication>(this.authenticationURL, this.makeRequestOptionsArgs(applicationId)).pipe(
            retry(3),
            tap((auth: IAuthentication) => {
                this.outPosition.emit({
                    applicationId: applicationId,
                    isManager: auth.myPosition === POSITION.MANAGER,
                    isGuest: auth.myPosition === POSITION.GUEST,
                    isUser: auth.myPosition === POSITION.USER
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
