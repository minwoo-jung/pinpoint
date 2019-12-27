
import { NgModule } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { SharedModule } from 'app/shared';
import { RoleInfoComponent } from './role-info.component';
import { RoleInfoContainerComponent } from './role-info-container.component';
import { RoleInfoDataService } from './role-info-data.service';

@NgModule({
    declarations: [
        RoleInfoComponent,
        RoleInfoContainerComponent
    ],
    imports: [
        MatCheckboxModule,
        MatRadioModule,
        SharedModule
    ],
    exports: [
        RoleInfoContainerComponent
    ],
    providers: [
        RoleInfoDataService
    ]
})
export class RoleInfoModule { }
