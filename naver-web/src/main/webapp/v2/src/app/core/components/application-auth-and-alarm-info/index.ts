import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular/main';

import { SharedModule } from 'app/shared';
import { ApplicationAuthAndAlarmDataService } from './application-auth-and-alarm-data.service';
import { ApplicationAuthAndAlarmInfoComponent } from './application-auth-and-alarm-info.component';
import { ApplicationAuthAndAlarmInfoContainerComponent } from './application-auth-and-alarm-info-container.component';

@NgModule({
    declarations: [
        ApplicationAuthAndAlarmInfoComponent,
        ApplicationAuthAndAlarmInfoContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        ApplicationAuthAndAlarmInfoContainerComponent
    ],
    providers: [
        ApplicationAuthAndAlarmDataService
    ]
})
export class ApplicationAuthAndAlarmInfoModule { }
