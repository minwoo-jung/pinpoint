import { Component, OnInit, Input } from '@angular/core';

import { Span } from 'app/service/app-data.service';

@Component({
    selector: 'pp-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {
    @Input() data: Span[];

    displayedColumns = ['x', 'y'];

    constructor() {}
    ngOnInit() {}
    getTotalCount(): number {
        return this.data.reduce((acc: number, {y}: Span) => y + acc, 0);
    }
}
