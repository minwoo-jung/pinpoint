import { Component, OnInit, Input } from '@angular/core';

import { IUserInfo } from 'app/core/components/configuration-users/configuration-users-data.service';
import { UserType } from 'app/core/components/user-password/user-password-container.component';

@Component({
    selector: 'pp-configuration-user-info-container-for-general',
    templateUrl: './configuration-user-info-container-for-general.component.html',
    styleUrls: ['./configuration-user-info-container-for-general.component.css']
})
export class ConfigurationUserInfoContainerForGeneralComponent implements OnInit {
    @Input() userInfo: IUserInfo;
    hasUserEditPerm = true;
    loggedInUserType = UserType.ELSE;

    constructor() {}
    ngOnInit() {}
}
