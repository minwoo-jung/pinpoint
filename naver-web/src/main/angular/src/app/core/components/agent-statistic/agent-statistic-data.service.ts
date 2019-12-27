import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable()
export class AgentStatisticDataService {
    private url = 'getAgentList.pinpoint';
    private cache: IAgentList = null;
    private lastCacheTime: number;
    constructor(private http: HttpClient) {}
    get(force: boolean = false): Observable<IAgentList> {
        if (force === true || this.hasData() === false) {
            return this.http.get<IAgentList>(this.url).pipe(
                tap((data: any) => {
                    if (data.errorCode) {
                        throw data.errorMessage;
                    }
                    this.cache = data;
                    this.lastCacheTime = new Date().getTime();
                }),
                catchError(this.handleError)
            );
        } else {
            return of(this.cache);
        }
    }
    getLastRequestTime(): number {
        return this.lastCacheTime;
    }
    hasData(): boolean {
        return this.cache !== null;
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
}
