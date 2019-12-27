import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl, Validators, ValidationErrors } from '@angular/forms';

import { IChangedPasswordState } from './user-password-interaction.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';
import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';

@Component({
    selector: 'pp-user-password-for-general',
    templateUrl: './user-password-for-general.component.html',
    styleUrls: ['./user-password.component.css']
})
export class UserPasswordForGeneralComponent implements OnInit {
    @Input()
    set userPassword(_: IUserPassword) {
        this.userPasswordForm.reset();
    }
    @Input() hasUserEditPerm: boolean;
    @Input() fieldErrorMessage: { [key: string]: IFormFieldErrorType };
    @Input() fieldLabel: { [key: string]: string };
    @Output() outUserPasswordChange = new EventEmitter<IChangedPasswordState>();

    userPasswordForm = new FormGroup({
        currentPassword: new FormControl('', [
            Validators.required
        ]),
        password: new FormControl('', [
            Validators.required,
            CustomFormValidatorService.validate(/^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*()])[A-Za-z0-9!@#$%^&*()]{8,24}$/)
        ]),
        confirmPassword: new FormControl('', [
            Validators.required
        ])
    }, { validators: this.mustMatch });

    constructor() {}
    ngOnInit() {}
    onKeyUp(): void {
        this.userPasswordForm.valid
            ? this.outUserPasswordChange.emit({ isValid: true, password: {
                currentPassword: this.userPasswordForm.get('currentPassword').value,
                password: this.userPasswordForm.get('password').value,
            }})
            : this.outUserPasswordChange.emit({ isValid: false });
    }

    private mustMatch(formGroup: FormGroup): ValidationErrors | null {
        const pwControl = formGroup.get('password');
        const confirmPwControl = formGroup.get('confirmPassword');

        if (confirmPwControl.errors && !confirmPwControl.errors.pwMustMatch) {
            return;
        }

        if (pwControl.value !== confirmPwControl.value) {
            const errors = { pwMustMatch : true };

            confirmPwControl.setErrors(errors);
            return errors;
        } else {
            return null;
        }
    }
}
