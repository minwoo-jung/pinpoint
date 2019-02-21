import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { UserPasswordContainerComponent } from './user-password-container.component';
import { UserPasswordComponent } from './user-password.component';
import { SharedModule } from 'app/shared';
import { UserPasswordInteractionService } from './user-password-interaction.service';
import { UserPasswordDataService } from './user-password-data.service';

@NgModule({
    declarations: [
        UserPasswordContainerComponent,
        UserPasswordComponent
    ],
    imports: [
        ReactiveFormsModule,
        SharedModule
    ],
    exports: [
        UserPasswordContainerComponent
    ],
    providers: [
        UserPasswordInteractionService,
        UserPasswordDataService
    ],
})
export class UserPasswordModule { }
