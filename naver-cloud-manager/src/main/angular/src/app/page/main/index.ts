import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { MainPageRoutingModule } from './main-page-routing.module';
import { MainPageComponent } from './main-page.component';
import { SelectModule } from 'app/feature/select';
import { PeriodModule } from 'app/feature/period';
import { TableModule } from 'app/feature/table';
import { ChartModule } from 'app/feature/chart';

@NgModule({
    imports: [
        CommonModule,
        MainPageRoutingModule,
        MatButtonModule,
        MatSnackBarModule,
        MatCardModule,
        MatProgressSpinnerModule,
        SelectModule,
        PeriodModule,
        TableModule,
        ChartModule
    ],
    exports: [],
    declarations: [MainPageComponent],
    providers: [
        {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 5000}}
    ],
})
export class MainPageModule { }
