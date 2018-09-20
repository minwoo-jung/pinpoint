import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

export class Authentication {
    public applicationId: string;
    constructor(
        public role: string,
        public userGroupId: string,
        public serverMap: boolean,
        public apiMeta: boolean,
        public paramMeta: boolean,
        public sqlMeta: boolean
    ) {
    }
}

@Component({
    selector: 'pp-authentication-create-and-update',
    templateUrl: './authentication-create-and-update.component.html',
    styleUrls: ['./authentication-create-and-update.component.css']
})
export class AuthenticationCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() showCreate: boolean;
    @Input() userGroupList: string[];
    @Input() fixRole: string;
    @Input() i18nLabel: any;
    @Input() i18nGuide: any;
    @Input() editAuth: Authentication = null;
    @Output() outUpdateAuth: EventEmitter<Authentication> = new EventEmitter();
    @Output() outCreateAuth: EventEmitter<Authentication> = new EventEmitter();
    @Output() outClose: EventEmitter<null> = new EventEmitter();
    roleList = ['manager', 'user'];
    newAuthModel = new Authentication('', '', false, false, false, false);
    authForm: FormGroup;
    title = 'Authentication';

    constructor() {}
    ngOnInit() {
        this.authForm = new FormGroup({
            'role': new FormControl(this.newAuthModel.role, [
                Validators.required
            ]),
            'userGroupId': new FormControl(this.newAuthModel.userGroupId, [
                Validators.required
            ]),
            'serverMap': new FormControl(this.newAuthModel.serverMap, []),
            'apiMeta': new FormControl(this.newAuthModel.apiMeta, []),
            'paramMeta': new FormControl(this.newAuthModel.paramMeta, []),
            'sqlMeta': new FormControl(this.newAuthModel.sqlMeta, [])
        });
    }
    ngOnChanges(changes: SimpleChanges) {
        if (changes['showCreate'] && changes['showCreate'].currentValue === true) {
            this.setValue(this.fixRole, '', false, false, false, false);
            if (this.fixRole !== '') {
                this.authForm.get('role').disable();
            }
        }
        if (changes['editAuth'] && changes['editAuth'].currentValue) {
            this.setValue(
                this.editAuth.role,
                this.editAuth.userGroupId,
                this.editAuth.serverMap,
                this.editAuth.apiMeta,
                this.editAuth.paramMeta,
                this.editAuth.sqlMeta
            );
            if (this.fixRole === 'manager' || this.fixRole === 'guest') {
                this.authForm.get('userGroupId').disable();
                this.authForm.get('role').disable();
            }
        }
    }
    private setValue(role: string, userGroupId: string, serverMap: boolean, apiMeta: boolean, paramMeta: boolean, sqlMeta: boolean): void {
        this.authForm.get('role').setValue(role);
        this.authForm.get('userGroupId').setValue(userGroupId);
        this.authForm.get('serverMap').setValue(serverMap);
        this.authForm.get('apiMeta').setValue(apiMeta);
        this.authForm.get('paramMeta').setValue(paramMeta);
        this.authForm.get('sqlMeta').setValue(sqlMeta);
    }
    onCreateOrUpdate() {
        const auth = new Authentication(
            this.authForm.get('role').value,
            this.authForm.get('userGroupId').value,
            this.authForm.get('serverMap').value,
            this.authForm.get('apiMeta').value,
            this.authForm.get('paramMeta').value,
            this.authForm.get('sqlMeta').value
        );
        if (this.editAuth) {
            this.outUpdateAuth.emit(auth);
        } else {
            this.outCreateAuth.emit(auth);
        }
        this.onClose();
    }
    onClose() {
        this.editAuth = null;
        this.outClose.emit();
        this.authForm.get('role').enable();
        this.authForm.get('userGroupId').enable();
        this.authForm.reset();
    }
    get role() {
        return this.authForm.get('role');
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
