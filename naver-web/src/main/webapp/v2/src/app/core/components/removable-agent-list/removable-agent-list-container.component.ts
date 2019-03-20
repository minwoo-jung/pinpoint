import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { RemovableAgentDataService } from './removable-agent-data.service';

enum REMOVE_TYPE {
    ALL = 'ALL',
    EACH = 'EACH',
    NONE = 'NONE'
}

@Component({
    selector: 'pp-removable-agent-list-container',
    templateUrl: './removable-agent-list-container.component.html',
    styleUrls: ['./removable-agent-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RemovableAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    useDisable = false;
    showLoading = false;
    errorMessage: string;
    agentList: any[];
    currentApplication: IApplication = null;
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
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private removableAgentDataService: RemovableAgentDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService
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
            filter((selectedApplication: IApplication) => {
                return selectedApplication !== null;
            })
        ).subscribe((selectedApplication: IApplication) => {
            this.removeType = REMOVE_TYPE.NONE;
            this.currentApplication = selectedApplication;
            this.initStatus();
            this.getRemovableAgentList();
            this.changeDetectorRef.detectChanges();
        });
    }
    private getI18NText(): void {
        this.translateService.get([
            'COMMON.REQUIRED_SELECT',
            'COMMON.REMOVE',
            'COMMON.CANCEL',
            'CONFIGURATION.AGENT_MANAGEMENT.REMOVE_INACTIVE_AGENTS',
            'CONFIGURATION.AGENT_MANAGEMENT.REMOVE_AGENT',
        ]).subscribe((i18nText: {[key: string]: string}) => {
            this.i18nText.select = this.translateReplaceService.replace(i18nText['COMMON.REQUIRED_SELECT'], 'Agent');
            this.i18nText.removeButton = i18nText['COMMON.REMOVE'];
            this.i18nText.cancelButton = i18nText['COMMON.CANCEL'];
            this.i18nText.removeAllAgents = i18nText['CONFIGURATION.AGENT_MANAGEMENT.REMOVE_INACTIVE_AGENTS'];
            this.i18nText.removeAgent = i18nText['CONFIGURATION.AGENT_MANAGEMENT.REMOVE_AGENT'];
        });
    }
    private initStatus(): void {
        this.errorMessage = '';
    }
    private getRemovableAgentList(): void {
        this.showProcessing();
        this.removableAgentDataService.getAgentList(this.currentApplication.getApplicationName()).subscribe((agentList: IAgentList) => {
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
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    onRemoveSelectAgent(agentInfo: string[]): void {
        this.removeTarget = agentInfo;
        this.removeType = REMOVE_TYPE.EACH;
        this.changeDetectorRef.detectChanges();
    }
    onRemoveAllInactiveAgents(): void {
        this.removeType = REMOVE_TYPE.ALL;
        this.changeDetectorRef.detectChanges();
    }
    onRemoveCancel(): void {
        this.removeType = REMOVE_TYPE.NONE;
    }
    onRemoveConfirm(): void {
        this.showProcessing();
        let result: Observable<string>;
        if (this.isAllRemove()) {
            result = this.removableAgentDataService.removeInactiveAgents();
        } else {
            result = this.removableAgentDataService.removeAgentId({
                applicationName: this.removeTarget[0],
                agentId: this.removeTarget[1]
            });
        }
        result.subscribe((response: string) => {
            if (response === 'OK') {
                this.currentApplication = null;
                this.removeType = REMOVE_TYPE.NONE;
                this.getRemovableAgentList();
            } else {
                this.hideProcessing();
            }
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
            this.hideProcessing();
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    isRemove(): boolean {
        return this.removeType !== REMOVE_TYPE.NONE;
    }
    isAllRemove(): boolean {
        return this.removeType === REMOVE_TYPE.ALL;
    }
    isNone(): boolean {
        return this.currentApplication === null;
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
