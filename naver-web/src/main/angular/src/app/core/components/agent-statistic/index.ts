import { NgModule } from '@angular/core';
import { AgGridModule } from 'ag-grid-angular';
import { SharedModule } from 'app/shared';
import { AgentStatisticChartComponent } from './agent-statistic-chart.component';
import { AgentStatisticListComponent } from './agent-statistic-list.component';
import { AgentStatisticContainerComponent } from './agent-statistic-container.component';
import { AgentStatisticDataService } from './agent-statistic-data.service';

@NgModule({
    declarations: [
        AgentStatisticChartComponent,
        AgentStatisticListComponent,
        AgentStatisticContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        AgentStatisticContainerComponent
    ],
    providers: [
        AgentStatisticDataService
    ]
})
export class AgentStatisticModule { }
