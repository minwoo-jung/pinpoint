import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-confirm-remove-user',
    templateUrl: './confirm-remove-user.component.html',
    styleUrls: ['./confirm-remove-user.component.css']
})
export class ConfirmRemoveUserComponent implements OnInit {
    @Input() userProfile: IUserProfile;

    constructor() {}
    ngOnInit() {}
}
