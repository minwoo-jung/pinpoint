import { Component, OnInit, Inject } from '@angular/core';

@Component({
    selector: 'pp-confirm-remove-user-container',
    templateUrl: './confirm-remove-user-container.component.html',
    styleUrls: ['./confirm-remove-user-container.component.css']
})
export class ConfirmRemoveUserContainerComponent implements OnInit {
    constructor(
        @Inject('userInfo') public userInfo: any,
    ) {}

    ngOnInit() {}
}
