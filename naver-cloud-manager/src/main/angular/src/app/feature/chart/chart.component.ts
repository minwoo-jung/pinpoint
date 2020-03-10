import { Component, OnInit, Input, ViewChild, ElementRef, AfterViewInit, OnChanges, SimpleChanges } from '@angular/core';
import { bb, PrimitiveArray } from 'billboard.js';
import { format } from 'date-fns';

import { Span } from 'app/service/app-data.service';

@Component({
    selector: 'pp-chart',
    templateUrl: './chart.component.html',
    styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit, OnChanges, AfterViewInit {
    @ViewChild('chartHolder', { static: false }) chartHolder: ElementRef;
    @Input() data: Span[];

    private chartInstance: any;
    private chartData: PrimitiveArray[];
    private yMax: number;
    private defaultYMax = 4;

    constructor() {}
    ngOnInit() {}
    ngOnChanges({data: {firstChange, currentValue}}: SimpleChanges) {
        this.chartData = this.makeChartData(currentValue);
        this.yMax = this.getMaxTickValue(this.chartData, 1);

        if (!firstChange) {
            this.updateChart();
        }
    }

    ngAfterViewInit() {
        this.chartInstance = bb.generate({
            bindto: this.chartHolder.nativeElement,
            data: {
                x: 'x',
                columns: this.chartData,
                empty: {
                    label: {
                        text: '데이터가 없습니다.'
                    }
                },
                names: {
                    count: 'Count'
                }
            },
            padding: {
                bottom: 10,
                right: 25
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        count: 6,
                        format: (time: Date) => {
                            return format(time, 'yyyy.MM.dd') + '\n' + format(time, 'H:mm');
                        }
                    },
                    padding: {
                        left: 0,
                        right: 0
                    }
                },
                y: {
                    tick: {
                        count: 5,
                        format: (v: number): string => this.convertWithUnit(v)
                    },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    max: this.yMax,
                    default: [0, this.defaultYMax]
                }
            },
            tooltip: {
                order: '',
                format: {
                    value: (v: number) => this.addComma(v.toString())
                }
            },
            point: {
                r: 0,
                focus: {
                    expand: {
                        r: 3
                    }
                }
            }
        });
    }

    private makeChartData(data: Span[]): PrimitiveArray[] {
        return data.reduce(([xArr, countArr]: PrimitiveArray[], {timestamp, count}: Span) => {
            return [[...xArr, timestamp], [...countArr, count]];
        }, [['x'], ['count']]);
    }

    private updateChart(): void {
        this.chartInstance.config('axis.y.max', this.yMax);
        this.chartInstance.load({
            columns: this.chartData,
            unload: this.getEmptyDataKeys(this.chartData)
        });
    }

    private getEmptyDataKeys(data: PrimitiveArray[]): string[] {
        return data.slice(1).filter((d: PrimitiveArray) => d.length === 1).map(([key]: PrimitiveArray) => key as string);
    }

    private addComma(str: string): string {
        return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
    }

    private convertWithUnit(value: number): string {
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1000
                ? (v / 1000).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }

    private getMaxTickValue(data: PrimitiveArray[], startIndex: number, endIndex?: number): number {
        const maxData = Math.max(...data.slice(startIndex, endIndex).map((d: PrimitiveArray) => d.slice(1)).flat() as number[]);
        const adjustedMax = maxData + maxData / 4;
        const baseUnitNumber = 1000;
        const maxTick = Array(4).fill(0).reduce((acc: number, _: number, i: number, arr: number[]) => {
            const unitNumber = Math.pow(baseUnitNumber, i);

            return acc / unitNumber >= baseUnitNumber
                ? acc
                : (arr.splice(i + 1), this.getNearestNiceNumber(acc / unitNumber) * unitNumber);
        }, adjustedMax);

        return maxTick === 0 ? this.defaultYMax : maxTick;
    }

    private getNearestNiceNumber(v: number): number {
        /**
         * ex: v = 10.2345
         * 정수부 자릿수 switch/case
         * 1자리면 4의배수
         * 2자리면 20의배수
         * 3자리면 100의배수
         * v보다 큰 가장가까운 수 리턴해주기.
         */
        const integerPartLength = v.toString().split('.')[0].length;

        switch (integerPartLength) {
            case 1:
                return Math.ceil(v / 4) * 4;
            case 2:
                return Math.ceil(v / 20) * 20;
            case 3:
                return Math.ceil(v / 100) * 100;
        }
    }
}
