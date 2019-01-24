import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
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
    message: string;
    useDisable = false;
    showLoading = false;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private roleInfoDataService: RoleInfoDataService
    ) {}
    ngOnInit() {
        // this.roleInfoDataService.getRoleInfo(selectedRole).pipe(
        //     takeUntil(this.unsubscribe)
        // ).subscribe((roleInfo: IPermissions) => {
        //     console.log( roleInfo);
        // });

    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelected(selectedRole: string): void {
    }
    hasMessage(): boolean {
        return false;
    }
    onCloseMessage(): void {

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
