import { Component, OnInit, OnDestroy, Renderer2, ElementRef } from '@angular/core';
import { Router, RouterEvent, NavigationStart } from '@angular/router';
import { Subject, Observable, merge } from 'rxjs';
import { filter, mapTo, tap, map } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class';

@Component({
    selector: 'pp-side-bar-container',
    templateUrl: './side-bar-container.component.html',
    styleUrls: ['./side-bar-container.component.css'],
})
export class SideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    target: ISelectedTarget;
    useDisable = true;
    showLoading = true;
    isTargetMerged: boolean;
    securityGuideUrl$: Observable<string>;

    constructor(
        private router: Router,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private el: ElementRef,
        private renderer: Renderer2
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
            filter((e: RouterEvent) => {
                return e instanceof NavigationStart;
            })
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
        });
    }

    private connectStore(): void {
        this.storeHelperService.getServerMapData(this.unsubscribe).pipe(
            filter((serverMapData: ServerMapData) => {
                return !!serverMapData;
            }),
            filter((serverMapData: ServerMapData) => serverMapData.getNodeCount() === 0)
        ).subscribe(() => {
            this.renderer.setStyle(this.el.nativeElement, 'width', '0px');
        });

        merge(
            this.storeHelperService.getServerMapTargetSelectedByList(this.unsubscribe).pipe(mapTo(false)),
            this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
                filter((target: ISelectedTarget) => !!target),
                tap((target: ISelectedTarget) => {
                    this.target = target;
                    this.renderer.setStyle(this.el.nativeElement, 'width', '477px');
                    this.showLoading = false;
                    this.useDisable = false;
                }),
                map(({isMerged}: ISelectedTarget) => isMerged)
            )
        ).subscribe((isMerged: boolean) => {
            this.isTargetMerged = isMerged;
        });
    }

    hasTopElement(): boolean {
        if (!this.target) {
            return false;
        }

        const {isNode, isWAS, isMerged} = this.target;

        return isNode && isWAS && !isMerged;
    }

    hasAuthrization(): boolean {
        return this.target && this.target.isAuthorized;
    }
}
