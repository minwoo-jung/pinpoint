import { Component, OnInit, ViewEncapsulation, EventEmitter, Input, Output } from '@angular/core';
import { GridOptions } from 'ag-grid';

export interface IGridData {
    applicationName: string;
    position: string;
    detail: string;
    delete: string;
    alarm: string;
}

@Component({
    selector: 'pp-application-auth-and-alarm-info',
    templateUrl: './application-auth-and-alarm-info.component.html',
    styleUrls: ['./application-auth-and-alarm-info.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ApplicationAuthAndAlarmInfoComponent implements OnInit {

    @Input() rowData: IGridData[];
    @Output() outCellSelected: EventEmitter<{applicationName: string, field: string}> = new EventEmitter();
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
            localeText: {noRowsToShow: 'User Group을 선택하세요.'}
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: 'Application Authentication',
                children: [
                    {
                        headerName: 'Application Name',
                        field: 'applicationName',
                        width: 400,
                        cellStyle: this.argumentCellStyle
                    },
                    {
                        headerName: 'Position',
                        field: 'position',
                        width: 110,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Detail',
                        field: 'detail',
                        width: 110,
                        cellRenderer: () => { return '<i class="far fa-list-alt"></i>'; },
                        cellStyle: this.alignCenterPointCellStyle
                    },
                    {
                        headerName: 'Delete',
                        field: 'delete',
                        width: 110,
                        cellRenderer: () => { return '<i class="far fa-trash-alt"></i>'; },
                        cellStyle: this.alignCenterPointCellStyle
                    }
                ]
            },
            {
                headerName: 'Alarm',
                children: [
                    {
                        headerName: '',
                        field: 'alarm',
                        width: 300,
                        cellRenderer: () => { return '<i class="far fa-bell"></i>'; },
                        cellStyle: this.alignCenterPointCellStyle
                    }
                ]
            }
        ];
    }
    argumentCellStyle(): any {
        return {'text-align': 'left'};
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
        switch (row.colDef.field) {
            case 'detail':
            case 'delete':
            case 'alarm':
                this.outCellSelected.next({
                    applicationName : row.data.applicationName,
                    field: row.colDef.field
                });
                break;
        }
    }
}
