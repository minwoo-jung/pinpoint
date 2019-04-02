import { Component, OnInit, OnChanges, Input, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { UserProfileInteractionService, IChangedProfileState } from './user-profile-interaction.service';
import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { MinLength } from './user-profile.component';
import { IUserProfile, UserProfileDataService } from 'app/core/components/user-profile/user-profile-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-profile-container',
    templateUrl: './user-profile-container.component.html',
    styleUrls: ['./user-profile-container.component.css']
})
export class UserProfileContainerComponent implements OnInit, OnChanges {
    @Input() userProfile: IUserProfile;
    @Input() hasUserEditPerm: boolean;

    private tempUserProfile: IUserProfile;

    isValid: boolean;
    isUpdated = false;
    fieldErrorMessage$: Observable<{ [key: string]: IFormFieldErrorType }>;
    fieldLabel$: Observable<{ [key: string]: string }>;
    errorMessage: string;
    buttonText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userProfileInteractionService: UserProfileInteractionService,
        private userProfileDataService: UserProfileDataService,
        private analyticsService: AnalyticsService,
    ) { }
    
    ngOnChanges(changes: SimpleChanges) {
        const userProfileChange = changes['userProfile'];

        if (userProfileChange) {
            const { currentValue }: { currentValue: IUserProfile } = userProfileChange;
    
            this.isValid = !!currentValue;
            this.isUpdated = false;
        }
    }

    ngOnInit() {
        this.buttonText$ = this.translateService.get('COMMON.SUBMIT');
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
        this.fieldLabel$ = forkJoin(
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.COMMON.DEPARTMENT'),
            this.translateService.get('CONFIGURATION.COMMON.PHONE'),
            this.translateService.get('CONFIGURATION.COMMON.EMAIL'),
        ).pipe(
            map(([userId, name, department, phone, email]: string[]) => {
                return { userId, name, department, phone, email }
            })
        )
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
                        this.userProfileInteractionService.notifyUserProfileUpdate(),
                        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_USER_PROFILE)
                    );
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
