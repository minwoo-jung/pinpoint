
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CommandGroupComponent } from './command-group.component';
import { CommandGroupContainerComponent } from './command-group-container.component';
import { YobiNoticeCheckService } from './yobi-notice-check.service';
import { ConfigurationPopupModule } from 'app/core/components/configuration-popup';

@NgModule({
    declarations: [
        CommandGroupComponent,
        CommandGroupContainerComponent
    ],
    imports: [
        CommonModule,
        ConfigurationPopupModule
    ],
    exports: [
        CommandGroupContainerComponent
    ],
    providers: [
        YobiNoticeCheckService
    ]
})
export class CommandGroupModule { }
