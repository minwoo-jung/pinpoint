import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MessageQueueService, MESSAGE_TO, UserPermissionCheckService } from 'app/shared/services';
import { RoleListDataService } from './role-list-data.service';

@Component({
    selector: 'pp-role-list-container',
    templateUrl: './role-list-container.component.html',
    styleUrls: ['./role-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoleListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    hasRoleEditPerm = false;
    roleList: any = [];
    errorMessage: string;
    useDisable = true;
    showLoading = true;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private roleListDataService: RoleListDataService,
        private messageQueueService: MessageQueueService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}
    ngOnInit() {
        this.getRoleList();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_REMOVED).subscribe((param: string[]) => {
            this.getRoleList();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_CREATED).subscribe((param: string[]) => {
            this.getRoleList();
            this.onSelectRole(param[0]);
        });
        this.hasRoleEditPerm = this.userPermissionCheckService.canEditRole();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getRoleList(): void {
        this.showProcessing();
        this.roleListDataService.getRoleList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((roleList: any) => {
            this.roleList = roleList;
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
            this.changeDetectorRef.detectChanges();
        });
    }
    onAddRole(): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.ROLE_INFO_CREATE_ROLE,
            param: []
        });
    }
    onSelectRole(selectedRole: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.ROLE_INFO_SELECT_ROLE,
            param: [selectedRole]
        });
    }
    onRemoveRole(selectedRole: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.ROLE_INFO_REMOVE_SELECT_ROLE,
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
