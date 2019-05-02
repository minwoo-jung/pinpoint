import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationUsersModule } from 'app/core/components/configuration-users';
import { ConfigurationUserProfileContainerComponent } from './configuration-user-profile-container.component';

@NgModule({
    declarations: [
        ConfigurationUserProfileContainerComponent
    ],
    imports: [
        SharedModule,
        ConfigurationUsersModule
    ],
    exports: [
        ConfigurationUserProfileContainerComponent
    ],
    providers: []
})
export class ConfigurationUserProfileModule { }
