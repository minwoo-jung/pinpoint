import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ReplaySubject } from 'rxjs';
import { map } from 'rxjs/operators';

export enum SelectType {
    ORG = 'org',
    APP = 'app',
    AGENT = 'agent'
}

@Component({
    selector: 'pp-select',
    templateUrl: './select.component.html',
    styleUrls: ['./select.component.css']
})
export class SelectComponent implements OnInit {
    @Input() type: SelectType;
    @Input()
    set optionList(list: string[]) {
        if (this.originList) {
            // When a new list is given, reset the select form.
            this.selectCtrl.reset();
            this.outSelect.emit('');
        }

        // Enable the select form after getting the list.
        list ? this.selectCtrl.enable() : this.selectCtrl.disable();

        this.originList = list;
        this.filteredOptList.next(list);
    }

    @Output() outSelect = new EventEmitter<string>();

    private originList: string[];

    selectType = SelectType;
    selectCtrl = new FormControl({value: '', disabled: true});
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
        this.outSelect.emit(value ? value : '');
    }

    getSelectLabel(): string {
        return this.type === SelectType.ORG ? 'Organization'
            : this.type === SelectType.APP ? 'Application'
            : 'Agent';
    }

    getSearchLabel(): string {
        return `Search ${this.getSelectLabel()}`;
    }

    isTypeOrg(): boolean {
        return this.type === SelectType.ORG;
    }

    private filterList(list: string[], value: string): string[] {
        return value ? list.filter((item: string) => item.toLowerCase().indexOf(value.toLowerCase()) > -1) : list.slice();
    }
}
