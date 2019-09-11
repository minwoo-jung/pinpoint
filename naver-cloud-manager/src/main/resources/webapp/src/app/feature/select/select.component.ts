import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ReplaySubject } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';

@Component({
    selector: 'pp-select',
    templateUrl: './select.component.html',
    styleUrls: ['./select.component.css']
})
export class SelectComponent implements OnInit {
    @Input() selectLabel: string;
    @Input()
    set optionList(list: string[]) {
        this.originList = list;
        this.filteredOptList.next(list);
    }

    @Output() outSelect = new EventEmitter<string>();

    private originList: string[];

    searchCtrl = new FormControl();
    filteredOptList = new ReplaySubject<string[]>(1);

    constructor() {}
    ngOnInit() {
        this.searchCtrl.valueChanges.pipe(
            map((v: string) => this.filterList(this.originList, v))
        ).subscribe((list: string[]) => {
            this.filteredOptList.next(list);
        });
    }

    onSelect(value: string): void {
        this.outSelect.emit(value);
    }

    shouldDisabled(): boolean {
        return !this.originList;
    }

    getSearchLabel(): string {
        return `Search ${this.selectLabel}`;
    }

    private filterList(list: string[], value: string): string[] {
        return value ? list.filter((item: string) => item.toLowerCase().indexOf(value.toLowerCase()) > -1) : list.slice();
    }
}
