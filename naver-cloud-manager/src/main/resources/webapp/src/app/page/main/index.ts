import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

import { MainPageRoutingModule } from './main-page-routing.module';
import { MainPageComponent } from './main-page.component';
import { SelectModule } from 'app/feature/select';
import { PeriodModule } from 'app/feature/period';
import { ResultModule } from 'app/feature/result';

@NgModule({
    imports: [
        CommonModule,
        MainPageRoutingModule,
        MatButtonModule,
        MatDividerModule,
        SelectModule,
        PeriodModule,
        ResultModule,
    ],
    exports: [],
    declarations: [MainPageComponent],
    providers: [],
})
export class MainPageModule { }
