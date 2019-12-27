
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { RoleListComponent } from './role-list.component';
import { RoleListContainerComponent } from './role-list-container.component';
import { RoleListDataService } from './role-list-data.service';
import { RoleListForUsersComponent } from './role-list-for-users.component';
import { RoleListContainerForUsersComponent } from './role-list-container-for-users.component';
import { RoleListInteractionService } from './role-list-interaction.service';
import { RoleListContainerForGeneralComponent } from './role-list-container-for-general.component';

@NgModule({
    declarations: [
        RoleListComponent,
        RoleListContainerComponent,
        RoleListForUsersComponent,
        RoleListContainerForUsersComponent,
        RoleListContainerForGeneralComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        RoleListContainerComponent,
        RoleListContainerForUsersComponent,
        RoleListContainerForGeneralComponent
    ],
    providers: [
        RoleListDataService,
        RoleListInteractionService
    ]
})
export class RoleListModule { }
