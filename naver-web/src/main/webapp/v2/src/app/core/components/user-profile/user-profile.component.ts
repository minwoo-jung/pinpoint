import { Component, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { filterObj } from 'app/core/utils/util';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IChangedProfileState } from './user-profile-interaction.service';

export const enum MinLength {
    USER_ID = 3,
    NAME = 3
}

@Component({
    selector: 'pp-user-profile',
    templateUrl: './user-profile.component.html',
    styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit, OnChanges {
    @Input() userProfile: IUserProfile;
    @Input() hasUserEditPerm: boolean;
    @Input() fieldErrorMessage: { [key: string]: IFormFieldErrorType };
    @Input() fieldLabel: { [key: string]: string };
    @Output() outUserProfileChange = new EventEmitter<IChangedProfileState>();

    userProfileForm = new FormGroup({
        userId: new FormControl('', [
            Validators.required,
            Validators.minLength(MinLength.USER_ID)
        ]),
        name: new FormControl('', [
            Validators.required,
            Validators.minLength(MinLength.NAME)
        ]),
        department: new FormControl(''),
        phoneNumber: new FormControl('', [
            Validators.pattern(/\d*/)
        ]),
        email: new FormControl('', [
            Validators.email
        ])
    });

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const userProfileChange = changes['userProfile'];
        const { currentValue }: { currentValue: IUserProfile } = userProfileChange;

        const profileFormattedObj = currentValue
            ? filterObj((key: string) => Object.keys(this.userProfileForm.controls).includes(key), currentValue)
            : {};

        this.userProfileForm.reset(profileFormattedObj);
    }

    onKeyUp(): void {
        this.userProfileForm.valid
            ? this.outUserProfileChange.emit({ isValid: true, profile: this.userProfileForm.value })
            : this.outUserProfileChange.emit({ isValid: false });
    }
}
