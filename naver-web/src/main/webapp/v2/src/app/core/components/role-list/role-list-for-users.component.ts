import { Component, OnInit, Input, Output, EventEmitter, TemplateRef, HostBinding } from '@angular/core';

@Component({
    selector: 'pp-role-list-for-users',
    templateUrl: './role-list-for-users.component.html',
    styleUrls: ['./role-list-for-users.component.css']
})
export class RoleListForUsersComponent implements OnInit {
    @HostBinding('class.font-opensans') fontFamily = true;
    @Input() roleList: string[];
    @Input() hasUserEditPerm: boolean;
    @Input() emptyText: string;
    @Input() iconTemplate: TemplateRef<any>;
    @Output() outSelectRole = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    onClickRole(role: string): void {
        this.outSelectRole.emit(role);
    }

    isListEmpty(): boolean {
        return this.roleList.length === 0;
    }
}
