import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ScrollingModule } from '@angular/cdk/scrolling';

import { SharedModule } from 'app/shared';
import { PinpointUserComponent } from './pinpoint-user.component';
import { PinpointUserCreateAndUpdateComponent } from './pinpoint-user-create-and-update.component';
import { PinpointUserContainerComponent } from './pinpoint-user-container.component';
import { PinpointUserInteractionService } from './pinpoint-user-interaction.service';
import { PinpointUserDataService } from './pinpoint-user-data.service';
import { PinpointUserContainerForUsersComponent } from './pinpoint-user-container-for-users.component';
import { PinpointUserForUsersComponent } from './pinpoint-user-for-users.component';
import { PinpointUserForUsersDataService } from './pinpoint-user-for-users-data.service';

@NgModule({
    declarations: [
        PinpointUserComponent,
        PinpointUserCreateAndUpdateComponent,
        PinpointUserContainerComponent,
        PinpointUserContainerForUsersComponent,
        PinpointUserForUsersComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        ScrollingModule,
        SharedModule
    ],
    exports: [
        PinpointUserContainerComponent,
        PinpointUserContainerForUsersComponent
    ],
    entryComponents: [
        PinpointUserComponent,
        PinpointUserContainerComponent
    ],
    providers: [
        PinpointUserInteractionService,
        PinpointUserDataService,
        PinpointUserForUsersDataService
    ]
})
export class PinpointUserModule { }
