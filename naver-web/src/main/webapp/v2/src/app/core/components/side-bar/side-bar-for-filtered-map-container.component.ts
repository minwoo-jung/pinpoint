import { Component, OnInit, OnDestroy, ElementRef, Renderer2, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Router, RouterEvent, NavigationStart } from '@angular/router';
import { Subject, Observable, merge } from 'rxjs';
import { filter, mapTo, tap, map, takeUntil } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-side-bar-for-filtered-map-container',
    templateUrl: './side-bar-for-filtered-map-container.component.html',
    styleUrls: ['./side-bar-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    target: ISelectedTarget;
    useDisable = true;
    showLoading = true;
    showDivider = false;
    isAuthorized = false;
    isTargetMerged: boolean;
    securityGuideUrl$: Observable<string>;

    constructor(
        private router: Router,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private el: ElementRef,
        private renderer: Renderer2,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.securityGuideUrl$ = this.webAppSettingDataService.getSecurityGuideUrl();
        this.addPageLoadingHandler();
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private addPageLoadingHandler(): void {
        this.router.events.pipe(
            takeUntil(this.unsubscribe),
            filter((e: RouterEvent) => {
                return e instanceof NavigationStart;
            })
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
            this.cd.detectChanges();
        });
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

            this.cd.detectChanges();
        });

        merge(
            this.storeHelperService.getServerMapTargetSelectedByList(this.unsubscribe).pipe(mapTo(false)),
            this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
                filter((target: ISelectedTarget) => !!target),
                tap((target: ISelectedTarget) => {
                    this.target = target;
                    this.renderer.setStyle(this.el.nativeElement, 'width', '477px');
                }),
            )
        ).subscribe(({isNode, isWAS, isMerged, isAuthorized}: ISelectedTarget) => {
            this.isTargetMerged = isMerged;
            this.isAuthorized = isAuthorized;
            this.showDivider = isNode && isWAS && !isMerged;
            this.cd.detectChanges();
        });
    }
}
