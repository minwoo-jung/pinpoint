import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

interface IParam {
    userGroupId: string;
    role?: string;
    applicationId?: string;
}

@Component({
    selector: 'pp-authentication-list',
    templateUrl: './authentication-list.component.html',
    styleUrls: ['./authentication-list.component.css']
})
export class AuthenticationListComponent implements OnInit {
    @Input() authorityList: any;
    @Input() hasUpdateAndRemoveAuthority: boolean;
    @Output() outRemove: EventEmitter<IParam> = new EventEmitter();
    @Output() outEdit: EventEmitter<IParam> = new EventEmitter();
    @Output() outInfo: EventEmitter<IParam> = new EventEmitter();
    private removeConformId = '';
    private removeConformApplicationId = '';
    constructor() { }
    ngOnInit() {}
    onRemove(userGroupId: string, applicationId: string): void {
        this.removeConformId = userGroupId;
        this.removeConformApplicationId = applicationId;
    }
    onEdit(userGroupId: string, role: string): void {
        this.outEdit.emit({ userGroupId, role});
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
    onShowInfo(userGroupId: string, role: string): void {
        this.outInfo.emit({ userGroupId, role });
    }
    isRemoveTarget(userGroupId: string, applicationId: string): boolean {
        return this.removeConformId === userGroupId && this.removeConformApplicationId === applicationId;
    }
}
