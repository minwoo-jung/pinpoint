import { Component, OnInit, OnChanges, Input, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { UserProfileInteractionService, IChangedProfileState } from './user-profile-interaction.service';
import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
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
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.USER_ID_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.NAME_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.DEPARTMENT_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.PHONE_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.EMAIL_VALIDATION'),
        ).pipe(
            map(([
                requiredMessage, idLabel, nameLabel,
                userIdValidation, nameValidation, departmentValidation, phoneValidation, emailValidation
            ]: string[]) => {
                return {
                    userId: {
                        required: this.translateReplaceService.replace(requiredMessage, idLabel),
                        valueRule: userIdValidation
                    },
                    name: {
                        required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                        valueRule: nameValidation
                    },
                    department: {
                        valueRule: departmentValidation
                    },
                    phoneNumber: {
                        valueRule: phoneValidation
                    },
                    email: {
                        minlength: emailValidation,
                        maxlength: emailValidation,
                        valueRule: emailValidation
                    }
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
            map(([userId, name, department, phoneNumber, email]: string[]) => {
                return { userId, name, department, phoneNumber, email }
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
