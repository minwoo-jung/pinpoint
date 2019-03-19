import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { UrlPath } from 'app/shared/models';
import { StoreHelperService, UrlRouteManagerService } from 'app/shared/services';
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
    showLoading = true;
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
    constructor(
        private storeHelperService: StoreHelperService,
        private agentStatisticDataService: AgentStatisticDataService,
        private urlRouteManagerService: UrlRouteManagerService
    ) {}

    ngOnInit() {
        this.agentStatisticDataService.get().subscribe((agentList: { [key: string]: IAgent[] }) => {
        //     this.storeHelperService.dispatch(new Actions.UpdateAdminAgentList(agentList));
            this.extractChartData(agentList);
            this.makeGridData(agentList);
            this.showLoading = false;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
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
    }
}
