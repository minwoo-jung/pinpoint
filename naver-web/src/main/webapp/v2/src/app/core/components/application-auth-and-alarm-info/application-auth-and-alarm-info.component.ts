import { Component, OnInit, ViewEncapsulation, EventEmitter, Input, Output } from '@angular/core';
import { GridOptions } from 'ag-grid-community';

@Component({
    selector: 'pp-application-auth-and-alarm-info',
    templateUrl: './application-auth-and-alarm-info.component.html',
    styleUrls: ['./application-auth-and-alarm-info.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ApplicationAuthAndAlarmInfoComponent implements OnInit {
    @Input() rowData: IApplicationAuthInfo[];
    @Output() outCellSelected: EventEmitter<any> = new EventEmitter();
    gridOptions: GridOptions;

    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }
    private initGridOptions() {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: false
            },
            columnDefs : this.makeColumnDefs(),
            headerHeight: 32,
            animateRows: true,
            rowHeight: 30,
            suppressRowClickSelection: false,
            suppressLoadingOverlay: true,
            suppressCellSelection: true,
            localeText: {noRowsToShow: 'No Application'}
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: 'Application Authentication & Alarm',
                children: [
                    {
                        headerName: 'Application Name',
                        field: 'applicationId',
                        width: 500,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Position',
                        field: 'position',
                        width: 150,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Detail',
                        field: 'configuration',
                        width: 150,
                        cellRenderer: () => { return '<i class="far fa-list-alt"></i>'; },
                        cellStyle: this.alignCenterPointCellStyle
                    },
                    {
                        headerName: 'More',
                        field: 'position',
                        width: 150,
                        cellRenderer: (params: any) => {
                            return '<i class="fas fa-external-link-alt"></i>';
                        },
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
        switch (row.colDef.headerName) {
            case 'Detail':
                this.outCellSelected.next({
                    type: 'configuration',
                    applicationId: row.data.applicationId,
                    coord: (row.event.target as HTMLElement).getBoundingClientRect(),
                    configuration: row.value
                });
                break;
            case 'More':
                this.outCellSelected.next({
                    type: 'edit',
                    applicationId : row.data.applicationId,
                    position: row.data.position
                });
                break;
        }
    }
    onGridReady(params: any) {
        params.api.sizeColumnsToFit();
    }
}
