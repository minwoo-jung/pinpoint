import { Component, OnInit, Input } from '@angular/core';

import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';

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
