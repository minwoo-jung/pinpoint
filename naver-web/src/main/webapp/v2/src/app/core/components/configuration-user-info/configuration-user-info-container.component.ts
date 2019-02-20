import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { tap, filter, pluck, takeUntil } from 'rxjs/operators';

import { IUserProfile } from 'app/core/components/pinpoint-user/pinpoint-user-for-users-data.service';
import { UserProfileInteractionService, IChangedProfileState } from 'app/core/components/user-profile/user-profile-interaction.service';
import { UserPermissionCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-user-info-container',
    templateUrl: './configuration-user-info-container.component.html',
    styleUrls: ['./configuration-user-info-container.component.css']
})
export class ConfigurationUserInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private userProfile: IUserProfile;

    hasUserEditPerm: boolean;
    isUserProfileValid: boolean;

    constructor(
        @Inject('userInfo') public userInfo: any,
        private userProfileInteractionService: UserProfileInteractionService,
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
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
