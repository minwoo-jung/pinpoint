import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { TranslateReplaceService } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { AuthenticationDataService, ROLE, IAuthentication, IApplicationAuthData, IAuthenticationCreated, IAuthenticationResponse } from 'app/core/components/authentication-list/authentication-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { Application } from 'app/core/models/application';
import { Authentication } from './authentication-create-and-update.component';

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
    private editAuthIndex: number;
    showSelectedAuthInfo = false;
    selectedAuth: IApplicationAuthData;
    useDisable = false;
    showLoading = false;
    showCreate = false;
    message = '';
    myRole: string;
    fixEditRole: string;
    authenticationList: IApplicationAuthData[] = [];
    userGroupList: string[] = [];
    filteredUserGroupList: string[];
    editAuth: Authentication;

    i18nLabel = {
        ROLE: '',
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
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private authenticationDataService: AuthenticationDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService
    ) { }
    ngOnInit() {
        this.userGroupDataSerivce.retrieve().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userGroupList: IUserGroup[]) => {
            this.userGroupList = userGroupList.map((userGroup: IUserGroup) => {
                return userGroup.id;
            });
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {

        });
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((selectedApplication: Application) => {
            this.currentApplication = selectedApplication;
            this.initStatus();
            this.getAuthenticationData();
            this.changeDetectorRef.detectChanges();
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
            this.translateService.get('CONFIGURATION.COMMON.ROLE'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.AUTH.SERVER_MAP'),
            this.translateService.get('CONFIGURATION.AUTH.API_META'),
            this.translateService.get('CONFIGURATION.AUTH.PARAM_META'),
            this.translateService.get('CONFIGURATION.AUTH.SQL_META'),
        ).subscribe((i18n: string[]) => {
            this.i18nGuide.DO_NOT_HAVE_PERMISSION = i18n[1];

            this.i18nGuide.ROLE_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[2]);
            this.i18nGuide.USER_GROUP_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[3]);

            this.i18nLabel.ROLE = i18n[2];
            this.i18nLabel.USER_GROUP = i18n[3];
            this.i18nLabel.SERVER_MAP = i18n[4];
            this.i18nLabel.API_META = i18n[5];
            this.i18nLabel.PARAM_META = i18n[6];
            this.i18nLabel.SQL_META = i18n[7];
        });
    }
    private initStatus(): void {
        this.fixEditRole = '';
        this.message = '';
        this.showSelectedAuthInfo = false;
    }
    private getAuthenticationData(): void {
        this.showProcessing();
        this.authenticationDataService.retrieve(this.currentApplication.getApplicationName()).subscribe((authenticationData: IAuthentication | IServerErrorShortFormat) => {
            if ((authenticationData as IServerErrorShortFormat).errorCode) {
                this.message = (authenticationData as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.myRole = (authenticationData as IAuthentication).myRole;
                this.authenticationList = (authenticationData as IAuthentication).userGroupAuthList;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
            this.changeDetectorRef.detectChanges();
        });
    }
    private getAuth(userGroupId: string, role: string): IApplicationAuthData {
        return this.authenticationList.filter((auth: IApplicationAuthData) => {
            return userGroupId === auth.userGroupId && role === auth.role;
        })[0];
    }
    private getAuthIndex(userGroupId: string): number {
        let index = -1;
        for (let i = 0 ; i < this.authenticationList.length ; i++) {
            if (this.authenticationList[i].userGroupId === userGroupId) {
                index = i;
                break;
            }
        }
        return index;
    }
    private isManager(role: string): boolean {
        return role === ROLE.MANAGER;
    }
    private isGuest(role: string): boolean {
        return role === ROLE.GUEST;
    }
    private hasManagerGroup(): boolean {
        return this.getManagerRoleCount() > 0;
    }
    hasAuthority(): boolean {
        if (this.isManager(this.myRole)) {
            return true;
        }
        this.message = this.i18nGuide.DO_NOT_HAVE_PERMISSION;
        return false;
    }
    private hasAddAuthority(): boolean {
        if (this.isManager(this.myRole)) {
            return true;
        }
        if (this.isGuest(this.myRole) && this.hasManagerGroup() === false) {
            return true;
        }
        return false;
    }
    onCreateAuth(auth: Authentication): void {
        this.showProcessing();
        this.authenticationDataService.create({
            applicationId: this.currentApplication.getApplicationName(),
            configuration: {
                apiMetaData: auth.apiMeta,
                paramMetaData: auth.paramMeta,
                serverMapData: auth.serverMap,
                sqlMetaData: auth.sqlMeta
            },
            role: auth.role,
            userGroupId: auth.userGroupId
        }).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthenticationData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onUpdateAuth(auth: Authentication): void {
        const editAuth = this.authenticationList[this.editAuthIndex];
        this.authenticationDataService.update({
            applicationId: editAuth.applicationId,
            configuration: {
                apiMetaData: auth.apiMeta,
                paramMetaData: auth.paramMeta,
                serverMapData: auth.serverMap,
                sqlMetaData: auth.sqlMeta
            },
            role: auth.role,
            userGroupId: auth.userGroupId
        }).subscribe((response: IAuthenticationCreated | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthenticationData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onAddAuthPopup(): void {
        if (this.isApplicationSelected() === false) {
            return;
        }
        this.fixEditRole = this.hasManagerGroup() ? '' : ROLE.MANAGER;
        this.onShowAuthPopup();
    }
    private onShowAuthPopup(): void {
        this.filteredUserGroupList = this.userGroupList.filter((id: string) => {
            return this.authenticationList.find((auth: IApplicationAuthData) => {
                return auth.userGroupId === id;
            }) === undefined || (this.editAuth && this.editAuth.userGroupId === id);
        });
        this.showCreate = true;
    }
    onCloseCreateAuthPopup(): void {
        this.editAuth = null;
        this.fixEditRole = '';
        this.showCreate = false;
    }
    onCloseMessage(): void {
        this.message = '';
    }
    onRemoveAuth(auth: EventEmiiterParam): void {
        if (this.hasAuthority() === false ) {
            return;
        }
        this.showProcessing();
        this.authenticationDataService.remove(auth.userGroupId, auth.applicationId)
        .subscribe((response: IAuthenticationResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAuthenticationData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onEditAuth(auth: EventEmiiterParam): void {
        if (this.hasAuthority() === false ) {
            return;
        }
        this.fixEditRole = this.getEditFixRole(auth.role);
        this.editAuthIndex = this.getAuthIndex(auth.userGroupId);
        const editAuth = this.authenticationList[this.editAuthIndex];
        this.editAuth = new Authentication(
            editAuth.role,
            editAuth.userGroupId,
            editAuth.configuration.serverMapData,
            editAuth.configuration.apiMetaData,
            editAuth.configuration.paramMetaData,
            editAuth.configuration.sqlMetaData
        );
        this.onShowAuthPopup();
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
    private getEditFixRole(role: string): string {
        if (this.isGuest(role)) {
            return ROLE.GUEST;
        }
        if (this.getManagerRoleCount() === 1) {
            return ROLE.MANAGER;
        }
        return '';
    }
    private getManagerRoleCount(): number {
        let count = 0;
        for (let i = 0 ; i < this.authenticationList.length ; i++) {
            if (this.authenticationList[i].role === ROLE.MANAGER) {
                count++;
            }
        }
        return count;
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
