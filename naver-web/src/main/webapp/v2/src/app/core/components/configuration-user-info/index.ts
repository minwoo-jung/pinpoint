import { NgModule } from '@angular/core';

import { ConfigurationUserInfoContainerComponent } from './configuration-user-info-container.component';
import { SharedModule } from 'app/shared';
import { UserProfileModule } from 'app/core/components/user-profile';
import { UserPasswordModule } from 'app/core/components/user-password';
import { RoleListModule } from 'app/core/components/role-list';
import { ConfigurationUserInfoInteractionService } from './configuration-user-info-interaction.service';
import { ConfigurationUserInfoDataService } from './configuration-user-info-data.service';

@NgModule({
    declarations: [
        ConfigurationUserInfoContainerComponent
    ],
    imports: [
        SharedModule,
        UserProfileModule,
        UserPasswordModule,
        RoleListModule
    ],
    exports: [],
    entryComponents: [
        ConfigurationUserInfoContainerComponent
    ],
    providers: [
        ConfigurationUserInfoInteractionService,
        ConfigurationUserInfoDataService
    ],
})
export class ConfigurationUserInfoModule { }
