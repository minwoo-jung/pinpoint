import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class YobiNoticeCheckService {
    private requestURL = 'https://yobi.navercorp.com/api/projects/pinpoint/posts?boardId=0a70564d-5c5f-16e7-815c-5f06e84c006f';

    constructor(private http: HttpClient) {}
    getYobiNotice(): Observable<any> {
        return this.http.get<any>(this.requestURL).pipe(
            catchError(this.handleError)
        );
    }
    private handleError(error: HttpErrorResponse) {
        return throwError({
            message: error.message,
            name: error.name,
            ok: error.ok,
            status: error.status,
            statusText: error.statusText,
            url: error.url
        });
    }
}
