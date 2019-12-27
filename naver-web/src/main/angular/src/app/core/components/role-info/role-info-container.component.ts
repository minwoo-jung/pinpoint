import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, UserPermissionCheckService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { RoleInfoDataService } from './role-info-data.service';
import { IPermissionData } from './role-info.component';
import { isThatType } from 'app/core/utils/util';

enum CRUD_ACTION {
    UPDATE = 'Update',
    REMOVE = 'Remove',
    CREATE = 'Create',
    NONE = 'None'
}
@Component({
    selector: 'pp-role-info-container',
    templateUrl: './role-info-container.component.html',
    styleUrls: ['./role-info-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoleInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    hasRoleEditPerm = false;
    currentRoleId = '';
    createRoleId: string;
    roleInfo: IPermissions;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    dataChanged = false;
    lastChangedData: IPermissionData;
    currentAction: CRUD_ACTION = CRUD_ACTION.NONE;
    roleIdValidationGuideMsg = false;
    roleIdValidationReg = /^[\w\-]{3,24}$/;

    i18nText: {[key: string]: string} = {
        saveButton: '',
        removeButton: '',
        removeGuide: '',
        inputGuide: '',
        validationGuide: '',
        select: '',
        adminMenuTitle: '',
        viewAdminMenu: '',
        editRoleTitle: '',
        editRole: '',
        editUserTitle: '',
        editUser: '',
        callAdminApiTitle: '',
        callAdminApi: '',
        preoccupancyTitle: '',
        preoccupancy: '',
        editAuthorTitle: '',
        editAuthorForEverything: '',
        editAuthorOnlyManager: '',
        obtainAllTitle: '',
        obtainAllAuthorization: '',
        editAlarmTitle: '',
        editAlarmForEverything: '',
        editAlarmOnlyManager: '',
        editGroupTitle: '',
        editGroupForEverything: '',
        editGroupOnlyGroupMember: ''
    };
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
        private roleInfoDataService: RoleInfoDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.hasRoleEditPerm = this.userPermissionCheckService.canEditRole();
        this.getI18NText();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_SELECT_ROLE).subscribe((roleId: string) => {
            this.currentRoleId = roleId;
            this.getRoleInfo(CRUD_ACTION.UPDATE);
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_REMOVE_SELECT_ROLE).subscribe((roleId: string) => {
            this.currentRoleId = roleId;
            this.getRoleInfo(CRUD_ACTION.REMOVE);
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_CREATE_ROLE).subscribe(() => {
            this.currentAction = CRUD_ACTION.CREATE;
            this.createRoleId = '';
            this.roleInfo = this.getPermissionForm('');
            this.changeDetectorRef.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        this.translateService.get([
            'COMMON.SUBMIT',
            'COMMON.REMOVE',
            'COMMON.CANCEL',
            'CONFIGURATION.ROLE.WILL_REMOVE',
            'CONFIGURATION.ROLE.INPUT_NAME',
            'CONFIGURATION.ROLE.SELECT',
            'CONFIGURATION.ROLE.VALIDATION_GUIDE',
            'CONFIGURATION.PERMISSION'
        ]).subscribe((i18nTexts: {[key: string]: any}) => {
            this.i18nText.saveButton = i18nTexts['COMMON.SUBMIT'];
            this.i18nText.removeButton = i18nTexts['COMMON.REMOVE'];
            this.i18nText.cancelButton = i18nTexts['COMMON.CANCEL'];
            this.i18nText.select = i18nTexts['CONFIGURATION.ROLE.SELECT'];
            this.i18nText.removeGuide = i18nTexts['CONFIGURATION.ROLE.WILL_REMOVE'];
            this.i18nText.inputGuide = i18nTexts['CONFIGURATION.ROLE.INPUT_NAME'];
            this.i18nText.validationGuide = i18nTexts['CONFIGURATION.ROLE.VALIDATION_GUIDE'];

            const permissionTexts = i18nTexts['CONFIGURATION.PERMISSION'];
            Object.keys(permissionTexts).map((key: string) => {
                const newKey = key.toLowerCase().replace(/\_(\D)/ig, function(m, s) {
                    return s.toUpperCase();
                });
                this.i18nText[newKey] = permissionTexts[key];
            });
        });
    }
    private getRoleInfo(nextAction: CRUD_ACTION): void {
        this.showProcessing();
        this.lastChangedData = null;
        this.dataChanged = false;
        this.changeDetectorRef.detectChanges();
        this.roleInfoDataService.get(this.currentRoleId).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((roleInfo: IPermissions) => {
            this.hideProcessing();
            this.roleInfo = roleInfo;
            this.currentAction = nextAction;
            this.changeDetectorRef.detectChanges();
        }, () => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    checkUpdateBtnDisable(): boolean {
        return !(this.hasRoleEditPerm === true && this.isSelectedRole() && this.dataChanged);
    }
    checkCreateBtnDisable(): boolean {
        return !(this.hasRoleEditPerm === true && this.createRoleId !== '');
    }
    isRemove(): boolean {
        return this.currentAction === CRUD_ACTION.REMOVE;
    }
    isUpdate(): boolean {
        return this.currentAction === CRUD_ACTION.UPDATE;
    }
    isCreate(): boolean {
        return this.currentAction === CRUD_ACTION.CREATE;
    }
    isNone(): boolean {
        return this.currentAction === CRUD_ACTION.NONE;
    }
    isSelectedRole(): boolean {
        return this.currentRoleId !== '';
    }
    onChangedPermission(permission: IPermissionData): void {
        this.dataChanged = permission.isChanged;
        this.lastChangedData = permission;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_PERMISSION);
    }
    onCreate(): void {
        if (this.roleIdValidationReg.test(this.createRoleId) === false) {
            this.roleIdValidationGuideMsg = true;
            return;
        }
        this.roleIdValidationGuideMsg = false;
        this.showProcessing();
        this.roleInfoDataService.create(this.getPermissionForm(this.createRoleId, this.lastChangedData)).subscribe((response: IUserRequestSuccessResponse | any) => {
            if (isThatType<any>(response, 'exception')) {
                this.errorMessage = (response as any).exception.message;
            } else {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.ROLE_INFO_CREATED,
                    param: this.createRoleId
                });
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_ROLE);
            }
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    onUpdate(): void {
        this.showProcessing();
        this.roleInfoDataService.update(this.getPermissionForm(this.roleInfo.roleId, this.lastChangedData)).subscribe((response: IUserRequestSuccessResponse | any) => {
            if (isThatType<any>(response, 'exception')) {
                this.errorMessage = (response as any).exception.message;
            } else {
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_ROLE);
            }
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    private getPermissionForm(roleId: string, data?: IPermissionData): IPermissions {
        return {
            roleId: roleId,
            permissionCollection: {
                permsGroupAdministration: {
                    viewAdminMenu: data ? data.viewAdminMenu : false,
                    editUser: data ? data.editUser : false,
                    editRole: data ? data.editRole : false,
                    callAdminApi: data ? data.callAdminApi : false
                },
                permsGroupAppAuthorization: {
                    preoccupancy: data ? data.preoccupancy : false,
                    editAuthorForEverything: data ? data.editAuthorForEverything : false,
                    editAuthorOnlyManager: data ? data.editAuthorOnlyManager : false,
                    obtainAllAuthorization: data ? data.obtainAllAuthorization : false
                },
                permsGroupAlarm: {
                    editAlarmForEverything: data ? data.editAlarmForEverything : false,
                    editAlarmOnlyManager: data ? data.editAlarmOnlyManager : false
                },
                permsGroupUserGroup: {
                    editGroupForEverything: data ? data.editGroupForEverything : false,
                    editGroupOnlyGroupMember: data ? data.editGroupOnlyGroupMember : false
                }
            }
        };
    }
    onRemoveCancel(): void {
        this.currentAction = CRUD_ACTION.NONE;
        this.changeDetectorRef.detectChanges();
    }
    onRemoveConfirm(): void {
        this.showProcessing();
        this.roleInfoDataService.remove(this.roleInfo.roleId).subscribe((response: IUserRequestSuccessResponse | any) => {
            if (isThatType<any>(response, 'exception')) {
                this.errorMessage = (response as any).exception.message;
                this.hideProcessing();
            } else {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.ROLE_INFO_REMOVED,
                });
                this.currentAction = CRUD_ACTION.NONE;
                this.currentRoleId = '';
                this.roleInfo = null;
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ROLE);
                this.hideProcessing();
            }
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
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
