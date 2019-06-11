import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import * as moment from 'moment-timezone';
import { TranslateService } from '@ngx-translate/core';

import { UrlPath } from 'app/shared/models';
import { StoreHelperService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { AgentStatisticDataService } from './agent-statistic-data.service';

interface IGridData {
    index: number;
    application: string;
    serviceType: string;
    agent: string;
    agentVersion: string;
    jvmVersion: string;
    folder?: boolean;
    open?: boolean;
    children?: IGridData[];
}

@Component({
    selector: 'pp-agent-statistic-container',
    templateUrl: './agent-statistic-container.component.html',
    styleUrls: ['./agent-statistic-container.component.css']
})
export class AgentStatisticContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    timezone: string;
    dateFormat: string;
    loadingData = false;
    useDisable = false;
    showLoading = false;
    agentCount = 0;
    agentListData: any;
    chartData: {
        jvmVersion: {
            [key: string]: number
        },
        agentVersion: {
            [key: string]: number
        }
    } = {
        jvmVersion: {},
        agentVersion: {}
    };
    i18nText: {[key: string]: string} = {
        loadGuide: '',
        loadButton: '',
        reloadButton: ''
    };
    constructor(
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
        private agentStatisticDataService: AgentStatisticDataService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.connectStore();
        if (this.agentStatisticDataService.hasData()) {
            this.onLoadStart();
        }
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        this.translateService.get([
            'CONFIGURATION.AGENT_STATISTIC.LOAD_GUIDE',
            'CONFIGURATION.AGENT_STATISTIC.LOADING',
            'CONFIGURATION.AGENT_STATISTIC.RELOAD'
        ]).subscribe((i18nText: {[key: string]: string}) => {
            this.i18nText.loadGuide = i18nText['CONFIGURATION.AGENT_STATISTIC.LOAD_GUIDE'];
            this.i18nText.loadButton = i18nText['CONFIGURATION.AGENT_STATISTIC.LOADING'];
            this.i18nText.reloadButton = i18nText['CONFIGURATION.AGENT_STATISTIC.RELOAD'];
        });
    }
    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });
        this.storeHelperService.getDateFormat(this.unsubscribe, 0).subscribe((dateFormat: string) => {
            this.dateFormat = dateFormat;
        });
    }
    private extractChartData(agentList: IAgentList): void {
        this.chartData = {
            jvmVersion: {},
            agentVersion: {}
        };
        let count = 0;
        Object.keys(agentList).forEach((key: string) => {
            const agents = agentList[key];
            agents.forEach((agent: IAgent) => {
                count++;
                if (agent.agentVersion) {
                    this.chartData.agentVersion[agent.agentVersion] = (this.chartData.agentVersion[agent.agentVersion] || 0) + 1;
                }
                if (agent.jvmInfo && agent.jvmInfo.jvmVersion) {
                    this.chartData.jvmVersion[agent.jvmInfo.jvmVersion] = (this.chartData.jvmVersion[agent.jvmInfo.jvmVersion] || 0) + 1;
                } else {
                    this.chartData.jvmVersion['UNKNOWN'] = (this.chartData.jvmVersion['UNKNOWN'] || 0) + 1;
                }
            });
        });
        this.agentCount = count;
    }
    private makeGridData(agentList: IAgentList): void {
        let index = 1;
        const resultData: IGridData[] = [];
        Object.keys(agentList).forEach((key: string, innerIndex: number) => {
            const list: IAgent[] = agentList[key];
            let row: IGridData;

            if (list.length === 0) {
                row = this.makeRow(list[0], index, false, false);
                index++;
            } else {
                list.forEach((agent: IAgent, agentIndex: number) => {
                    if (agentIndex === 0) {
                        row = this.makeRow(agent, index, true, false);
                    } else {
                        row.children.push(this.makeRow(agent, index, false, true));
                    }
                    index++;
                });
            }
            resultData.push(row);
        });
        this.agentCount = index - 1;
        this.agentListData = resultData;
    }
    private makeRow(agent: IAgent, index: number, hasChild: boolean, isChild: boolean): any {
        const oRow: IGridData = {
            index: index,
            application: isChild ? '' : agent.applicationName,
            serviceType: agent.serviceType,
            agent: agent.agentId,
            agentVersion: agent.agentVersion,
            jvmVersion: agent.jvmInfo ? agent.jvmInfo.jvmVersion : ''
        };
        if (hasChild) {
            oRow.folder = true;
            oRow.open = true;
            oRow.children = [];
        }

        return oRow;
    }
    onCellClick(params: any): void {
        this.urlRouteManagerService.openPage([
            UrlPath.MAIN,
            params.application + '@' + params.serviceType
        ]);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_APPLICATION_IN_STATISTIC_LIST);
    }
    onReload(): void {
        this.onLoadStart(true);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_AGENT_STATISTIC_DATA);
    }
    onLoadStart(force = false): void {
        if (!force) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.FETCH_AGENT_STATISTIC_DATA);
        }

        this.loadingData = true;
        this.showProcessing();
        this.agentStatisticDataService.get(force).subscribe((agentList: IAgentList) => {
        //     this.storeHelperService.dispatch(new Actions.UpdateAdminAgentList(agentList));
            this.extractChartData(agentList);
            this.makeGridData(agentList);
            this.loadingData = false;
            this.hideProcessing();
        }, (error: any) => {
            this.hideProcessing();
        });
    }
    getLastRequestTime(): string {
        return moment(this.agentStatisticDataService.getLastRequestTime()).tz(this.timezone).format(this.dateFormat);
    }
    hasData(): boolean {
        return this.agentStatisticDataService.hasData();
    }
    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }
    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
