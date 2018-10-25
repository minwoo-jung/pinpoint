import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-side-bar-for-filtered-map-container',
    templateUrl: './side-bar-for-filtered-map-container.component.html',
    styleUrls: ['./side-bar-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    target: any;
    sideBarWidth = 0;
    useDisable = true;
    showLoading = true;
    securityGuideUrl$: Observable<string>;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    ngOnInit() {
        this.securityGuideUrl$ = this.webAppSettingDataService.getSecurityGuideUrl();
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapLoadingState(this.unsubscribe).subscribe((state: string) => {
            switch (state) {
                case 'loading':
                    this.showLoading = true;
                    this.useDisable = true;
                    break;
                case 'pause':
                case 'completed':
                    this.showLoading = false;
                    this.useDisable = false;
                    break;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: any) => {
            this.target = target;
            this.sideBarWidth = 461;
            this.changeDetectorRef.detectChanges();
        });
    }
    hasTopElement(): boolean {
        return this.target && (this.target.isNode || this.target.isMerged);
    }
    hasAuthrization(): boolean {
        return this.target && this.target.isAuthorized;
    }
}
