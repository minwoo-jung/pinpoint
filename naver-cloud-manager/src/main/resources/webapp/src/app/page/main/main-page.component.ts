import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { AppDataService, Span } from 'app/service/app-data.service';

@Component({
    selector: 'pp-main-page',
    templateUrl: './main-page.component.html',
    styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit {
    private selectedOrg: string;
    private selectedApp: string;
    private selectedAgent: string;

    orgList$: Observable<string[]>;
    appList$: Observable<string[]>;
    agentList$: Observable<string[]>;
    result$: Observable<Span[]>;

    constructor(
        private appDataService: AppDataService
    ) {}

    ngOnInit() {
        this.orgList$ = this.appDataService.getOrgList();
    }

    onSelectOrg(org: string): void {
        this.selectedOrg = org;
        this.appList$ = this.appDataService.getAppList({
            organization: org
        });
    }

    onSelectApp(app: string): void {
        this.selectedApp = app;
        this.agentList$ = this.appDataService.getAgentList({
            organization: this.selectedOrg,
            application: app
        });
    }

    onSelectAgent(agent: string): void {
        this.selectedAgent = agent;
    }

    onSearch(): void {
        this.result$ = this.appDataService.getSpanCount({});
    }
}
