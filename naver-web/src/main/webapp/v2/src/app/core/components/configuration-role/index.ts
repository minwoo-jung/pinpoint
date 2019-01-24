import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { RoleListModule } from 'app/core/components/role-list';
import { RoleInfoModule } from 'app/core/components/role-info';
import { ConfigurationRoleContainerComponent } from './configuration-role-container.component';

@NgModule({
    declarations: [
        ConfigurationRoleContainerComponent
    ],
    imports: [
        SharedModule,
        RoleListModule,
        RoleInfoModule
    ],
    exports: [
        ConfigurationRoleContainerComponent
    ],
    providers: []
})
export class ConfigurationRoleModule { }
