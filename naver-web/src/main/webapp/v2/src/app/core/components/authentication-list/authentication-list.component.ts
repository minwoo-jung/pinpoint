import { Component, OnInit, OnChanges, Input, Output, EventEmitter } from '@angular/core';

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
    @Input() authorityList: any;
    @Input() hasUpdateAndRemoveAuthority: boolean;
    @Input() i18nLabel: any;
    @Output() outRemove: EventEmitter<IParam> = new EventEmitter();
    @Output() outEdit: EventEmitter<IParam> = new EventEmitter();
    @Output() outInfo: EventEmitter<IParam> = new EventEmitter();
    private removeConformId = '';
    private removeConformApplicationId = '';
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
        this.removeConformId = userGroupId;
        this.removeConformApplicationId = applicationId;
    }
    onEdit(userGroupId: string, position: string): void {
        this.outEdit.emit({ userGroupId, position});
    }
    onCancelRemove(): void {
        this.removeConformId = '';
        this.removeConformApplicationId = '';
    }
    onConfirmRemove(): void {
        this.outRemove.emit({
            userGroupId: this.removeConformId,
            applicationId: this.removeConformApplicationId
        });
        this.removeConformId = '';
        this.removeConformApplicationId = '';
    }
    toggleAuthInfo(userGroupId: string, position: string): void {
        if (this.selectedUserGroupId === userGroupId && this.selectedPosition === position) {
            this.initSelected();
        } else {
            this.selectedUserGroupId = userGroupId;
            this.selectedPosition = position;
        }
        this.outInfo.emit({ userGroupId, position });
    }
    isShowAuthInfo(userGroupId: string, position: string): boolean {
        return this.selectedUserGroupId === userGroupId && this.selectedPosition === position;
    }
    isRemoveTarget(userGroupId: string, applicationId: string): boolean {
        return this.removeConformId === userGroupId && this.removeConformApplicationId === applicationId;
    }
}
