import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { RoleInfoDataService } from './role-info-data.service';
import { MessageQueueService, MESSAGE_TO } from 'app/shared/services/message-queue.service';

@Component({
    selector: 'pp-role-info-container',
    templateUrl: './role-info-container.component.html',
    styleUrls: ['./role-info-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoleInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    roleList: any = [];
    roleInfo: IPermissions;
    errorMessage: string;
    useDisable = false;
    showLoading = false;

    i18nText: {[key: string]: string} = {
        adminMenuTitle: '',
        viewAdminMenu: '',
        editRoleTitle: '',
        editRole: '',
        editUserTitle: '',
        editUser: '',
        preoccupancyTitle: '',
        preoccupancy: '',
        editAuthorTitle: '',
        editAuthorForEverything: '',
        editAuthorOnlyManager: '',
        editAlarmTitle: '',
        editAlarmForEverything: '',
        editAlarmOnlyGroupMember: '',
        editGroupTitle: '',
        editGroupForEverything: '',
        editGroupOnlyGroupMember: ''
    };
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
        private roleInfoDataService: RoleInfoDataService
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SELECT_ROLE).subscribe((param: string[]) => {
            const role = param[0];
            this.showProcessing();
            this.roleInfoDataService.getRoleInfo(role).pipe(
                takeUntil(this.unsubscribe)
            ).subscribe((roleInfo: IPermissions) => {
                this.hideProcessing();
                this.roleInfo = roleInfo;
                this.changeDetectorRef.detectChanges();
            }, () => {
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            });
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        this.translateService.get('CONFIGURATION.PERMISSION').subscribe((i18n: {[key: string]: string}) => {
            Object.keys(i18n).map((key: string) => {
                const newKey = key.toLowerCase().replace(/\_(\D)/ig, function(m, s) {
                    return s.toUpperCase();
                });
                this.i18nText[newKey] = i18n[key];
            });
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
