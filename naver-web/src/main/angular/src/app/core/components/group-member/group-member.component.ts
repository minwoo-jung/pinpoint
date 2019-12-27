import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';
import { IGroupMember } from './group-member-data.service';

@Component({
    selector: 'pp-group-member',
    templateUrl: './group-member.component.html',
    styleUrls: ['./group-member.component.css']
})
export class GroupMemberComponent implements OnInit, OnChanges {
    @Input() currentUserId: string;
    @Input() canRemoveAllGroupMember: boolean;
    @Input() canRemoveAllGroupMemberExceptMe: boolean;
    @Input() groupMemberList: IGroupMember[];
    @Output() outRemove: EventEmitter<string> = new EventEmitter();
    private includeMe = false;
    private removeConformId = '';
    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        this.includeMe = false;
        this.groupMemberList.forEach((groupMember: IGroupMember) => {
            this.includeMe = this.includeMe || (this.currentUserId === groupMember.memberId);
        });
    }
    onRemove(id: string): void {
        this.removeConformId = id;
    }
    onCancelRemove(): void {
        this.removeConformId = '';
    }
    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConformId);
        this.removeConformId = '';
    }
    canNotRemove(id: string): boolean {
        if (this.canRemoveAllGroupMember) {
            return false;
        } else if (this.canRemoveAllGroupMemberExceptMe) {
            if (this.includeMe) {
                return this.currentUserId === id;
            }
        }
        return true;
    }
    isRemoveTarget(id: string): boolean {
        return this.removeConformId === id;
    }
}
