import { NgModule } from '@angular/core';

import { ConfigurationUserInfoContainerComponent } from './configuration-user-info-container.component';
import { SharedModule } from 'app/shared';

@NgModule({
    declarations: [
        ConfigurationUserInfoContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ConfigurationUserInfoContainerComponent
    ],
    providers: [],
})
export class ConfigurationUserInfoModule { }
