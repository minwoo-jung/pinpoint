import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfigurationRoleContainerComponent } from './configuration-role-container.component';

@NgModule({
    declarations: [
        ConfigurationRoleContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        ConfigurationRoleContainerComponent
    ],
    providers: []
})
export class ConfigurationRoleModule { }
