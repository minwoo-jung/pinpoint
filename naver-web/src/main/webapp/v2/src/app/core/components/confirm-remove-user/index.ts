import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfirmRemoveUserContainerComponent } from './confirm-remove-user-container.component';
import { ConfirmRemoveUserComponent } from './confirm-remove-user.component';
import { ConfirmRemoveUserInteractionService } from './confirm-remove-user-interaction.service';
import { ConfirmRemoveUserDataService } from './confirm-remove-user-data.service';

@NgModule({
    declarations: [
        ConfirmRemoveUserContainerComponent,
        ConfirmRemoveUserComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ConfirmRemoveUserContainerComponent
    ],
    providers: [
        ConfirmRemoveUserInteractionService,
        ConfirmRemoveUserDataService
    ],
})
export class ConfirmRemoveUserModule { }
