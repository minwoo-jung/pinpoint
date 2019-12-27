import { POSITION } from 'app/core/components/authentication-list/authentication-data.service';
import { Component, OnInit, OnChanges, Input, Output, EventEmitter } from '@angular/core';

import { IApplicationAuthData } from './authentication-data.service';

export interface IParam {
    userGroupId: string;
    position?: string;
    applicationId?: string;
}
@Component({
    selector: 'pp-authentication-list',
    templateUrl: './authentication-list.component.html',
    styleUrls: ['./authentication-list.component.css']
})
export class AuthenticationListComponent implements OnInit, OnChanges {
    @Input() authorityList: IApplicationAuthData[];
    @Input() hasUpdateAndRemoveAuthority: boolean;
    @Input() i18nLabel: {[key: string]: string};
    @Output() outRemove = new EventEmitter<IParam>();
    @Output() outEdit = new EventEmitter<IParam>();
    @Output() outInfo = new EventEmitter<IParam>();

    private removeConfirmId = '';
    private removeConfirmApplicationId = '';

    selectedUserGroupId: string;
    selectedPosition: string;

    constructor() { }
    ngOnInit() {}
    ngOnChanges() {
        this.initSelected();
    }

    initSelected() {
        this.selectedUserGroupId = '';
        this.selectedPosition = '';
    }

    onRemove(userGroupId: string, applicationId: string): void {
        this.removeConfirmId = userGroupId;
        this.removeConfirmApplicationId = applicationId;
    }

    onEdit(userGroupId: string): void {
        this.outEdit.emit({userGroupId});
    }

    onCancelRemove(): void {
        this.removeConfirmId = '';
        this.removeConfirmApplicationId = '';
    }

    onConfirmRemove(): void {
        this.outRemove.emit({
            userGroupId: this.removeConfirmId,
            applicationId: this.removeConfirmApplicationId
        });
        this.removeConfirmId = '';
        this.removeConfirmApplicationId = '';
    }

    toggleAuthInfo(userGroupId: string, position: string): void {
        if (this.selectedUserGroupId === userGroupId && this.selectedPosition === position) {
            this.initSelected();
        } else {
            this.selectedUserGroupId = userGroupId;
            this.selectedPosition = position;
        }
        this.outInfo.emit({userGroupId, position});
    }

    isShowAuthInfo(userGroupId: string, position: string): boolean {
        return this.selectedUserGroupId === userGroupId && this.selectedPosition === position;
    }

    isRemoveTarget(userGroupId: string, applicationId: string): boolean {
        return this.removeConfirmId === userGroupId && this.removeConfirmApplicationId === applicationId;
    }

    isGuest(position: string): boolean {
        return position === POSITION.GUEST;
    }
}
