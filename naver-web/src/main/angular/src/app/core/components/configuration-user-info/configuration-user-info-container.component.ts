import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { tap, filter, pluck, takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { UserProfileInteractionService, IChangedProfileState } from 'app/core/components/user-profile/user-profile-interaction.service';
import { UserPasswordInteractionService, IChangedPasswordState } from 'app/core/components/user-password/user-password-interaction.service';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';
import { IUserInfo } from 'app/core/components/configuration-users/configuration-users-data.service';
import { UserPermissionCheckService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { RoleListInteractionService } from 'app/core/components/role-list/role-list-interaction.service';
import { ConfigurationUserInfoInteractionService } from './configuration-user-info-interaction.service';
import { ConfigurationUserInfoDataService } from './configuration-user-info-data.service';
import { isThatType } from 'app/core/utils/util';
import { UserType } from 'app/core/components/user-password/user-password-container.component';

@Component({
    selector: 'pp-configuration-user-info-container',
    templateUrl: './configuration-user-info-container.component.html',
    styleUrls: ['./configuration-user-info-container.component.css']
})
export class ConfigurationUserInfoContainerComponent implements OnInit, OnDestroy {
    @Input() userInfo: IUserInfo;

    private unsubscribe = new Subject<void>();
    private userProfile: IUserProfile;
    private userPassword: IUserPassword;
    private userRoleList: string[];

    hasUserEditPerm: boolean;
    isUserProfileValid: boolean;
    isUserPasswordValid: boolean;
    isUserRoleValid: boolean;
    isUserInserted: boolean;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    loggedInUserType = UserType.ADMIN;
    buttonText$: Observable<string>;

    constructor(
        private userProfileInteractionService: UserProfileInteractionService,
        private userPasswordInteractionService: UserPasswordInteractionService,
        private roleListInteractionService: RoleListInteractionService,
        private configurationUserInfoInteractionService: ConfigurationUserInfoInteractionService,
        private configurationUserInfoDataService: ConfigurationUserInfoDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.buttonText$ = this.translateService.get('COMMON.SUBMIT');
        this.userProfileInteractionService.onUserProfileChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((v: IChangedProfileState) => {
                this.isUserProfileValid = v.isValid;
            }),
            filter(({ isValid }: { isValid: boolean }) => {
                return isValid;
            }),
            pluck('profile')
        ).subscribe((profile: IUserProfile) => {
            this.userProfile = profile;
        });

        this.userPasswordInteractionService.onUserPasswordChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((v: IChangedPasswordState) => {
                this.isUserPasswordValid = v.isValid;
            }),
            filter(({ isValid }: { isValid: boolean }) => {

                return isValid;
            }),
            pluck('password')
        ).subscribe((password: IUserPassword) => {
            this.userPassword = password;
        });

        this.roleListInteractionService.onUserRoleListChange$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((roleList: string[]) => {
            this.userRoleList = roleList;
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onClickInsertUser(): void {
        this.showProcessing();
        this.configurationUserInfoDataService.insertUser({
            profile: this.userProfile,
            account: this.userPassword,
            role: { roleList: this.userRoleList }
        }).subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? (this.errorMessage = result.errorMessage, this.hideProcessing())
                : (
                    this.isUserInserted = true,
                    this.configurationUserInfoInteractionService.notifyUserCreate(),
                    this.userInfo = {
                        profile: this.userProfile,
                        account: {} as IUserPassword,
                        role: { roleList: this.userRoleList }
                    },
                    setTimeout(() => {
                        this.hideProcessing();
                    }, 1000),
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_USER_IN_USERS)
                );
        });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
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
