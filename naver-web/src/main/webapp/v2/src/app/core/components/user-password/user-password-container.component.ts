import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin, iif } from 'rxjs';
import { map } from 'rxjs/operators';

import { UserPasswordInteractionService, IChangedPasswordState } from './user-password-interaction.service';
import { IUserPassword, UserPasswordDataService } from 'app/core/components/user-password/user-password-data.service';
import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { isThatType } from 'app/core/utils/util';

export enum UserType {
    ADMIN,
    ELSE
}

@Component({
    selector: 'pp-user-password-container',
    templateUrl: './user-password-container.component.html',
    styleUrls: ['./user-password-container.component.css']
})
export class UserPasswordContainerComponent implements OnInit, OnChanges {
    @Input() userPassword: IUserPassword;
    @Input() hasUserEditPerm: boolean;
    @Input() userId: string;
    @Input() loggedInUserType: UserType

    private tempUserPassword: IUserPassword;

    userType = UserType;
    isValid: boolean;
    isUpdated = false;
    fieldErrorMessage$: Observable<{ [key: string]: IFormFieldErrorType }>;
    fieldLabel$: Observable<{ [key: string]: string }>;
    errorMessage: string;
    buttonText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userPasswordInteractionService: UserPasswordInteractionService,
        private userPasswordDataService: UserPasswordDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        const userIdChange = changes['userId'];

        if (userIdChange) {
            this.userPassword = {} as IUserPassword;
            this.isValid = false;
            this.isUpdated = false;
        }
    }

    ngOnInit() {
        this.buttonText$ = this.translateService.get('COMMON.SUBMIT');
        this.fieldErrorMessage$ = forkJoin(
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.PASSWORD'),
            this.translateService.get('CONFIGURATION.COMMON.PASSWORD_MISMATCH')
        ).pipe(
            map(([requiredMessage, passwordLabel, mismatchMessage]: string[]) => {
                return {
                    password: {
                        required: this.translateReplaceService.replace(requiredMessage, passwordLabel),
                    },
                    confirmPassword: {
                        required: this.translateReplaceService.replace(requiredMessage, passwordLabel),
                        pwMustMatch: mismatchMessage
                    }
                };
            })
        );
        this.fieldLabel$ = iif(() => this.loggedInUserType === UserType.ADMIN,
            this.translateService.get('CONFIGURATION.COMMON.PASSWORD').pipe(
                map((password: string) => ({ password }))
            ),
            forkJoin(
                this.translateService.get('CONFIGURATION.COMMON.CURRENT_PASSWORD'),
                this.translateService.get('CONFIGURATION.COMMON.NEW_PASSWORD'),
                this.translateService.get('CONFIGURATION.COMMON.CONFIRM_NEW_PASSWORD'),
            ).pipe(
                map(([currentPassword, newPassword, confirmNewPassword]: string[]) => {
                    return { currentPassword, newPassword, confirmNewPassword };
                })
            )
        );
    }

    onUserPasswordChange(change: IChangedPasswordState): void {
        const { isValid, password } = change;

        if (isValid) {
            this.tempUserPassword = password;
        }

        this.isValid = isValid;
        this.isUpdated = false;
        this.userPasswordInteractionService.notifyUserPasswordChange(change);
    }

    onClickUpdateButton(): void {
        this.userPasswordDataService.update(this.userId, this.tempUserPassword)
            .subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                    : (
                        this.isUpdated = true,
                        this.userPassword = {} as IUserPassword,
                        this.userPasswordInteractionService.notifyUserPasswordUpdate(this.userId),
                        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_USER_PASSWORD)
                    );
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
