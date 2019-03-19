import { Component, OnInit, OnDestroy, Input, ViewChild, ElementRef } from '@angular/core';
import { Chart } from 'chart.js';

@Component({
    selector: 'pp-agent-statistic-chart',
    templateUrl: './agent-statistic-chart.component.html',
    styleUrls: ['./agent-statistic-chart.component.css']
})
export class AgentStatisticChartComponent implements OnInit, OnDestroy  {
    @ViewChild('agentChart') el: ElementRef;
    _chartData: any;
    chartObj: Chart;
    @Input() type: string;
    @Input() title: string;
    @Input() barColor: string;
    @Input()
    set chartData(value: any) {
        if (value && value.jvmVersion && value.agentVersion) {
            this._chartData = value;
            this.initChartObj();
        }
    }
    constructor() {}
    ngOnInit() {}
    ngOnDestroy() {}
    private initChartObj() {
        this.chartObj = new Chart(this.el.nativeElement.getContext('2d'), {
            type: 'horizontalBar',
            data: this.makeDataOption(),
            options: this.makeNormalOption()
        });
    }
    private makeDataOption(): any {
        const labels: string[] = [];
        const values: number[] = [];
        const dataSet = this._chartData[this.type];
        Object.keys(dataSet).sort().forEach((key: string) => {
            labels.push(key);
            values.push(dataSet[key]);
        });
        const dataOption = {
            labels: labels,
            borderWidth: 0,
            datasets: []
        };
        dataOption.datasets.push({
            label: this.title,
            data: values,
            backgroundColor: [
                '#36688D',
                '#F3CD05',
                '#F49F05',
                '#F18904',
                '#DBA589',
                '#A7414A',
                '#282726',
                '#6A8A82',
                '#A37C27',
                '#563838',
                '#0444BF',
                '#0584F2',
                '#0AAFF1',
                '#EDF259',
                '#A79674',
                '#6465A5',
                '#6975A6',
                '#F3E96B',
                '#F28A30',
                '#F05837',
                '#ABA6BF',
                '#595775',
                '#583E2E',
                '#F1E0D6',
                '#BF988F',
                '#192E5B',
                '#1D65A6',
                '#72A2C0',
                '#00743F',
                '#F2A104',
                '#040C0E',
                '#132226',
                '#525B56',
                '#BE9063',
                '#A4978E',
                '#DAA2DA',
                '#DBB4DA',
                '#DE8CF0',
                '#BED905',
                '#93A806',
                '#A4A4BF',
                '#16235A',
                '#2A3457',
                '#888C46',
                '#F2EAED',
                '#A3586D',
                '#5C4A72',
                '#F3B05A',
                '#F4874B',
                '#F46A4E',
                '#80ADD7',
                '#0ABDA0',
                '#EBF2EA',
                '#D4DCA9',
                '#BF9D7A',
            ],
            borderWidth: 0
        });
        return dataOption;
    }
    private makeNormalOption(): any {
        return {
            maintainAspectRatio: false,
            tooltips: {
                enabled: false
            },
            scales: {
                yAxes: [{
                    gridLines: {
                        display: false
                    },
                    ticks: {
                        fontFamily: 'monospace'
                    }
                }],
                xAxes: [{
                    ticks: {
                        fontFamily: 'monospace'
                    }
                }]
            },
            animation: {
                duration: 0,
                onComplete: (chartElement: any) => {
                    const ctx = chartElement.chart.ctx;
                    ctx.fillStyle = chartElement.chart.config.options.defaultFontColor;
                    ctx.fontSize = 9;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'top';
                    chartElement.chart.data.datasets.forEach((dataset: any) => {
                        for (let i = 0 ; i < dataset.data.length ; i++) {
                            const model = dataset._meta[Object.keys(dataset._meta)[0]].data[i]._model;
                            ctx.fillText(dataset.data[i], model.x, model.y - 5);
                        }
                    });
                }
            },
            hover: {
                animationDuration: 0
            },
            legend: {
                display: true,
                labels: {
                    boxWidth: 30,
                    padding: 10,
                    fontFamily: 'monospace'
                },
                position: 'bottom'
            }
        };
    }
}
