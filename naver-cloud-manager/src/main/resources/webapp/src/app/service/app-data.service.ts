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
        return of(['A', 'B', 'C', 'D', 'D', 'D', 'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D',
         'D', 'D', 'D', 'D', 'D']);
    }

    getAppList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.appListUrl, {params});
        return of(['E', 'F', 'G']);
    }

    getAgentList(params: {[key: string]: string}): Observable<string[]> {
        // return this.http.get<string[]>(this.agentListUrl, {params});
        return of(['H', 'I', 'J', 'K']);
    }

    getSpanCountByOrg(params: {[key: string]: any}): Observable<Span[]> {
        console.log(params);
        console.log('By ORG');
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
        return this.http.get<Span[]>(this.spanCountByOrgUrl, {params});
    }

    getSpanCountByOrgAndApp(params: {[key: string]: any}): Observable<Span[]> {
        console.log(params);
        console.log('By ORG And APP');
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
        return this.http.get<Span[]>(this.spanCountByOrgAndAppUrl, {params});
    }

    getSpanCountByAll(params: {[key: string]: any}): Observable<Span[]> {
        console.log(params);
        console.log('By ALL');
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
        return this.http.get<Span[]>(this.spanCountByAllUrl, {params});
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
