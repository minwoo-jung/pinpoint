import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ConfirmRemoveUserContainerComponent } from './confirm-remove-user-container.component';

@NgModule({
    declarations: [
        ConfirmRemoveUserContainerComponent,
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ConfirmRemoveUserContainerComponent
    ],
    providers: [],
})
export class ConfirmRemoveUserModule { }
