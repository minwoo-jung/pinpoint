import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentStatisticModule } from 'app/core/components/agent-statistic';
import { ConfigurationAgentStatisticContainerComponent } from './configuration-agent-statistic-container.component';

@NgModule({
    declarations: [
        ConfigurationAgentStatisticContainerComponent
    ],
    imports: [
        SharedModule,
        AgentStatisticModule
    ],
    exports: [
        ConfigurationAgentStatisticContainerComponent
    ],
    providers: []
})
export class ConfigurationAgentStatisticModule { }
