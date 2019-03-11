import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, UserConfigurationDataService, UserPermissionCheckService } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { Alarm, IAlarmCommandForm, NOTIFICATION_TYPE } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService, IAlarmRule, IAlarmRuleCreated, IAlarmRuleResponse } from './alarm-rule-data.service';
import { isThatType } from 'app/core/utils/util';
import { AuthenticationDataService, IPosition } from 'app/core/components/authentication-list/authentication-data.service';
import { AlarmInteractionService, CMD_TYPE } from './alarm-interaction.service';

@Component({
    selector: 'pp-alarm-rule-list-container',
    templateUrl: './alarm-rule-list-container.component.html',
    styleUrls: ['./alarm-rule-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlarmRuleListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private currentApplication: IApplication = null;
    private authInfo: IPosition;
    hasUpdateAndRemoveAlarm: boolean;
    errorMessage = '';
    useDisable = false;
    showLoading = false;
    checkerList: string[];
    alarmRuleList: IAlarmRule[];
    userGroupList: string[];

    i18nLabel = {
        CHECKER_LABEL: '',
        USER_GROUP_LABEL: '',
        THRESHOLD_LABEL: '',
        TYPE_LABEL: '',
        NOTES_LABEL: '',
    };
    i18nGuide: { [key: string]: IFormFieldErrorType };
    noPermMessage: string;
    editAlarm: any;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userConfigurationDataService: UserConfigurationDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private alarmRuleDataService: AlarmRuleDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private authenticationDataService: AuthenticationDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private alarmInteractionService: AlarmInteractionService
    ) {}
    ngOnInit() {
        this.loadAlarmRule();
        this.loadUserData();
        this.connectApplicationList();
        this.connectAuthenticationComponent();
        this.connectAlarmComponent();
        this.getI18NText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private loadAlarmRule(): void {
        this.alarmRuleDataService.getCheckerList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((result: string[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.checkerList = result as string[];
        });
    }
    private loadUserData(): void {
        this.userGroupDataSerivce.retrieve(
            this.userPermissionCheckService.canEditAllAlarm() ? {} : { userId: this.userConfigurationDataService.getUserId() }
        ).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((result: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.userGroupList = result.map((userGroup: IUserGroup) => userGroup.id);

            this.changeDetectorRef.detectChanges();
        });
        this.authenticationDataService.outRole.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((authInfo: IPosition) => {
            this.authInfo = authInfo;
            this.changeDetectorRef.detectChanges();
        });
    }
    private connectApplicationList(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((selectedApplication: IApplication) => {
                return selectedApplication !== null;
            })
        ).subscribe((selectedApplication: IApplication) => {
            this.currentApplication = selectedApplication;
            this.errorMessage = '';
            this.getAlarmData();
            this.changeDetectorRef.detectChanges();
        });
    }
    private connectAuthenticationComponent(): void {
        this.authenticationDataService.outRole.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((authInfo: IPosition) => {
            this.authInfo = authInfo;
            this.hasUpdateAndRemoveAlarm = this.userPermissionCheckService.canUpdateAndRemoveAlarm(this.authInfo.isManager);
            this.changeDetectorRef.detectChanges();
        });
    }
    private connectAlarmComponent(): void {
        this.alarmInteractionService.onComplete$.subscribe((formData: IAlarmCommandForm) => {
            switch (formData.type) {
                case CMD_TYPE.CREATE:
                    this.onCreateAlarm(formData.data as Alarm);
                    break;
                case CMD_TYPE.UPDATE:
                    this.onUpdateAlarm(formData.data as Alarm);
                    break;
                case CMD_TYPE.CLOSE:
                    this.onCloseAlarm();
                    break;
            }
        });
    }
    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('CONFIGURATION.COMMON.CHECKER'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.COMMON.THRESHOLD'),
            this.translateService.get('CONFIGURATION.COMMON.TYPE'),
            this.translateService.get('CONFIGURATION.COMMON.NOTES'),
            this.translateService.get('COMMON.DO_NOT_HAVE_PERMISSION'),
        ).subscribe(([requiredMessage, checkerLabel, userGroupLabel, thresholdLabel, typeLabel, notesLabel, noPermMessage]: string[]) => {
            this.i18nGuide = {
                checkerName: { required: this.translateReplaceService.replace(requiredMessage, checkerLabel) },
                userGroupId: { required: this.translateReplaceService.replace(requiredMessage, userGroupLabel) },
                threshold: {
                    required: this.translateReplaceService.replace(requiredMessage, thresholdLabel),
                    min: 'Must be greater than 0'
                },
                type: { required: this.translateReplaceService.replace(requiredMessage, typeLabel) }
            };

            this.i18nLabel = {
                CHECKER_LABEL: checkerLabel,
                USER_GROUP_LABEL: userGroupLabel,
                THRESHOLD_LABEL: thresholdLabel,
                TYPE_LABEL: typeLabel,
                NOTES_LABEL: notesLabel
            };
            this.noPermMessage = noPermMessage;
        });
    }
    private getAlarmData(): void {
        this.showProcessing();
        this.alarmRuleDataService.retrieve(this.currentApplication.getApplicationName())
            .subscribe((result: IAlarmRule[] | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                : this.alarmRuleList = result as IAlarmRule[];
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            }, (error: IServerErrorFormat) => {
                this.hideProcessing();
                this.errorMessage = error.exception.message;
                this.changeDetectorRef.detectChanges();
            });
    }
    private getFilteredUserGroupList(userGroupId?: string): string[] {
        if (this.userPermissionCheckService.canEditAllAlarm()) {
            return this.userGroupList;
        } else {
            if (this.authInfo.isUser) {
                if (this.userGroupList.indexOf(userGroupId) === -1) {
                    return this.userGroupList.concat(userGroupId);
                } else {
                    return this.userGroupList;
                }
            }
        }
        return this.userGroupList;
    }
    private getTypeStr(smsSend: boolean, emailSend: boolean): string {
        if (smsSend && emailSend) {
            return NOTIFICATION_TYPE.ALL;
        } else {
            if (smsSend) {
                return NOTIFICATION_TYPE.SMS;
            }
            if (emailSend) {
                return NOTIFICATION_TYPE.EMAIL;
            }
            return '';
        }
    }
    onShowCreateAlarm(): void {
        if (this.isApplicationSelected() === false || this.userPermissionCheckService.canAddAlarm(this.authInfo.isManager) === false) {
            return;
        }
        this.alarmInteractionService.showCreate({
            applicationId: this.currentApplication.getApplicationName(),
            serviceType: this.currentApplication.getServiceType(),
            userGroupList: this.getFilteredUserGroupList(),
            checkerList: this.checkerList
        });
    }
    onCreateAlarm(alarm: Alarm): void {
        this.showProcessing();
        this.alarmRuleDataService.create({
            applicationId: alarm.applicationId,
            serviceType: alarm.serviceType,
            userGroupId: alarm.userGroupId,
            checkerName: alarm.checkerName,
            threshold: alarm.threshold,
            smsSend: alarm.smsSend,
            emailSend: alarm.emailSend,
            notes: alarm.notes
        } as IAlarmRule).subscribe((response: IAlarmRuleCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onUpdateAlarm(alarm: Alarm): void {
        this.showProcessing();
        this.alarmRuleDataService.update({
            applicationId: alarm.applicationId,
            ruleId: alarm.ruleId,
            serviceType: alarm.serviceType,
            checkerName: alarm.checkerName,
            userGroupId: alarm.userGroupId,
            threshold: alarm.threshold,
            smsSend: alarm.smsSend,
            emailSend: alarm.emailSend,
            notes: alarm.notes
        } as IAlarmRule).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onCloseAlarm(): void {
    }
    onCloseMessage(): void {
        this.errorMessage = '';
    }
    onRemoveAlarm(ruleId: string): void {
        this.showProcessing();
        this.alarmRuleDataService.remove(this.currentApplication.getApplicationName(), ruleId).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onShowUpdateAlarm(ruleId: string): void {
        const alarmInfo = this.alarmRuleList.find((alarm: IAlarmRule) => {
            return alarm.ruleId === ruleId;
        });
        const alarmObj = new Alarm(
            this.currentApplication.getApplicationName(),
            this.currentApplication.getServiceType(),
            alarmInfo.checkerName,
            alarmInfo.userGroupId,
            alarmInfo.threshold,
            this.getTypeStr(alarmInfo.smsSend, alarmInfo.emailSend),
            alarmInfo.notes
        );
        alarmObj.ruleId = alarmInfo.ruleId;
        this.alarmInteractionService.showUpdate({
            applicationId: this.currentApplication.getApplicationName(),
            serviceType: this.currentApplication.getServiceType(),
            userGroupList: this.getFilteredUserGroupList(),
            checkerList: this.checkerList,
            data: alarmObj
        });
    }
    isApplicationSelected(): boolean {
        return this.currentApplication !== null;
    }
    getAddButtonClass(): object {
        return {
            'btn-blue': this.isApplicationSelected() && (this.authInfo && this.userPermissionCheckService.canAddAlarm(this.authInfo.isManager)),
            'btn-gray': !this.isApplicationSelected() || (this.authInfo && !this.userPermissionCheckService.canAddAlarm(this.authInfo.isManager))
        };
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
