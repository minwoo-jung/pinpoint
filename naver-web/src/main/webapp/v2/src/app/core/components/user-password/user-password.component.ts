import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IChangedPasswordState } from './user-password-interaction.service';
import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';

@Component({
    selector: 'pp-user-password',
    templateUrl: './user-password.component.html',
    styleUrls: ['./user-password.component.css']
})
export class UserPasswordComponent implements OnInit {
    @Input()
    set userPassword(_: IUserPassword) {
        this.userPasswordForm.reset();
    }
    @Input() fieldErrorMessage: { [key: string]: IFormFieldErrorType };
    @Input() fieldLabel: { [key: string]: string };
    @Output() outUserPasswordChange = new EventEmitter<IChangedPasswordState>();

    userPasswordForm = new FormGroup({
        password: new FormControl('', [
            // TODO: password 규칙?
            Validators.required
        ]),
    });

    constructor() {}
    ngOnInit() {}
    onKeyUp(): void {
        this.userPasswordForm.valid
            ? this.outUserPasswordChange.emit({ isValid: true, password: this.userPasswordForm.value })
            : this.outUserPasswordChange.emit({ isValid: false });
    }
}
