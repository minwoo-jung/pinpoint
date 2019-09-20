import { Component, OnInit, Input } from '@angular/core';

import { Span } from 'app/service/app-data.service';

@Component({
    selector: 'pp-result',
    templateUrl: './result.component.html',
    styleUrls: ['./result.component.css']
})
export class ResultComponent implements OnInit {
    @Input() data: Span[];

    displayedColumns = ['x', 'y'];

    constructor() {}
    ngOnInit() {}
    getTotalCount(): number {
        return this.data ? this.data.reduce((acc: number, {y}: Span) => y + acc, 0) : 0;
    }
}
