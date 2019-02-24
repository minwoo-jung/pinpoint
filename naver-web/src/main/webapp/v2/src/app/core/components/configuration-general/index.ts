import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { InboundOutboundRangeSelectorModule } from 'app/core/components/inbound-outbound-range-selector';
import { SearchPeriodModule } from 'app/core/components/search-period';
import { ApplicationListModule } from 'app/core/components/application-list';
import { TimezoneModule } from 'app/core/components/timezone';
import { DateFormatModule } from 'app/core/components/date-format';
import { ConfigurationUsersModule } from 'app/core/components/configuration-users';
import { ConfigurationGeneralContainerComponent } from './configuration-general-container.component';

@NgModule({
    declarations: [
        ConfigurationGeneralContainerComponent
    ],
    imports: [
        SharedModule,
        InboundOutboundRangeSelectorModule,
        SearchPeriodModule,
        ApplicationListModule,
        TimezoneModule,
        DateFormatModule,
        ConfigurationUsersModule
    ],
    exports: [
        ConfigurationGeneralContainerComponent
    ],
    providers: []
})
export class ConfigurationGeneralModule { }
