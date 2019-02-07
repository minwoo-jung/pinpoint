import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { UrlRouteManagerService } from 'app/shared/services';
import { IGridData } from './application-auth-and-alarm-info.component';
import { UserGroupInteractionService } from 'app/core/components/user-group/user-group-interaction.service';

@Component({
    selector: 'pp-application-auth-and-alarm-info-container',
    templateUrl: './application-auth-and-alarm-info-container.component.html',
    styleUrls: ['./application-auth-and-alarm-info-container.component.css']
})
export class ApplicationAuthAndAlarmInfoContainerComponent implements OnInit {
    private unsubscribe: Subject<null> = new Subject();
    selectedUserGroupId: string;
    rowData: IGridData[] = [];
    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private userGroupInteractionService: UserGroupInteractionService
    ) {}
    ngOnInit() {
        this.userGroupInteractionService.onSelect$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((id: string) => {
            this.selectedUserGroupId = id;
        });
    }
    onCellClicked(value: {applicationName: string, field: string}): void {
        // detail
        // delete
        // alarm

    }
    onAdd(): void {
        if (this.selectedUserGroupId) {
            this.urlRouteManagerService.moveOnPage({
                url: [
                    UrlPath.CONFIG,
                    UrlPathId.ALARM
                ],
                queryParam: {
                    userGroupId: this.selectedUserGroupId
                }
            });
        }
    }
    getBtnClass(): string {
        return this.selectedUserGroupId ? 'btn btn-sm btn-blue' : 'btn btn-sm btn-gray';
    }
}
