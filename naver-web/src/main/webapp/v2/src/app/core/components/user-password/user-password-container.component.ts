import { Component, OnInit, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

import { UserPasswordInteractionService, IChangedPasswordState } from './user-password-interaction.service';
import { IUserPassword, UserPasswordDataService } from 'app/core/components/user-password/user-password-data.service';
import { TranslateReplaceService } from 'app/shared/services';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-password-container',
    templateUrl: './user-password-container.component.html',
    styleUrls: ['./user-password-container.component.css']
})
export class UserPasswordContainerComponent implements OnInit {
    @Input()
    set userPassword(userPassword: IUserPassword) {
        this._userPassword = userPassword;
        this.isValid = false;
    }

    get userPassword(): IUserPassword {
        return this._userPassword;
    }

    @Input() userId: string;

    private tempUserPassword: IUserPassword;

    _userPassword: IUserPassword;
    isValid: boolean;
    isUpdated = false;
    fieldErrorMessage$: Observable<{ [key: string]: IFormFieldErrorType }>;
    errorMessage: string;

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userPasswordInteractionService: UserPasswordInteractionService,
        private userPasswordDataService: UserPasswordDataService
    ) {}

    ngOnInit() {
        this.isValid = false;
        this.fieldErrorMessage$ = forkJoin(
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.PASSWORD'),
        ).pipe(
            map(([requiredMessage, passwordLabel]: string[]) => {
                return {
                    password: {
                        required: this.translateReplaceService.replace(requiredMessage, passwordLabel),
                    }
                };
            })
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
                        this.userPasswordInteractionService.notifyUserPasswordUpdate(result.userId)
                    );
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
