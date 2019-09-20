import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

import { AppDataService, Span } from 'app/service/app-data.service';
import { AppInteractionService } from 'app/service/app-interaction.service';
import { SelectType } from 'app/feature/select/select.component';

const enum ValidPeriodWith {
    ONLY_ORG = 40 * 24 * 60 * 60 * 1000, // 40d
    ORG_AND_APP = 7 * 24 * 60 * 60 * 1000, // 7d
    ALL = 24 * 60 * 60 * 1000 // 24h
}

@Component({
    selector: 'pp-main-page',
    templateUrl: './main-page.component.html',
    styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit {
    private selectedOrg = '';
    private selectedApp = '';
    private selectedAgent = '';
    private selectedPeriod: number[];

    orgList$: Observable<string[]>;
    appList$: Observable<string[]>;
    agentList$: Observable<string[]>;
    result$: Observable<Span[]>;
    selectType = SelectType;
    showSpinner = false;

    constructor(
        private appDataService: AppDataService,
        private appInteractionService: AppInteractionService,
        private snackBar: MatSnackBar
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
        if (this.selectedApp === app) {
            return;
        }

        this.selectedApp = app;
        this.appInteractionService.onAppSelect(app);
        this.agentList$ = app ? this.appDataService.getAgentList({
            organization: this.selectedOrg,
            application: app
        }) : of(null);
    }

    onSelectAgent(agent: string): void {
        if (this.selectedAgent === agent) {
            return;
        }

        this.selectedAgent = agent;
        this.appInteractionService.onAgentSelect(agent);
    }

    onPeriodSelect(period: number[]): void {
        this.selectedPeriod = period;
    }

    onSearch(): void {
        if (!this.isValidPeriod()) {
            this.snackBar.open('검색 기간을 잘못 설정하였습니다.', '닫기');
            return;
        }

        this.showSpinner = true;
        this.result$ = this.getResultObs$().pipe(
            tap(() => this.showSpinner = false)
        );
    }

    shouldDisableSearch(): boolean {
        return !(this.selectedOrg && this.selectedPeriod);
    }

    private getResultObs$(): Observable<Span[]> {
        const [from, to] = this.selectedPeriod;
        const baseParams = {organization: this.selectedOrg, from, to};

        return this.selectedAgent ? this.appDataService.getSpanCountByAll({
            ...baseParams,
            application: this.selectedApp,
            agent: this.selectedAgent,
            timeUnit: 'min'
        }) : this.selectedApp ? this.appDataService.getSpanCountByOrgAndApp({
            ...baseParams,
            application: this.selectedApp,
            timeUnit: 'hour'
        }) : this.appDataService.getSpanCountByOrg({
            ...baseParams,
            timeUnit: 'day'
        });
    }

    private isValidPeriod(): boolean {
        /**
         * Valid Period Condition
         * 1. only org: To - From <= 40d
         * 2. org * app: To - From <= 7d
         * 3. org * app * agent: To - From <= 24h
         */
        const timeDiff = this.selectedPeriod[1] - this.selectedPeriod[0];

        return timeDiff < 0 ? false
            : this.selectedAgent ? timeDiff <= ValidPeriodWith.ALL
            : this.selectedApp ? timeDiff <= ValidPeriodWith.ORG_AND_APP
            : timeDiff <= ValidPeriodWith.ONLY_ORG;
    }
}
