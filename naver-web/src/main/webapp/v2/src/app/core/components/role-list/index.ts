
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { RoleListComponent } from './role-list.component';
import { RoleListContainerComponent } from './role-list-container.component';
import { RoleListDataService } from './role-list-data.service';

@NgModule({
    declarations: [
        RoleListComponent,
        RoleListContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        RoleListContainerComponent
    ],
    providers: [
        RoleListDataService
    ]
})
export class RoleListModule { }
