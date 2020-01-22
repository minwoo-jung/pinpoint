import { Component, OnInit, Input, Output, EventEmitter, ElementRef } from '@angular/core';
import { fromEvent } from 'rxjs';
import { pluck } from 'rxjs/operators';

@Component({
    selector: 'pp-pinpoint-user-for-users',
    templateUrl: './pinpoint-user-for-users.component.html',
    styleUrls: ['./pinpoint-user-for-users.component.css']
})
export class PinpointUserForUsersComponent implements OnInit {
    @Input() pinpointUser: IUserProfile;
    @Input() loggedInUserId: string;
    @Input() hasUserEditPerm: boolean;
    @Output() outSelect = new EventEmitter<string>();
    @Output() outRemove = new EventEmitter<string>();

    constructor(
        private el: ElementRef
    ) {}

    ngOnInit() {
        fromEvent(this.el.nativeElement, 'click').pipe(
            pluck('target', 'id')
        ).subscribe((id: string) => {
            const userId = this.pinpointUser.userId;

            id === 'removeBtn' ? this.outRemove.emit(userId) : this.outSelect.emit(userId);
        });
    }

    onRemove(userId: string): void {
        this.outRemove.emit(userId);
    }

    isUserMe(userId: string): boolean {
        return userId === this.loggedInUserId;
    }
}
