import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { UserProfileContainerComponent } from './user-profile-container.component';
import { UserProfileComponent } from './user-profile.component';
import { SharedModule } from 'app/shared';
import { UserProfileInteractionService } from './user-profile-interaction.service';
import { UserProfileDataService } from './user-profile-data.service';

@NgModule({
    declarations: [
        UserProfileContainerComponent,
        UserProfileComponent
    ],
    imports: [
        ReactiveFormsModule,
        SharedModule,
    ],
    exports: [
        UserProfileContainerComponent
    ],
    providers: [
        UserProfileInteractionService,
        UserProfileDataService
    ],
})
export class UserProfileModule { }
