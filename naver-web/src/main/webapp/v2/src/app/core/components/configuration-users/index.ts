import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { PinpointUserModule } from 'app/core/components/pinpoint-user';
import { ConfigurationUsersContainerComponent } from './configuration-users-container.component';

@NgModule({
    declarations: [
        ConfigurationUsersContainerComponent
    ],
    imports: [
        SharedModule,
        PinpointUserModule,
    ],
    exports: [
        ConfigurationUsersContainerComponent
    ],
    providers: []
})
export class ConfigurationUsersModule { }
