import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { POSITION, IApplicationAuthData } from './authentication-data.service';

export interface IAuthForm {
    position: string;
    userGroupId: string;
    serverMapData: boolean;
    apiMetaData: boolean;
    paramMetaData: boolean;
    sqlMetaData: boolean;
}

export interface ILabel {
    POSITION: string;
    USER_GROUP: string;
    SERVER_MAP: string;
    API_META: string;
    PARAM_META: string;
    SQL_META: string;
}

@Component({
    selector: 'pp-authentication-create-and-update',
    templateUrl: './authentication-create-and-update.component.html',
    styleUrls: ['./authentication-create-and-update.component.css']
})
export class AuthenticationCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() isFirstAuth: boolean;
    @Input() userGroupList: string[];
    @Input() selectedAuth: IApplicationAuthData;
    @Input() i18nLabel: ILabel;
    @Input() i18nFormGuide: {[key: string]: IFormFieldErrorType};
    @Output() outUpdateAuth = new EventEmitter<IAuthForm>();
    @Output() outCreateAuth = new EventEmitter<IAuthForm>();
    @Output() outClose = new EventEmitter<void>();

    fixPosition: string;
    positionList = [POSITION.MANAGER, POSITION.USER];
    authForm = new FormGroup({
        'position': new FormControl('', [Validators.required]),
        'userGroupId': new FormControl('', [Validators.required]),
        'serverMapData': new FormControl(false),
        'apiMetaData': new FormControl(false),
        'paramMetaData': new FormControl(false),
        'sqlMetaData': new FormControl(false)
    });

    constructor() {}
    ngOnInit() {
        if (!this.selectedAuth && this.isFirstAuth) {
            // When adding an auth for the first time
            this.authForm.patchValue({position: POSITION.MANAGER});
            this.authForm.get('position').disable();
        } else if (this.selectedAuth) {
            // When updating auth
            this.authForm.get('userGroupId').disable();
            if (this.selectedAuth.position === POSITION.GUEST) {
                this.authForm.get('position').disable();
            }
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        const authChange = changes['selectedAuth'];

        if (authChange && authChange.currentValue) {
            const {position, userGroupId, configuration} = authChange.currentValue;
            const formattedObj = {position, userGroupId, ...configuration};

            this.authForm.reset(formattedObj);
        }
    }

    onCreateOrUpdate() {
        const auth = this.authForm.getRawValue();

        this.selectedAuth ? this.outUpdateAuth.emit(auth) : this.outCreateAuth.emit(auth);
        this.onClose();
    }

    onClose() {
        this.outClose.emit();
    }

    isEdittingGuest(): boolean {
        return this.selectedAuth && this.selectedAuth.position === POSITION.GUEST;
    }
}
