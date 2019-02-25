import { Component, OnInit, Input, Output, EventEmitter, TemplateRef, HostBinding } from '@angular/core';

@Component({
    selector: 'pp-role-list-for-users',
    templateUrl: './role-list-for-users.component.html',
    styleUrls: ['./role-list-for-users.component.css']
})
export class RoleListForUsersComponent implements OnInit {
    @HostBinding('class.font-opensans') fontFamily = true;
    @Input() roleList: string[];
    @Input() emptyText: string;
    @Input() buttonTemplate: TemplateRef<any>;
    @Output() outSelectRole = new EventEmitter<string>();

    listItemStyle: { [key: string]: string };

    constructor() {}
    ngOnInit() {
        this.listItemStyle = this.buttonTemplate
            ? { padding: '10px 15px 10px 20px' }
            : { padding: '10px 0', 'text-align': 'center' };
    }
    onClickRole(role: string): void {
        this.outSelectRole.emit(role);
    }

    isListEmpty(): boolean {
        return this.roleList.length === 0;
    }
}
