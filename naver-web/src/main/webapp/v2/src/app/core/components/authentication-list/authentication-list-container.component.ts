import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, UserPermissionCheckService, UserConfigurationDataService } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { IParam } from './authentication-list.component';
import { IAuthorityCommandForm, IAuthorityData, ILabel } from './authentication-create-and-update.component';
import { AuthenticationInteractionService, CMD_TYPE } from './authentication-interaction.service';
import { AuthenticationDataService, POSITION, IAuthentication, IApplicationAuthData, IAuthenticationCreated, IAuthenticationResponse } from 'app/core/components/authentication-list/authentication-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-authentication-list-container',
    templateUrl: './authentication-list-container.component.html',
    styleUrls: ['./authentication-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthenticationListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private currentApplication: IApplication = null;
    hasUpdateAndRemoveAuthority: boolean;
    showSelectedAuthInfo = false;
    selectedAuth: IApplicationAuthData;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    myPosition: string;
    authorityList: IApplicationAuthData[] = [];
    userGroupList: string[] = [];
    filteredUserGroupList: string[];

    noPermMessage: string;
    i18nLabel: ILabel;
    i18nGuide: { [key: string]: IFormFieldErrorType };

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userConfigurationDataService: UserConfigurationDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private userGroupDataSerivce: UserGroupDataService,
        private authenticationDataService: AuthenticationDataService,
        private authenticationInteractionService: AuthenticationInteractionService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService
    ) {}
    ngOnInit() {
        this.loadUserData();
        this.connectApplicationList();
        this.connectAuthenticationComponent();
        this.getI18NText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private loadUserData(): void {
        this.userGroupDataSerivce.retrieve(
            this.userPermissionCheckService.canEditAllAuth() ? {} : { userId: this.userConfigurationDataService.getUserId() }
        ).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userGroupList: IUserGroup[]) => {
            this.userGroupList = userGroupList.map((userGroup: IUserGroup) => {
                return userGroup.id;
            });
            this.changeDetectorRef.detectChanges();
        }, (_: IServerErrorFormat) => {});
    }
    private connectApplicationList(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((selectedApplication: IApplication) => {
                return selectedApplication !== null;
            })
        ).subscribe((selectedApplication: IApplication) => {
            this.currentApplication = selectedApplication;
            this.initStatus();
            this.getAuthorityData();
            this.changeDetectorRef.detectChanges();
        });
    }
    private connectAuthenticationComponent(): void {
        this.authenticationInteractionService.onComplete$.subscribe((formData: IAuthorityCommandForm) => {
            switch (formData.type) {
                case CMD_TYPE.CREATE:
                    this.onCreateAuth(formData.data as IAuthorityData);
                    break;
                case CMD_TYPE.UPDATE:
                    this.onUpdateAuth(formData.data as IAuthorityData);
                    break;
                case CMD_TYPE.CLOSE:
                    this.onCloseAuth();
                    break;
            }
        });
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
        ).subscribe(([requiredMessage, noPermMessage, positionLabel, userGroupLabel, serverMapLabel, apiMetaLabel, paramMetaLabel, sqlMetaLabel]: string[]) => {
            this.i18nGuide = {
                position: { required: this.translateReplaceService.replace(requiredMessage, positionLabel) },
                userGroupId: { required: this.translateReplaceService.replace(requiredMessage, userGroupLabel) }
            };
            this.i18nLabel = {
                POSITION: positionLabel,
                USER_GROUP: userGroupLabel,
                SERVER_MAP: serverMapLabel,
                API_META: apiMetaLabel,
                PARAM_META: paramMetaLabel,
                SQL_META: sqlMetaLabel
            };
            // TODO: 현재 안쓰이는중
            this.noPermMessage = noPermMessage;
        });
    }
    private initStatus(): void {
        this.errorMessage = '';
        this.showSelectedAuthInfo = false;
    }
    private getAuthorityData(): void {
        this.showProcessing();
        this.authenticationDataService.retrieve(this.currentApplication.getApplicationName()).subscribe((data: IAuthentication | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.errorMessage = data.errorMessage;
                this.hideProcessing();
            } else {
                this.myPosition = data.myPosition;
                this.authorityList = data.userGroupAuthList;
                this.hasUpdateAndRemoveAuthority = this.userPermissionCheckService.canUpdateAndRemoveAuth(this.isManager());
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
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
    private getAuth(userGroupId: string, position: string): IApplicationAuthData {
        return this.authorityList.find((auth: IApplicationAuthData) => {
            return userGroupId === auth.userGroupId && position === auth.position;
        });
    }
    private hasAddAuthority(): boolean {
        if (this.userPermissionCheckService.canPreoccupancy()) {
            if (this.userPermissionCheckService.canAddAuth(this.isManager())) {
                return true;
            } else {
                if (this.includeManagerPositionInList() === false) {
                    return true;
                }
            }
        } else {
            if ((this.userPermissionCheckService.canEditAllAuth() || this.userPermissionCheckService.canEditMyAuth()) && this.isManager()) {
                return true;
            }
        }
        return false;
    }
    private getRequestParam(authInfo: IAuthorityData): IApplicationAuthData {
        return {
            applicationId: authInfo.applicationId,
            configuration: {
                apiMetaData: authInfo.apiMeta,
                paramMetaData: authInfo.paramMeta,
                serverMapData: authInfo.serverMap,
                sqlMetaData: authInfo.sqlMeta
            },
            position: authInfo.position,
            userGroupId: authInfo.userGroupId
        };
    }
    onShowCreateAuth(): void {
        if ((this.isApplicationSelected() === false || this.hasAddAuthority()) === false) {
            return;
        }
        this.authenticationInteractionService.showCreate({
            applicationId: this.currentApplication.getApplicationName(),
            userGroupList: this.getFilteredUserGroupList(),
            fixPosition: this.includeManagerPositionInList() ? '' : POSITION.MANAGER
        });
    }
    onCreateAuth(authInfo: IAuthorityData): void {
        this.showProcessing();
        this.authenticationDataService.create(this.getRequestParam(authInfo)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onUpdateAuth(authInfo: IAuthorityData): void {
        this.showProcessing();
        this.authenticationDataService.update(this.getRequestParam(authInfo)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onCloseAuth(): void {}
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    onRemoveAuth(auth: IParam): void {
        this.showProcessing();
        this.authenticationDataService.remove(auth.userGroupId, auth.applicationId).subscribe((response: IAuthenticationResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onShowUpdateAuth(param: IParam): void {
        const authInfo = this.authorityList.find((auth: IApplicationAuthData) => {
            return auth.userGroupId === param.userGroupId;
        });
        this.authenticationInteractionService.showUpdate({
            applicationId: this.currentApplication.getApplicationName(),
            userGroupList: this.getFilteredUserGroupList(authInfo.userGroupId),
            fixPosition: '',
            data: {
                position: authInfo.position,
                userGroupId: authInfo.userGroupId,
                serverMap: authInfo.configuration.serverMapData,
                apiMeta: authInfo.configuration.apiMetaData,
                paramMeta: authInfo.configuration.paramMetaData,
                sqlMeta: authInfo.configuration.sqlMetaData
            } as IAuthorityData
        });
    }
    onShowAuthInfo(auth: IParam): void {
        this.selectedAuth = this.getAuth(auth.userGroupId, auth.position);
        this.showSelectedAuthInfo = true;
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
    private includeManagerPositionInList(): boolean {
        return this.authorityList.reduce((prev: boolean, auth: IApplicationAuthData) => {
            return prev || auth.position === POSITION.MANAGER;
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
