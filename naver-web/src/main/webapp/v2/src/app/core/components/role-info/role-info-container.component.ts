import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, UserPermissionCheckService } from 'app/shared/services';
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
    roleId: string;
    roleInfo: IPermissions;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    saveButtonText: string;
    removeButtonText: string;
    createButtonText: string;
    selectText: string;
    dataChanged = false;
    lastChangedData: IPermissionData;
    currentAction: CRUD_ACTION = CRUD_ACTION.NONE;

    i18nText: {[key: string]: string} = {
        adminMenuTitle: '',
        viewAdminMenu: '',
        editRoleTitle: '',
        editRole: '',
        editUserTitle: '',
        editUser: '',
        preoccupancyTitle: '',
        preoccupancy: '',
        editAuthorTitle: '',
        editAuthorForEverything: '',
        editAuthorOnlyManager: '',
        editAlarmTitle: '',
        editAlarmForEverything: '',
        editAlarmOnlyGroupMember: '',
        editGroupTitle: '',
        editGroupForEverything: '',
        editGroupOnlyGroupMember: ''
    };
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
        private roleInfoDataService: RoleInfoDataService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}
    ngOnInit() {
        this.hasRoleEditPerm = this.userPermissionCheckService.canEditRole();
        this.getI18NText();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_SELECT_ROLE).subscribe((param: string[]) => {
            this.currentAction = CRUD_ACTION.UPDATE;
            this.currentRoleId = param[0];
            this.getRoleInfo();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_REMOVE_SELECT_ROLE).subscribe((param: string[]) => {
            this.currentAction = CRUD_ACTION.REMOVE;
            this.currentRoleId = param[0];
            this.getRoleInfo();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_CREATE_ROLE).subscribe((param: string[]) => {
            this.currentAction = CRUD_ACTION.CREATE;
            this.currentRoleId = 'new';
            this.initRoleInfo();
            this.changeDetectorRef.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        this.translateService.get(['COMMON.SUBMIT', 'COMMON.REMOVE', 'CONFIGURATION.ROLE.SELECT', 'CONFIGURATION.PERMISSION']).subscribe((texts: {[key: string]: any}) => {
            this.saveButtonText = texts['COMMON.SUBMIT'];
            this.removeButtonText = texts['COMMON.REMOVE'];
            this.selectText = texts['CONFIGURATION.ROLE.SELECT'];

            const permissionTexts = texts['CONFIGURATION.PERMISSION'];
            Object.keys(permissionTexts).map((key: string) => {
                const newKey = key.toLowerCase().replace(/\_(\D)/ig, function(m, s) {
                    return s.toUpperCase();
                });
                this.i18nText[newKey] = permissionTexts[key];
            });
        });
    }
    private getRoleInfo(): void {
        this.lastChangedData = null;
        this.dataChanged = false;
        this.showProcessing();
        this.roleInfoDataService.get(this.currentRoleId).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((roleInfo: IPermissions) => {
            this.hideProcessing();
            this.roleInfo = roleInfo;
            this.changeDetectorRef.detectChanges();
        }, () => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    private initRoleInfo(): void {
        this.roleInfo = {
            roleId: '',
            permissionCollection: {
                permsGroupAdministration: {
                    viewAdminMenu: false,
                    editUser: false,
                    editRole: false
                },
                permsGroupAppAuthorization: {
                    preoccupancy: false,
                    editAuthorForEverything: false,
                    editAuthorOnlyManager: false
                },
                permsGroupAlarm: {
                    editAlarmForEverything: false,
                    editAlarmOnlyGroupMember: false
                },
                permsGroupUserGroup: {
                    editGroupForEverything: false,
                    editGroupOnlyGroupMember: false
                }
            }
        };
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    checkSaveBtnDisable(): boolean {
        return !(this.hasRoleEditPerm === true && this.isSelectedRole() && this.dataChanged);
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
    }
    onCreate(): void {
        this.showProcessing();
        this.roleInfoDataService.create({
            roleId: this.roleId,
            permissionCollection: {
                permsGroupAdministration: {
                    viewAdminMenu: this.lastChangedData.viewAdminMenu,
                    editUser: this.lastChangedData.editUser,
                    editRole: this.lastChangedData.editRole
                },
                permsGroupAppAuthorization: {
                    preoccupancy: this.lastChangedData.preoccupancy,
                    editAuthorForEverything: this.lastChangedData.editAuthorForEverything,
                    editAuthorOnlyManager: this.lastChangedData.editAuthorOnlyManager
                },
                permsGroupAlarm: {
                    editAlarmForEverything: this.lastChangedData.editAlarmForEverything,
                    editAlarmOnlyGroupMember: this.lastChangedData.editAlarmOnlyGroupMember
                },
                permsGroupUserGroup: {
                    editGroupForEverything: this.lastChangedData.editGroupForEverything,
                    editGroupOnlyGroupMember: this.lastChangedData.editGroupOnlyGroupMember
                }
            }
        }).subscribe((response: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.ROLE_INFO_CREATED,
                    param: [this.roleId]
                });
                this.hideProcessing();
            }
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    onUpdate(): void {
        this.showProcessing();
        this.roleInfoDataService.update({
            roleId: this.roleInfo.roleId,
            permissionCollection: {
                permsGroupAdministration: {
                    viewAdminMenu: this.lastChangedData.viewAdminMenu,
                    editUser: this.lastChangedData.editUser,
                    editRole: this.lastChangedData.editRole
                },
                permsGroupAppAuthorization: {
                    preoccupancy: this.lastChangedData.preoccupancy,
                    editAuthorForEverything: this.lastChangedData.editAuthorForEverything,
                    editAuthorOnlyManager: this.lastChangedData.editAuthorOnlyManager
                },
                permsGroupAlarm: {
                    editAlarmForEverything: this.lastChangedData.editAlarmForEverything,
                    editAlarmOnlyGroupMember: this.lastChangedData.editAlarmOnlyGroupMember
                },
                permsGroupUserGroup: {
                    editGroupForEverything: this.lastChangedData.editGroupForEverything,
                    editGroupOnlyGroupMember: this.lastChangedData.editGroupOnlyGroupMember
                }
            }
        }).subscribe((response: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.hideProcessing();
            }
            this.changeDetectorRef.detectChanges();
        }, (error: any) => {
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
    }
    onRemove(): void {
        this.showProcessing();
        this.roleInfoDataService.remove(this.roleInfo.roleId).subscribe((response: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.ROLE_INFO_REMOVED,
                    param: []
                });
                this.currentAction = CRUD_ACTION.NONE;
                this.currentRoleId = '';
                this.roleInfo = null;
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
