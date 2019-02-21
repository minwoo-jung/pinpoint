import { NgModule } from '@angular/core';

import { ConfigurationUserInfoContainerComponent } from './configuration-user-info-container.component';
import { SharedModule } from 'app/shared';
import { UserProfileModule } from 'app/core/components/user-profile';
import { UserPasswordModule } from 'app/core/components/user-password';

@NgModule({
    declarations: [
        ConfigurationUserInfoContainerComponent
    ],
    imports: [
        SharedModule,
        UserProfileModule,
        UserPasswordModule,
    ],
    exports: [],
    entryComponents: [
        ConfigurationUserInfoContainerComponent
    ],
    providers: [],
})
export class ConfigurationUserInfoModule { }
