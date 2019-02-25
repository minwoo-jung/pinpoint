import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { RoleListDataService } from './role-list-data.service';

@Component({
    selector: 'pp-role-list-container',
    templateUrl: './role-list-container.component.html',
    styleUrls: ['./role-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoleListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    roleList: any = [];
    errorMessage: string;
    useDisable = true;
    showLoading = true;

    constructor(
        private roleListDataService: RoleListDataService,
        private messageQueueService: MessageQueueService
    ) {}
    ngOnInit() {
        this.showProcessing();
        this.roleListDataService.getRoleList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((roleList: any) => {
            this.roleList = roleList;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelected(selectedRole: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SELECT_ROLE,
            param: [selectedRole]
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }
    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
