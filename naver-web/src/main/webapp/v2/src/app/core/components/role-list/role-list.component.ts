import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-role-list',
    templateUrl: './role-list.component.html',
    styleUrls: ['./role-list.component.css']
})
export class RoleListComponent implements OnInit {
    @Input() roleList: string[];
    @Input() hasRoleEditPerm: boolean;
    @Output() outSelectRole: EventEmitter<string> = new EventEmitter();
    @Output() outSelectBtnRole: EventEmitter<string> = new EventEmitter();
    private selectedRole: string;
    constructor() {}
    ngOnInit() {}
    isSelected(id: string): boolean {
        return this.selectedRole === id;
    }
    onSelect(role: string): void {
        this.selectedRole = role;
        this.outSelectRole.emit(role);
    }
    onRemove($event: MouseEvent, role: string): void {
        this.selectedRole = role;
        this.outSelectBtnRole.emit(role);
        $event.stopPropagation();
    }
}
