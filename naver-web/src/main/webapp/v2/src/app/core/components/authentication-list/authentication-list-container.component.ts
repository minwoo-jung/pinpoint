import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { TranslateReplaceService, UserPermissionCheckService, UserConfigurationDataService } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { AuthenticationDataService, POSITION, IAuthentication, IApplicationAuthData, IAuthenticationCreated, IAuthenticationResponse } from 'app/core/components/authentication-list/authentication-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { Application } from 'app/core/models/application';
import { IAuthorityCommandForm, IApplicationAuthority } from './authentication-create-and-update.component';
import { AuthenticationInteractionService, CMD_TYPE } from './authentication-interaction.service';

interface EventEmiiterParam {
    userGroupId: string;
    applicationId?: string;
    role?: string;
}

@Component({
    selector: 'pp-authentication-list-container',
    templateUrl: './authentication-list-container.component.html',
    styleUrls: ['./authentication-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthenticationListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private currentApplication: Application = null;
    hasUpdateAndRemoveAuthority: boolean;
    showSelectedAuthInfo = false;
    selectedAuth: IApplicationAuthData;
    message = '';
    useDisable = false;
    showLoading = false;
    myPosition: string;
    authorityList: IApplicationAuthData[] = [];
    userGroupList: string[] = [];
    filteredUserGroupList: string[];

    i18nLabel = {
        POSITION: '',
        USER_GROUP: '',
        SERVER_MAP: '',
        API_META: '',
        PARAM_META: '',
        SQL_META: ''
    };
    i18nGuide = {
        DO_NOT_HAVE_PERMISSION: '',
        ROLE_REQUIRED: '',
        USER_GROUP_REQUIRED: ''
    };
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private userConfigurationDataService: UserConfigurationDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private authenticationDataService: AuthenticationDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private authenticationInteractionService: AuthenticationInteractionService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService
    ) { }
    ngOnInit() {
        this.userGroupDataSerivce.retrieve(
            this.userPermissionCheckService.canEditAllAuth() ? {} : { userId: this.userConfigurationDataService.getUserId() }
        ).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userGroupList: IUserGroup[]) => {
            this.userGroupList = userGroupList.map((userGroup: IUserGroup) => {
                return userGroup.id;
            });
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {

        });
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((selectedApplication: Application) => {
                return selectedApplication !== null;
            })
        ).subscribe((selectedApplication: Application) => {
            this.currentApplication = selectedApplication;
            this.initStatus();
            this.getAuthorityData();
            this.changeDetectorRef.detectChanges();
        });
        this.authenticationInteractionService.onComplete$.subscribe((formData: IAuthorityCommandForm) => {
            switch (formData.type) {
                case CMD_TYPE.CREATE:
                    this.onCreateAuth(formData.data as IApplicationAuthority);
                    break;
                case CMD_TYPE.UPDATE:
                    this.onUpdateAuth(formData.data as IApplicationAuthority);
                    break;
                case CMD_TYPE.CLOSE:
                    this.onCloseAuth();
                    break;
            }
        });
        this.getI18NText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        combineLatest(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('COMMON.DO_NOT_HAVE_PERMISSION'),
            this.translateService.get('CONFIGURATION.COMMON.POSITION'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.AUTH.SERVER_MAP'),
            this.translateService.get('CONFIGURATION.AUTH.API_META'),
            this.translateService.get('CONFIGURATION.AUTH.PARAM_META'),
            this.translateService.get('CONFIGURATION.AUTH.SQL_META'),
        ).subscribe((i18n: string[]) => {
            this.i18nGuide.DO_NOT_HAVE_PERMISSION = i18n[1];

            this.i18nGuide.ROLE_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[2]);
            this.i18nGuide.USER_GROUP_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[3]);

            this.i18nLabel.POSITION = i18n[2];
            this.i18nLabel.USER_GROUP = i18n[3];
            this.i18nLabel.SERVER_MAP = i18n[4];
            this.i18nLabel.API_META = i18n[5];
            this.i18nLabel.PARAM_META = i18n[6];
            this.i18nLabel.SQL_META = i18n[7];
        });
    }
    private initStatus(): void {
        this.message = '';
        this.showSelectedAuthInfo = false;
    }
    private getAuthorityData(): void {
        this.showProcessing();
        this.authenticationDataService.retrieve(this.currentApplication.getApplicationName()).subscribe((authenticationData: IAuthentication | IServerErrorShortFormat) => {
            if ((authenticationData as IServerErrorShortFormat).errorCode) {
                this.message = (authenticationData as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.myPosition = (authenticationData as IAuthentication).myRole;
                this.authorityList = (authenticationData as IAuthentication).userGroupAuthList;
                this.hasUpdateAndRemoveAuthority = this.userPermissionCheckService.canUpdateAndRemoveAuth(this.isManager());
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
            this.changeDetectorRef.detectChanges();
        });
    }
    private getFilteredUserGroupList(userGroupId?: string): string[] {
        if (this.userPermissionCheckService.canEditAllAuth()) {
            return this.userGroupList;
        } else {
            if (userGroupId && userGroupId !== POSITION.GUEST) {
                if (this.userGroupList.indexOf(userGroupId) === -1) {
                    return this.userGroupList.concat(userGroupId);
                } else {
                    return this.userGroupList;
                }
            }
        }
        return this.userGroupList;
    }
    private getAuth(userGroupId: string, role: string): IApplicationAuthData {
        return this.authorityList.find((auth: IApplicationAuthData) => {
            return userGroupId === auth.userGroupId && role === auth.role;
        });
    }
    private hasAddAuthority(): boolean {
        if (this.userPermissionCheckService.canPreoccupancy()) {
            if (this.userPermissionCheckService.canAddAuth(this.isManager())) {
                return true;
            } else {
                if (this.includeManagerRoleInList() === false) {
                    return true;
                }
            }
        }
        return false;
    }
    private getRequestParam(authInfo: IApplicationAuthority): IApplicationAuthData {
        return {
            applicationId: authInfo.applicationId,
            configuration: {
                apiMetaData: authInfo.apiMeta,
                paramMetaData: authInfo.paramMeta,
                serverMapData: authInfo.serverMap,
                sqlMetaData: authInfo.sqlMeta
            },
            role: authInfo.position,
            userGroupId: authInfo.userGroupId
        };
    }
    onShowAddAuth(): void {
        if ((this.isApplicationSelected() && this.hasAddAuthority()) === false) {
            return;
        }
        this.authenticationInteractionService.showCreate({
            applicationId: this.currentApplication.getApplicationName(),
            userGroupList: this.getFilteredUserGroupList(),
            fixPosition: this.includeManagerRoleInList() ? '' : POSITION.MANAGER
        });
    }
    onCreateAuth(authInfo: IApplicationAuthority): void {
        this.showProcessing();
        this.authenticationDataService.create(this.getRequestParam(authInfo)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onUpdateAuth(authInfo: IApplicationAuthority): void {
        this.showProcessing();
        this.authenticationDataService.update(this.getRequestParam(authInfo)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onCloseAuth(): void {}
    onCloseMessage(): void {
        this.message = '';
    }
    onRemoveAuth(auth: EventEmiiterParam): void {
        if (this.hasUpdateAndRemoveAuthority === false) {
            this.message = this.i18nGuide.DO_NOT_HAVE_PERMISSION;
            return;
        }
        this.showProcessing();
        this.authenticationDataService.remove(auth.userGroupId, auth.applicationId).subscribe((response: IAuthenticationResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onShowUpdateAuth(param: EventEmiiterParam): void {
        if (this.hasUpdateAndRemoveAuthority === false) {
            this.message = this.i18nGuide.DO_NOT_HAVE_PERMISSION;
            return;
        }
        const authInfo = this.authorityList.find((auth: IApplicationAuthData) => {
            return auth.userGroupId === param.userGroupId;
        });
        this.authenticationInteractionService.showUpdate({
            applicationId: this.currentApplication.getApplicationName(),
            userGroupList: this.getFilteredUserGroupList(authInfo.userGroupId),
            fixPosition: '',
            data: {
                position: authInfo.position || authInfo.role,
                userGroupId: authInfo.userGroupId,
                serverMap: authInfo.configuration.serverMapData,
                apiMeta: authInfo.configuration.apiMetaData,
                paramMeta: authInfo.configuration.paramMetaData,
                sqlMeta: authInfo.configuration.sqlMetaData
            } as IApplicationAuthority
        });
    }
    onShowAuthInfo(auth: EventEmiiterParam): void {
        this.selectedAuth = this.getAuth(auth.userGroupId, auth.role);
        this.showSelectedAuthInfo = true;
    }
    hasMessage(): boolean {
        return this.message !== '';
    }
    isApplicationSelected(): boolean {
        return this.currentApplication !== null;
    }
    hideSelectedAuthInfo(): void {
        this.showSelectedAuthInfo = false;
    }
    getAddButtonClass(): object {
        return {
            'btn-blue': this.isApplicationSelected() && this.hasAddAuthority(),
            'btn-gray': !this.isApplicationSelected() || !this.hasAddAuthority()
        };
    }
    private isManager(position?: string): boolean {
        return (position || this.myPosition) === POSITION.MANAGER;
    }
    private includeManagerRoleInList(): boolean {
        return this.authorityList.reduce((prev: boolean, auth: IApplicationAuthData) => {
            return prev || auth.role === POSITION.MANAGER;
        }, false);
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
