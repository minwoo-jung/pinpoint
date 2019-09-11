import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

export interface Span {
    x: number; // time
    y: number; // count
}

@Injectable({
    providedIn: 'root'
})
export class AppDataService {
    private orgListUrl = '';
    private appListUrl = '';
    private agentListUrl = '';
    private spanCountUrl = '';

    constructor(
        private http: HttpClient
    ) {}

    getOrgList(): Observable<string[]> {
        // return this.http.get<string[]>(this.orgListUrl);
        return of(['A', 'B', 'C', 'D']);
    }

    getAppList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.appListUrl, {params});
        return of(['E', 'F', 'G']);
    }

    getAgentList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.agentListUrl, {params});
        return of(['H', 'I', 'J', 'K']);
    }

    getSpanCount(params: {[key: string]: any}): Observable<Span[]> {
        // return this.http.get<Span[]>(this.spanCountUrl, {params});
        return of([
            {
                x : 1567562560000,
                y : 12312323
            },
            {
                x : 1567562860000,
                y : 12312500
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            },
            {
                x : 1567563160000,
                y : 12312800
            }
        ]);
    }
}
