import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-command-group',
    templateUrl: './command-group.component.html',
    styleUrls: ['./command-group.component.css']
})
export class CommandGroupComponent implements OnInit {
    @Input() hasNewNotice = false;
    @Output() outOpenConfigurationPopup: EventEmitter<null> = new EventEmitter();
    @Output() outOpenYobi: EventEmitter<null> = new EventEmitter();

    constructor() {}
    ngOnInit() {}
    onOpenConfigurationPopup(): void {
        this.outOpenConfigurationPopup.emit();
    }
    onOpenYobi(): void {
        this.outOpenYobi.emit();
    }
}
