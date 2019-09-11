import { Component, OnInit, Input, ViewChild, Renderer2, ElementRef } from '@angular/core';
import { NgxTimepickerFieldComponent } from 'ngx-material-timepicker';
import { MatDatepicker } from '@angular/material/datepicker';

export const DATE_FORMAT = {
    parse: {
        dateInput: 'YYYY.MM.DD',
    },
    display: {
        dateInput: 'YYYY.MM.DD',
        monthYearLabel: 'YYYY',
        dateA11yLabel: 'LL',
        monthYearA11yLabel: 'YYYY',
    }
};

@Component({
    selector: 'pp-period',
    templateUrl: './period.component.html',
    styleUrls: ['./period.component.css']
})
export class PeriodComponent implements OnInit {
    @ViewChild('timePicker', {read: ElementRef, static: true}) timePicker: ElementRef;
    @Input() periodLabel: string;

    showTimePicker = false;

    constructor(
        private renderer: Renderer2
    ) {}

    ngOnInit() {}
    onCalendarOpen(): void {
        // const datePickerElem = document.querySelector('.mat-datepicker-content');

        // this.renderer.appendChild(datePickerElem, this.timePicker.nativeElement);
        // this.showTimePicker = true;
    }

    onCalendarClose(picker: MatDatepicker<Date>): void {
        // picker.open();
        // this.showTimePicker = false;
    }
}
