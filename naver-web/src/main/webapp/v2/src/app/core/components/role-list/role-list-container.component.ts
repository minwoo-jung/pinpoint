import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MessageQueueService, MESSAGE_TO, UserPermissionCheckService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
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
    selectedRoleId: string;
    roleList: any = [];
    errorMessage: string;
    useDisable = true;
    showLoading = true;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private roleListDataService: RoleListDataService,
        private messageQueueService: MessageQueueService,
        private userPermissionCheckService: UserPermissionCheckService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.getRoleList();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_REMOVED).subscribe(() => {
            this.selectedRoleId = '';
            this.getRoleList();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.ROLE_INFO_CREATED).subscribe((roleId: string) => {
            this.getRoleList();
            this.selectedRoleId = roleId;
            this.onSelectRole(roleId);
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
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ROLE_CREATION_VIEW);
    }
    onSelectRole(selectedRole: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.ROLE_INFO_SELECT_ROLE,
            param: selectedRole
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ROLE_UPDATE_VIEW);
    }
    onRemoveRole(selectedRole: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.ROLE_INFO_REMOVE_SELECT_ROLE,
            param: selectedRole
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ROLE_REMOVE_CONFIRM_VIEW);
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
