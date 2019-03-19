import { Component, OnInit, ViewEncapsulation, EventEmitter, Input, Output } from '@angular/core';
import { GridOptions } from 'ag-grid';

@Component({
    selector: 'pp-removable-agent-list',
    templateUrl: './removable-agent-list.component.html',
    styleUrls: ['./removable-agent-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class RemovableAgentListComponent implements OnInit {
    @Input() rowData: any[];
    @Output() outSelectAgent: EventEmitter<string[]> = new EventEmitter();
    gridOptions: GridOptions;

    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }
    private initGridOptions() {
        this.gridOptions = <GridOptions>{
            columnDefs : this.makeColumnDefs(),
            headerHeight: 32,
            enableColResize: true,
            enableSorting: false,
            animateRows: true,
            rowHeight: 30,
            suppressRowClickSelection: false,
            suppressLoadingOverlay: true,
            suppressCellSelection: true,
            localeText: {noRowsToShow: 'No Agent'}
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: 'Removable Agent List',
                children: [
                    {
                        headerName: 'REMOVE',
                        field: 'agentId',
                        width: 110,
                        cellRenderer: (param: any) => {
                            return '<i style="color:red" class="far fa-trash-alt"></i>';
                        },
                        cellStyle: this.alignCenterPointCellStyle
                    },
                    {
                        headerName: 'Host Name',
                        field: 'hostName',
                        width: 400,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Id',
                        field: 'agentId',
                        width: 250,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Version',
                        field: 'agentVersion',
                        width: 160,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'IP',
                        field: 'ip',
                        width: 150,
                        cellStyle: this.alignCenterPointCellStyle
                    }
                ]
            }
        ];
    }
    alignCenterCellStyle(): any {
        return {'text-align': 'center'};
    }
    alignCenterPointCellStyle(): any {
        return {
            'cursor': 'pointer',
            'text-align': 'center'
        };
    }
    onCellClick(row: any): void {
        if (row.colDef.headerName === 'REMOVE') {
            this.outSelectAgent.next([row.data.applicationName, row.data.agentId]);
        }
    }
    onGridReady(params: any) {
        params.api.sizeColumnsToFit();
    }
}
