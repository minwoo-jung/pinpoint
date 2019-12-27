import { Component, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { filterObj } from 'app/core/utils/util';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
import { IChangedProfileState } from './user-profile-interaction.service';
import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';

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
            CustomFormValidatorService.validate(/^[a-z0-9\_\-]{4,24}$/)
        ]),
        name: new FormControl('', [
            Validators.required,
            CustomFormValidatorService.validate(/^[\w\-\.ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{1,30}$/)
        ]),
        department: new FormControl('', [
            CustomFormValidatorService.validate(/^[\w\.\-ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{3,40}$/)
        ]),
        phoneNumber: new FormControl('', [
            CustomFormValidatorService.validate(/^[\d]{3,24}$/)
        ]),
        email: new FormControl('', [
            Validators.minLength(3),
            Validators.maxLength(60),
            CustomFormValidatorService.validate(/^[A-Za-z0-9\.\_\-]+@[A-Za-z0-9\.\-]+\.[A-Za-z]+$/)
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
