import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular';

import { SharedModule } from 'app/shared';
import { ApplicationAuthAndAlarmDataService } from './application-auth-and-alarm-data.service';
import { ApplicationAuthAndAlarmInfoComponent } from './application-auth-and-alarm-info.component';
import { ApplicationAuthAndAlarmInfoContainerComponent } from './application-auth-and-alarm-info-container.component';
import { ApplicationAuthAndAlarmPopupComponent } from './application-auth-and-alarm-popup.component';

@NgModule({
    declarations: [
        ApplicationAuthAndAlarmInfoComponent,
        ApplicationAuthAndAlarmInfoContainerComponent,
        ApplicationAuthAndAlarmPopupComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        ApplicationAuthAndAlarmInfoContainerComponent
    ],
    entryComponents: [
        ApplicationAuthAndAlarmPopupComponent
    ],
    providers: [
        ApplicationAuthAndAlarmDataService
    ]
})
export class ApplicationAuthAndAlarmInfoModule { }
