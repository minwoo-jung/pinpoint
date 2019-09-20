import { Component, OnInit, ViewChild, Renderer2, ElementRef, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { Moment } from 'moment';
import { fromEvent, merge, Observable, of } from 'rxjs';
import { pluck, map, switchMap, tap, filter } from 'rxjs/operators';

import { AppInteractionService } from 'app/service/app-interaction.service';

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
export class PeriodComponent implements OnInit, AfterViewInit {
    @ViewChild('fromTimepicker', {read: ElementRef, static: false}) fromTimepicker: ElementRef;
    @ViewChild('toTimepicker', {read: ElementRef, static: false}) toTimepicker: ElementRef;
    @Output() outPeriodSelect = new EventEmitter<number[]>();

    private fromTimepickerHourControl: HTMLElement;
    private fromTimepickerMinControl: HTMLElement;
    private toTimepickerHourControl: HTMLElement;
    private toTimepickerMinControl: HTMLElement;
    private selectedFromHour = 0;
    private selectedFromMin = 0;
    private selectedToHour = 0;
    private selectedToMin = 0;

    periodFrom: Date;
    periodTo: Date;
    minDate: Date;

    constructor(
        private appInteractionService: AppInteractionService,
        private renderer: Renderer2
    ) {}

    ngOnInit() {}
    ngAfterViewInit() {
        [this.fromTimepickerHourControl, this.fromTimepickerMinControl] = this.fromTimepicker.nativeElement.querySelectorAll('.ngx-timepicker-control');
        [this.toTimepickerHourControl, this.toTimepickerMinControl] = this.toTimepicker.nativeElement.querySelectorAll('.ngx-timepicker-control');

        this.subscribe();
        this.bindEventListener();
    }

    private subscribe(): void {
        merge(
            this.appInteractionService.onAppSelected$.pipe(
                switchMap((app: string) => {
                    const isAppNotSelected = app === '';

                    return of(this.fromTimepickerHourControl, this.toTimepickerHourControl).pipe(
                        tap((elm: HTMLElement) => {
                            if (isAppNotSelected) {
                                this.setInputValue(elm, '0');
                            }
                        }),
                        map((elm: HTMLElement) => ({elm, disabled: isAppNotSelected}))
                    );
                })
            ),
            this.appInteractionService.onAgentSelected$.pipe(
                switchMap((agent: string) => {
                    const isAgentNotSelected = agent === '';

                    return of(this.fromTimepickerMinControl, this.toTimepickerMinControl).pipe(
                        tap((elm: HTMLElement) => {
                            if (isAgentNotSelected) {
                                this.setInputValue(elm, '00');
                            }
                        }),
                        map((elm: HTMLElement) => ({elm, disabled: isAgentNotSelected}))
                    );
                })
            )
        ).subscribe(({elm, disabled}: {elm: HTMLElement, disabled: boolean}) => {
            disabled ? (this.renderer.addClass(elm, 'disabled')) : this.renderer.removeClass(elm, 'disabled');
        });
    }

    private setInputValue(parentElm: HTMLElement, value: string): void {
        const input = parentElm.querySelector('input');
        const event = new Event('keyup');

        input.value = value;
        input.dispatchEvent(event);
    }

    private bindEventListener(): void {
        const fromHourPair = this.getInputControllerPair(this.fromTimepickerHourControl);
        const fromMinPair = this.getInputControllerPair(this.fromTimepickerMinControl);
        const toHourPair = this.getInputControllerPair(this.toTimepickerHourControl);
        const toMinPair = this.getInputControllerPair(this.toTimepickerMinControl);

        merge(
            this.getTimepickerEventObs(fromHourPair).pipe(tap((v: number) => this.selectedFromHour = v)),
            this.getTimepickerEventObs(fromMinPair).pipe(tap((v: number) => this.selectedFromMin = v)),
            this.getTimepickerEventObs(toHourPair).pipe(tap((v: number) => this.selectedToHour = v)),
            this.getTimepickerEventObs(toMinPair).pipe(tap((v: number) => this.selectedToMin = v)),
        ).pipe(
            filter(() => !!(this.periodFrom && this.periodTo))
        ).subscribe(() => {
            this.emitPeriod();
        });
    }

    private getTimepickerEventObs([input, controller]: [HTMLInputElement, HTMLElement]): Observable<number> {
        return merge(
            fromEvent(input, 'keyup').pipe(pluck('target', 'value')),
            fromEvent(controller, 'click').pipe(map(() => input.value))
        ).pipe(
            map((v: string) => Number(v))
        );
    }

    private getInputControllerPair(parentElm: HTMLElement): [HTMLInputElement, HTMLElement] {
        return [parentElm.querySelector('input'), parentElm.querySelector('.ngx-timepicker-control__arrows')];
    }

    private emitPeriod(): void {
        this.outPeriodSelect.emit([
            this.periodFrom.setHours(this.selectedFromHour, this.selectedFromMin),
            this.periodTo.setHours(this.selectedToHour, this.selectedToMin),
        ]);
    }

    onFromDateChange(value: Moment): void {
        this.periodFrom = value.toDate();
        this.minDate = this.periodFrom;

        if (this.periodTo) {
            this.emitPeriod();
        }
    }

    onToDateChange(value: Moment): void {
        this.periodTo = value.toDate();

        if (this.periodFrom) {
            this.emitPeriod();
        }
    }
}
