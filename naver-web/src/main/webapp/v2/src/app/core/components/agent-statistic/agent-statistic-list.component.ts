import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { GridOptions } from 'ag-grid-community';

@Component({
    selector: 'pp-agent-statistic-list',
    templateUrl: './agent-statistic-list.component.html',
    styleUrls: ['./agent-statistic-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class AgentStatisticListComponent implements OnInit, OnDestroy  {
    @Input() gridData: any;
    @Input() agentCount: number;
    @Output() outCellClick: EventEmitter<any> = new EventEmitter();
    gridOptions: GridOptions;
    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }
    ngOnDestroy() {}
    private initGridOptions() {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: false,
                filter: true
            },
            columnDefs : this.makeColumnDefs(),
            headerHeight: 34,
            floatingFilter: true,
            animateRows: true,
            rowHeight: 30,
            getNodeChildDetails: (file) => {
                if (file && file.folder) {
                    return {
                        group: true,
                        children: file.children,
                        expanded: file.open
                    };
                } else {
                    return null;
                }
            },
            suppressRowClickSelection: true,
            rowSelection: 'multiple'
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: '#',
                field: 'index',
                width: 60,
                filter: false
            },
            {
                headerName: `Application`,
                field: 'application',
                width: 550,
                cellRenderer: 'agGroupCellRenderer',
                cellRendererParams: {
                    innerRenderer: (params: any) => {
                        return '&nbsp;' + params.data.application;
                    },
                    suppressCount: true
                },
                cellStyle: {
                    color: 'rgb(54, 162, 235)',
                    'font-weight': 600
                },
                filter: 'agTextColumnFilter',
                tooltipField: 'application'
            },
            {
                headerName: `Agent`,
                field: 'agent',
                width: 300,
                filter: 'agTextColumnFilter',
                tooltipField: 'agent'
            },
            {
                headerName: 'Agent Version',
                field: 'agentVersion',
                width: 150,
                filter: 'agTextColumnFilter',
                tooltipField: 'agentVersion'
            },
            {
                headerName: 'JVM Version',
                field: 'jvmVersion',
                width: 150,
                filter: 'agTextColumnFilter',
                tooltipField: 'jvmVersion'
            },
        ];
    }
    onCellClick(params: any): void {
        if (params.colDef.field === 'application') {
            this.outCellClick.next({
                application: params.data.application,
                serviceType: params.data.serviceType
            });
        }
    }
    onRendered(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
