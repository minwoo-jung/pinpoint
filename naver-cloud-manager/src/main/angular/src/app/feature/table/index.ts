import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';

import { TableComponent } from './table.component';

@NgModule({
    imports: [
        CommonModule,
        MatTableModule
    ],
    exports: [
        TableComponent
    ],
    declarations: [
        TableComponent
    ],
    providers: [],
})
export class TableModule { }
