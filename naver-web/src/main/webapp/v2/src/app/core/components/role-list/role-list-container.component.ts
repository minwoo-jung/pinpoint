import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Actions } from 'app/shared/store';
import { StoreHelperService } from 'app/shared/services';
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
        private storeHelperService: StoreHelperService,
        private roleListDataService: RoleListDataService
    ) {}
    ngOnInit() {
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
        this.storeHelperService.dispatch(new Actions.ChangeRoleSelection(selectedRole));
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
