import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { WebAppSettingDataService, TranslateReplaceService, UserPermissionCheckService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { IParam } from './authentication-list.component';
import { ILabel, IAuthForm } from './authentication-create-and-update.component';
import { AuthenticationDataService, POSITION, IAuthentication, IApplicationAuthData, IAuthenticationCreated, IAuthenticationResponse } from 'app/core/components/authentication-list/authentication-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-authentication-list-container',
    templateUrl: './authentication-list-container.component.html',
    styleUrls: ['./authentication-list-container.component.css'],
})
export class AuthenticationListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private selectedApplication: IApplication = null;

    hasUpdateAndRemoveAuthority: boolean;
    selectedAuth: IApplicationAuthData;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    showPopup = false;
    myPosition: POSITION;
    authorityList: IApplicationAuthData[] = [];
    userGroupList: string[] = [];
    userId: string;
    noPermMessage: string;
    i18nLabel: ILabel;
    i18nFormGuide: {[key: string]: IFormFieldErrorType};
    i18nTemplateGuide = {
        APP_NOT_SELECTED: '',
    };

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private webAppSettingDataService: WebAppSettingDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private userGroupDataSerivce: UserGroupDataService,
        private authenticationDataService: AuthenticationDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.hasUpdateAndRemoveAuthority = this.userPermissionCheckService.canUpdateAndRemoveAuth(false);
        if (this.hasUpdateAndRemoveAuthority) {
            this.loadUserGroupList();
        }
        this.webAppSettingDataService.getUserId().subscribe((userId: string) => {
            this.userId = userId;
        });
        this.bindToAppSelectionEvent();
        this.initI18NText();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private loadUserGroupList(): void {
        const param = this.userPermissionCheckService.canUpdateAndRemoveAuth(this.isManager()) ? {} : {userId: this.userId};

        this.userGroupDataSerivce.retrieve(param).subscribe((userGroupList: IUserGroup[]) => {
            this.userGroupList = userGroupList.map((userGroup: IUserGroup) => userGroup.id);
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
        });
    }

    private bindToAppSelectionEvent(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((app: IApplication) => app !== null),
        ).subscribe((selectedApplication: IApplication) => {
            this.selectedApplication = selectedApplication;
            this.errorMessage = '';
            this.onClosePopup();
            this.getAuthorityData();
        });
    }

    private initI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('COMMON.DO_NOT_HAVE_PERMISSION'),
            this.translateService.get('CONFIGURATION.COMMON.POSITION'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.AUTH.SERVER_MAP'),
            this.translateService.get('CONFIGURATION.AUTH.API_META'),
            this.translateService.get('CONFIGURATION.AUTH.PARAM_META'),
            this.translateService.get('CONFIGURATION.AUTH.SQL_META'),
            this.translateService.get('COMMON.SELECT_YOUR_APP')
        ).subscribe(([requiredMessage, noPermMessage, positionLabel, userGroupLabel, serverMapLabel, apiMetaLabel, paramMetaLabel, sqlMetaLabel, selectApp]: string[]) => {
            this.i18nFormGuide = {
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

            this.i18nTemplateGuide.APP_NOT_SELECTED = selectApp;
            this.noPermMessage = noPermMessage;
        });
    }

    private getAuthorityData(): void {
        const prevHasUpdateAndRemoveAuthority = this.hasUpdateAndRemoveAuthority;

        this.showProcessing();
        this.authenticationDataService.retrieve(this.selectedApplication.getApplicationName()).subscribe((data: IAuthentication | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.errorMessage = data.errorMessage;
                this.hideProcessing();
            } else {
                this.myPosition = data.myPosition;
                this.authorityList = data.userGroupAuthList;
                this.hasUpdateAndRemoveAuthority = this.userPermissionCheckService.canUpdateAndRemoveAuth(this.isManager());
                if (prevHasUpdateAndRemoveAuthority === false) {
                    this.loadUserGroupList();
                }
                this.hideProcessing();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    private hasAddAuthority(): boolean {
        if (this.userPermissionCheckService.canPreoccupancy()) {
            if (this.userPermissionCheckService.canAddAuth(this.isManager())) {
                return true;
            } else {
                if (!this.alreadyHasManager()) {
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

    private getRequestParam({position, userGroupId, apiMetaData, paramMetaData, serverMapData, sqlMetaData}: IAuthForm): IApplicationAuthData {
        return {
            applicationId: this.selectedApplication.getApplicationName(),
            configuration: {
                apiMetaData,
                paramMetaData,
                serverMapData,
                sqlMetaData
            },
            position,
            userGroupId
        };
    }

    shouldDisabled(): boolean {
        return !this.isApplicationSelected() || !this.hasAddAuthority();
    }

    onClickAddBtn(): void {
        this.showPopup = true;
        this.selectedAuth = null;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_AUTH_CREATION_POPUP);
    }

    onCreateAuth(value: IAuthForm): void {
        this.showProcessing();
        this.authenticationDataService.create(this.getRequestParam(value)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_AUTH);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onUpdateAuth(value: IAuthForm): void {
        this.showProcessing();
        this.authenticationDataService.update(this.getRequestParam(value)).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthorityData();
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_AUTH);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onClosePopup(): void {
        this.showPopup = false;
    }

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
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_AUTH);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onShowUpdateAuth({userGroupId}: IParam): void {
        this.selectedAuth = this.authorityList.find(({userGroupId: id}: IApplicationAuthData) => id === userGroupId);
        this.showPopup = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_AUTH_UPDATE_POPUP);
    }

    onShowAuthInfo(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_SELECTED_AUTH_INFO);
    }

    isApplicationSelected(): boolean {
        return this.selectedApplication !== null;
    }

    showGuide(): boolean {
        return !this.isApplicationSelected();
    }

    get guideMessage(): string {
        return this.i18nTemplateGuide.APP_NOT_SELECTED;
    }

    private isManager(): boolean {
        return this.myPosition === POSITION.MANAGER;
    }

    alreadyHasManager(): boolean {
        return !!this.authorityList.find(({position}: IApplicationAuthData) => position === POSITION.MANAGER);
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
