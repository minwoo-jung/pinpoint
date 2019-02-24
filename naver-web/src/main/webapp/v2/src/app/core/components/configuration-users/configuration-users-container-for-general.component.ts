import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { tap, filter } from 'rxjs/operators';

import { UserConfigurationDataService } from 'app/shared/services';
import { ConfigurationUsersDataService, IUserInfo } from './configuration-users-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-configuration-users-container-for-general',
    templateUrl: './configuration-users-container-for-general.component.html',
    styleUrls: ['./configuration-users-container-for-general.component.css']
})
export class ConfigurationUsersContainerForGeneralComponent implements OnInit {
    userInfo$: Observable<IUserInfo | IServerErrorShortFormat>;
    errorMessage: string;

    constructor(
        private userConfigurationDataService: UserConfigurationDataService,
        private configurationUsersDataService: ConfigurationUsersDataService
    ) {}

    ngOnInit() {
        const userId = this.userConfigurationDataService.getUserId();

        this.userInfo$ = this.configurationUsersDataService.selectUser(userId).pipe(
            filter((data: IUserInfo | IServerErrorShortFormat) => {
                return isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                    ? (this.errorMessage = data.errorMessage, false)
                    : true
            })
        )
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
