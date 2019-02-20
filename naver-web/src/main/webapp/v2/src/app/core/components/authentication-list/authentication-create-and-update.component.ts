import { Component, OnInit, Input } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthenticationInteractionService, CMD_TYPE } from './authentication-interaction.service';
import { POSITION } from './authentication-data.service';

export interface IAuthorityForm {
    applicationId: string;
    userGroupList: string[];
    fixPosition: string;
    data?: IAuthorityData;
}

export interface IAuthorityCommandForm {
    type: string;
    data?: IAuthorityForm | IAuthorityData;
}

export interface IAuthorityData {
    applicationId: string;
    position: string;
    userGroupId: string;
    serverMap: boolean;
    apiMeta: boolean;
    paramMeta: boolean;
    sqlMeta: boolean;
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
export class AuthenticationCreateAndUpdateComponent implements OnInit {
    @Input() i18nLabel: ILabel;
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    title = 'Authority';
    showForm = false;
    fixPosition: string;
    positionList = [POSITION.MANAGER, POSITION.USER];
    userGroupList: string[];
    authForm: FormGroup;
    actionParam: IAuthorityCommandForm;

    constructor(
        private authenticationInteractionService: AuthenticationInteractionService
    ) {}
    ngOnInit() {
        this.authForm = new FormGroup({
            'position': new FormControl('', [Validators.required]),
            'userGroupId': new FormControl('', [Validators.required]),
            'serverMap': new FormControl(false, []),
            'apiMeta': new FormControl(false, []),
            'paramMeta': new FormControl(false, []),
            'sqlMeta': new FormControl(false, [])
        });
        this.authenticationInteractionService.onShowInput$.subscribe((param: IAuthorityCommandForm) => {
            this.actionParam = param;
            switch (param.type) {
                case CMD_TYPE.CREATE:
                    this.createForm(param.data as IAuthorityForm);
                    break;
                case CMD_TYPE.UPDATE:
                    this.updateForm(param.data as IAuthorityForm);
                    break;
            }
            this.showForm = true;
        });
    }
    private createForm(param: IAuthorityForm): void {
        this.userGroupList = param.userGroupList;
        this.setValue(param.fixPosition, '', false, false, false, false);
        if (param.fixPosition !== '') {
            this.fixPosition = param.fixPosition;
            this.authForm.get('position').disable();
        }
    }
    private updateForm(param: IAuthorityForm): void {
        const { position, userGroupId, serverMap, apiMeta, paramMeta, sqlMeta } = param.data;
        this.userGroupList = param.userGroupList;
        this.setValue(position, userGroupId, serverMap, apiMeta, paramMeta, sqlMeta);
        if (position === POSITION.MANAGER || position === POSITION.GUEST) {
            this.fixPosition = position;
            this.authForm.get('userGroupId').disable();
            this.authForm.get('position').disable();
        }
    }
    private setValue(position: string, userGroupId: string, serverMap: boolean, apiMeta: boolean, paramMeta: boolean, sqlMeta: boolean): void {
        this.authForm.get('position').setValue(position);
        this.authForm.get('userGroupId').setValue(userGroupId);
        this.authForm.get('serverMap').setValue(serverMap);
        this.authForm.get('apiMeta').setValue(apiMeta);
        this.authForm.get('paramMeta').setValue(paramMeta);
        this.authForm.get('sqlMeta').setValue(sqlMeta);
    }
    onCreateOrUpdate() {
        this.authenticationInteractionService.completeAction(this.actionParam.type, {
            applicationId: this.actionParam.data.applicationId,
            position: this.authForm.get('position').value,
            userGroupId: this.authForm.get('userGroupId').value,
            serverMap: this.authForm.get('serverMap').value,
            apiMeta: this.authForm.get('apiMeta').value,
            paramMeta: this.authForm.get('paramMeta').value,
            sqlMeta: this.authForm.get('sqlMeta').value
        });
        this.onClose();
    }
    onClose() {
        this.authForm.get('position').enable();
        this.authForm.get('userGroupId').enable();
        this.authForm.reset();
        this.showForm = false;
        this.authenticationInteractionService.closeInput();
    }
    isFixPositionGuest(): boolean {
        return this.fixPosition === POSITION.GUEST;
    }
    get position() {
        return this.authForm.get('position');
    }
    get userGroupId() {
        return this.authForm.get('userGroupId');
    }
    get serverMap() {
        return this.authForm.get('serverMap');
    }
    get apiMeta() {
        return this.authForm.get('apiMeta');
    }
    get paramMeta() {
        return this.authForm.get('paramMeta');
    }
    get sqlMeta() {
        return this.authForm.get('sqlMeta');
    }
}
