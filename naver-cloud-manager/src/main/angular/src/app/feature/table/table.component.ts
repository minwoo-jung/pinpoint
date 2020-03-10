import { Component, OnInit, Input } from '@angular/core';

import { Span } from 'app/service/app-data.service';

@Component({
    selector: 'pp-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {
    @Input() data: Span[];

    displayedColumns = ['timestamp', 'count'];

    constructor() {}
    ngOnInit() {}
    getTotalCount(): number {
        return this.data.reduce((acc: number, {count}: Span) => count + acc, 0);
    }
}
