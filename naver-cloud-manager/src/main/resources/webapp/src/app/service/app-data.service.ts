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
    private orgListUrl = 'organizationList.pinpoint';
    private appListUrl = 'applicationList.pinpoint';
    private agentListUrl = 'agentList.pinpoint';
    private spanCountByOrgUrl = 'spanCountStat/organization.pinpoint';
    private spanCountByOrgAndAppUrl = 'spanCountStat/organization/application.pinpoint';
    private spanCountByAllUrl = 'spanCountStat/organization/application/agent.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getOrgList(): Observable<string[]> {
        // return this.http.get<string[]>(this.orgListUrl);
        return of(Array(26).fill(1).map((_, i: number) => String.fromCharCode(65 + i)));
    }

    getAppList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.appListUrl, {params});
        return of(['App1', 'App2', 'App3']);
    }

    getAgentList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.agentListUrl, {params});
        return of(['Agent1', 'Agent2', 'Agent3', 'Agent4']);
    }

    getSpanCountByOrg(params: {[key: string]: any}): Observable<Span[]> {
        return of(
            Array(20).fill({x: 1567562260000, y: 10}).map(({x, y}: Span, i: number) =>  ({x: x + 60000 * i, y: y * (i + 1)}))
        );
        return this.http.get<Span[]>(this.spanCountByOrgUrl, {params});
    }

    getSpanCountByOrgAndApp(params: {[key: string]: any}): Observable<Span[]> {
        return of(
            Array(20).fill({x: 1567562560000, y: 30}).map(({x, y}: Span, i: number) =>  ({x: x + 60000 * i, y: y * (i + 1)}))
        );
        return this.http.get<Span[]>(this.spanCountByOrgAndAppUrl, {params});
    }

    getSpanCountByAll(params: {[key: string]: any}): Observable<Span[]> {
        return of(
            Array(20).fill({x: 1567562860000, y: 50}).map(({x, y}: Span, i: number) =>  ({x: x + 60000 * i, y: y * (i + 1)}))
        );
        return this.http.get<Span[]>(this.spanCountByAllUrl, {params});
    }

}
