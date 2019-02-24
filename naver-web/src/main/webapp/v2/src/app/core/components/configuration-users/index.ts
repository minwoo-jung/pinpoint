import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { PinpointUserModule } from 'app/core/components/pinpoint-user';
import { ConfigurationUsersContainerComponent } from './configuration-users-container.component';
import { ConfigurationUsersDataService } from './configuration-users-data.service';
import { ConfigurationUserInfoModule } from 'app/core/components/configuration-user-info';
import { ConfirmRemoveUserModule } from 'app/core/components/confirm-remove-user';
import { ConfigurationUsersContainerForGeneralComponent } from './configuration-users-container-for-general.component';

@NgModule({
    declarations: [
        ConfigurationUsersContainerComponent,
        ConfigurationUsersContainerForGeneralComponent
    ],
    imports: [
        SharedModule,
        PinpointUserModule,
        ConfigurationUserInfoModule,
        ConfirmRemoveUserModule
    ],
    exports: [
        ConfigurationUsersContainerComponent,
        ConfigurationUsersContainerForGeneralComponent
    ],
    providers: [
        ConfigurationUsersDataService
    ]
})
export class ConfigurationUsersModule { }
