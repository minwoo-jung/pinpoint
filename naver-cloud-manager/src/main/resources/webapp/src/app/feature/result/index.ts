import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';

import { ResultComponent } from './result.component';

@NgModule({
    imports: [
        CommonModule,
        MatTableModule
    ],
    exports: [
        ResultComponent
    ],
    declarations: [
        ResultComponent
    ],
    providers: [],
})
export class ResultModule { }
