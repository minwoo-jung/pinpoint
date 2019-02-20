import { Component, OnInit, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { UserProfileInteractionService, IChangedProfileState } from './user-profile-interaction.service';
import { TranslateReplaceService } from 'app/shared/services';
import { MinLength } from './user-profile.component';
import { IUserProfile, UserProfileDataService } from 'app/core/components/user-profile/user-profile-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-profile-container',
    templateUrl: './user-profile-container.component.html',
    styleUrls: ['./user-profile-container.component.css']
})
export class UserProfileContainerComponent implements OnInit {
    @Input() userProfile: IUserProfile;
    @Input() hasUserEditPerm: boolean;

    private tempUserProfile: IUserProfile;

    isValid: boolean;
    isUpdated = false;
    fieldErrorMessage$: Observable<{ [key: string]: IFormFieldErrorType }>;
    errorMessage: string;

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userProfileInteractionService: UserProfileInteractionService,
        private userProfileDataService: UserProfileDataService
    ) { }

    ngOnInit() {
        this.isValid = !!this.userProfile;
        this.tempUserProfile = this.userProfile;
        this.fieldErrorMessage$ = forkJoin(
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
        ).pipe(
            map(([requiredMessage, minLengthMessage, idLabel, nameLabel]: string[]) => {
                return {
                    userId: {
                        required: this.translateReplaceService.replace(requiredMessage, idLabel),
                        minlength: this.translateReplaceService.replace(minLengthMessage, MinLength.USER_ID)
                    },
                    name: {
                        required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                        minlength: this.translateReplaceService.replace(minLengthMessage, MinLength.NAME)
                    },
                };
            })
        );
    }

    onUserProfileChange(change: IChangedProfileState): void {
        const { isValid, profile } = change;

        if (isValid) {
            this.tempUserProfile = profile;
        }

        this.isValid = isValid;
        this.isUpdated = false;
        this.userProfileInteractionService.notifyUserProfileChange(change);
    }

    onClickUpdateButton(): void {
        this.userProfileDataService.update(this.tempUserProfile)
            .subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                    : (
                        this.isUpdated = true,
                        this.userProfile = this.tempUserProfile,
                        this.userProfileInteractionService.notifyUserProfileUpdate(result.userId)
                    );
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
