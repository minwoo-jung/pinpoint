import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
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
    message: string;
    useDisable = true;
    showLoading = true;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private roleListDataService: RoleListDataService
    ) {}
    ngOnInit() {
        this.roleListDataService.getRoleList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((roleList: any) => {
            console.log( roleList );
            this.roleList = roleList;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelected(selectedRole: string): void {
        console.log( selectedRole);
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
