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
    private orgListUrl = 'organizations.pinpoint';
    private appListUrl = 'repositories/{organizationName}/applications.pinpoint';
    private agentListUrl = 'repositories/{organizationName}/applications/{applicationName}/agentIds.pinpoint';
    private spanCountByOrgUrl = 'spanCountStat/organization.pinpoint';
    private spanCountByOrgAndAppUrl = 'spanCountStat/organization/application.pinpoint';
    private spanCountByAllUrl = 'spanCountStat/organization/application/agent.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getOrgList(): Observable<string[]> {
        return this.http.get<string[]>(this.orgListUrl);
    }

    getAppList(params: {[key: string]: string}): Observable<string[]> {
        return this.http.get<string[]>(this.parseUrl(this.appListUrl, params));
    }

    getAgentList(params: {[key: string]: string}): Observable<string[]> {
        return this.http.get<string[]>(this.parseUrl(this.agentListUrl, params));
    }

    getSpanCountByOrg(params: {[key: string]: any}): Observable<Span[]> {
        return this.http.get<Span[]>(this.spanCountByOrgUrl, {params});
    }

    getSpanCountByOrgAndApp(params: {[key: string]: any}): Observable<Span[]> {
        return this.http.get<Span[]>(this.spanCountByOrgAndAppUrl, {params});
    }

    getSpanCountByAll(params: {[key: string]: any}): Observable<Span[]> {
        return this.http.get<Span[]>(this.spanCountByAllUrl, {params});
    }

    private parseUrl(url: string, params: {[key: string]: string}): string {
        return Object.keys(params).reduce((acc: string, curr: string) => {
            return acc.replace(new RegExp(`\\{${curr}\\}`), params[curr]);
        }, url);
    }
}
