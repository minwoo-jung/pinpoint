import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { tap, filter, pluck, takeUntil } from 'rxjs/operators';

import { UserProfileInteractionService, IChangedProfileState } from 'app/core/components/user-profile/user-profile-interaction.service';
import { UserPasswordInteractionService, IChangedPasswordState } from 'app/core/components/user-password/user-password-interaction.service';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';
import { IUserInfo } from 'app/core/components/configuration-users/configuration-users-data.service';
import { UserPermissionCheckService } from 'app/shared/services';
import { RoleListInteractionService } from 'app/core/components/role-list/role-list-interaction.service';
import { ConfigurationUserInfoInteractionService } from './configuration-user-info-interaction.service';
import { ConfigurationUserInfoDataService } from './configuration-user-info-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-configuration-user-info-container',
    templateUrl: './configuration-user-info-container.component.html',
    styleUrls: ['./configuration-user-info-container.component.css']
})
export class ConfigurationUserInfoContainerComponent implements OnInit, OnDestroy {
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

    constructor(
        @Inject('userInfo') public userInfo: IUserInfo,
        private userProfileInteractionService: UserProfileInteractionService,
        private userPasswordInteractionService: UserPasswordInteractionService,
        private roleListInteractionService: RoleListInteractionService,
        private configurationUserInfoInteractionService: ConfigurationUserInfoInteractionService,
        private configurationUserInfoDataService: ConfigurationUserInfoDataService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}

    ngOnInit() {
        // this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.hasUserEditPerm = true;
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
            console.log(profile);
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
            console.log(password);
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
        this.configurationUserInfoDataService.insertUser({
            profile: this.userProfile,
            account: this.userPassword,
            role: { roleList: this.userRoleList }
        }).subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : (
                    this.isUserInserted = true,
                    this.configurationUserInfoInteractionService.notifyUserCreate(result.userId),
                    setTimeout(() => {
                        this.userInfo = {
                            profile: this.userProfile,
                            account: {} as IUserPassword,
                            role: { roleList: this.userRoleList }
                        };
                    }, 1000)
                );
        });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
