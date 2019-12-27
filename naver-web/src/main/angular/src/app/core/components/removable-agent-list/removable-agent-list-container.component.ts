import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable, forkJoin } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { RemovableAgentDataService } from './removable-agent-data.service';

enum REMOVE_TYPE {
    APP = 'APP',
    EACH = 'EACH',
    NONE = 'NONE'
}

@Component({
    selector: 'pp-removable-agent-list-container',
    templateUrl: './removable-agent-list-container.component.html',
    styleUrls: ['./removable-agent-list-container.component.css'],
})
export class RemovableAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    useDisable = false;
    showLoading = false;
    errorMessage: string;
    agentList: any[];
    selectedApplication: IApplication = null;
    removeType: REMOVE_TYPE = REMOVE_TYPE.NONE;
    removeTarget: string[];
    i18nText: {[key: string]: string} = {
        select: '',
        cancelButton: '',
        removeButton: '',
        removeAllAgents: '',
        removeAgent: ''
    };

    constructor(
        private translateService: TranslateService,
        private messageQueueService: MessageQueueService,
        private removableAgentDataService: RemovableAgentDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.connectApplicationList();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectApplicationList(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((app: IApplication) => app !== null),
        ).subscribe((selectedApplication: IApplication) => {
            this.removeType = REMOVE_TYPE.NONE;
            this.selectedApplication = selectedApplication;
            this.errorMessage = '';
            this.getRemovableAgentList();
        });
    }

    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.SELECT_YOUR_APP'),
            this.translateService.get('COMMON.REMOVE'),
            this.translateService.get('COMMON.CANCEL'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.REMOVE_APPLICATION'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.REMOVE_AGENT'),
        ).subscribe(([selectApp, removeBtnLabel, cancelBtnLabel, removeApp, removeAgent]: string[]) => {
            this.i18nText.select = selectApp;
            this.i18nText.removeButton = removeBtnLabel;
            this.i18nText.cancelButton = cancelBtnLabel;
            this.i18nText.removeApplication = removeApp;
            this.i18nText.removeAgent = removeAgent;
        });
    }

    private getRemovableAgentList(): void {
        this.showProcessing();
        this.removableAgentDataService.getAgentList(this.selectedApplication.getApplicationName()).subscribe((agentList: IAgentList) => {
            const tempAgentList: any[] = [];
            Object.keys(agentList).forEach((key: string) => {
                agentList[key].forEach((agent: IAgent) => {
                    tempAgentList.push({
                        applicationName: agent.applicationName,
                        hostName: agent.hostName,
                        agentId: agent.agentId,
                        agentVersion: agent.agentVersion,
                        startTime: agent.startTimestamp,
                        ip: agent.ip
                    });
                });
            });
            this.agentList = tempAgentList;
            this.hideProcessing();
        }, () => {
            this.hideProcessing();
        });
    }

    onRemoveSelectAgent(agentInfo: string[]): void {
        this.removeTarget = agentInfo;
        this.removeType = REMOVE_TYPE.EACH;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ONE_AGENT_REMOVE_CONFIRM_VIEW);
    }

    onRemoveApplication(): void {
        this.removeType = REMOVE_TYPE.APP;
        // this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALL_INACTIVE_AGENTS_REMOVE_CONFIRM_VIEW);
    }

    onRemoveCancel(): void {
        this.removeType = REMOVE_TYPE.NONE;
    }

    onRemoveConfirm(): void {
        this.showProcessing();
        let result: Observable<string>;
        if (this.isApplicationRemove()) {
            result = this.removableAgentDataService.removeApplication(this.selectedApplication.getApplicationName());
            // this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ALL_INACTIVE_AGENTS);
        } else {
            result = this.removableAgentDataService.removeAgentId({
                applicationName: this.removeTarget[0],
                agentId: this.removeTarget[1]
            });
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ONE_AGENT);
        }

        result.pipe(
            filter((response: string) => response === 'OK')
        ).subscribe((response: string) => {
            if (this.removeType === REMOVE_TYPE.APP) {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.APPLICATION_REMOVED,
                    param: {
                        appName: this.selectedApplication.getApplicationName(),
                        appServiceType: this.selectedApplication.getServiceType(),
                    }
                });
                this.selectedApplication = null;
                this.removeType = REMOVE_TYPE.NONE;
                this.hideProcessing();
            } else {
                this.removeType = REMOVE_TYPE.NONE;
                this.getRemovableAgentList();
            }
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
            this.hideProcessing();
        });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    isAgentRemove(): boolean {
        return this.removeType !== REMOVE_TYPE.NONE;
    }

    isApplicationRemove(): boolean {
        return this.removeType === REMOVE_TYPE.APP;
    }

    isNone(): boolean {
        return this.selectedApplication === null;
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
