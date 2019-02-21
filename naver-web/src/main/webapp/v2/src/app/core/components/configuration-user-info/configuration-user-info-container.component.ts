import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { tap, filter, pluck, takeUntil } from 'rxjs/operators';

import { UserProfileInteractionService, IChangedProfileState } from 'app/core/components/user-profile/user-profile-interaction.service';
import { UserPasswordInteractionService, IChangedPasswordState } from 'app/core/components/user-password/user-password-interaction.service';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';
import { UserPermissionCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-user-info-container',
    templateUrl: './configuration-user-info-container.component.html',
    styleUrls: ['./configuration-user-info-container.component.css']
})
export class ConfigurationUserInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private userProfile: IUserProfile;
    private userPassword: IUserPassword;

    hasUserEditPerm: boolean;
    isUserProfileValid: boolean;
    isUserPasswordValid: boolean;

    constructor(
        @Inject('userInfo') public userInfo: any,
        private userProfileInteractionService: UserProfileInteractionService,
        private userPasswordInteractionService: UserPasswordInteractionService,
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
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
