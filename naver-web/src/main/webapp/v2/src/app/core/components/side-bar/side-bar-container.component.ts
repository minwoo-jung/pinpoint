import { Component, OnInit, OnDestroy, Renderer2, ElementRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Router, RouterEvent, NavigationStart } from '@angular/router';
import { Subject, Observable, merge } from 'rxjs';
import { filter, mapTo, tap, takeUntil, map } from 'rxjs/operators';

import { WebAppSettingDataService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class';

@Component({
    selector: 'pp-side-bar-container',
    templateUrl: './side-bar-container.component.html',
    styleUrls: ['./side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    useDisable = true;
    showLoading = true;
    showDivider = false;
    isAuthorized = false;
    isTargetMerged: boolean;
    securityGuideUrl$: Observable<string>;
    sidebarVisibility = 'hidden';

    constructor(
        private router: Router,
        private messageQueueService: MessageQueueService,
        private webAppSettingDataService: WebAppSettingDataService,
        private el: ElementRef,
        private renderer: Renderer2,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.securityGuideUrl$ = this.webAppSettingDataService.getSecurityGuideUrl();
        this.addPageLoadingHandler();
        this.listenToEmitter();
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

    private listenToEmitter(): void {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
            map((data: ServerMapData) => data.getNodeCount() === 0)
        ).subscribe((isEmpty: boolean) => {
            this.renderer.setStyle(this.el.nativeElement, 'display', isEmpty ? 'none' : 'block');
        });

        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT_BY_LIST).pipe(mapTo(false)),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                tap(({isNode, isWAS, isMerged, isAuthorized}: ISelectedTarget) => {
                    this.showLoading = false;
                    this.useDisable = false;
                    this.isAuthorized = isAuthorized;
                    this.showDivider = isNode && isWAS && !isMerged;
                    this.sidebarVisibility = 'visible';
                }),
                map(({isMerged}: ISelectedTarget) => isMerged)
            )
        ).subscribe((isMerged: boolean) => {
            this.isTargetMerged = isMerged;
            this.cd.detectChanges();
        });
    }
}
