import { NgModule } from '@angular/core';
import { MatTableModule } from '@angular/material/table';

import { ResultComponent } from './result.component';

@NgModule({
    imports: [
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
