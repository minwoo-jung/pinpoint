import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { Actions } from 'app/shared/store';
import { StoreHelperService } from 'app/shared/services';
import { STORE_KEY } from 'app/shared/store';
import { RoleInfoDataService } from './role-info-data.service';

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
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private roleInfoDataService: RoleInfoDataService
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.storeHelperService.getObservable(STORE_KEY.ROLE_SELECTION, this.unsubscribe).subscribe((role: string) => {
            if (typeof role === 'undefined') {
                return;
            }
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
        this.storeHelperService.dispatch(new Actions.ChangeRoleSelection(''));
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
