import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { DynamicPopup } from 'app/shared/services/dynamic-popup.service';

@Component({
    selector: 'pp-application-auth-and-alarm-popup',
    templateUrl: './application-auth-and-alarm-popup.component.html',
    styleUrls: ['./application-auth-and-alarm-popup.component.css']
})
export class ApplicationAuthAndAlarmPopupComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: any;
    @Input() coord: ICoordinate;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();

    constructor() {}

    ngOnInit() {}
    ngAfterViewInit() {
        this.outCreated.emit(this.coord);
    }
    onClickOutside(): void {
        this.outClose.emit();
    }
}
