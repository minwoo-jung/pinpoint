import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { LocalStorageService } from 'angular-2-local-storage';
import { AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService, WindowRefService } from 'app/shared/services';
import { YobiNoticeCheckService } from './yobi-notice-check.service';
import { ConfigurationPopupContainerComponent } from 'app/core/components/configuration-popup/configuration-popup-container.component';

@Component({
    selector: 'pp-command-group-container',
    templateUrl: './command-group-container.component.html',
    styleUrls: ['./command-group-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommandGroupContainerComponent implements OnInit {
    private lastNoticeTime = -1;
    hasNewNotice = false;
    constructor(
        private windowRefService: WindowRefService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private localStorageService: LocalStorageService,
        private yobiNoticeCheckServer: YobiNoticeCheckService
    ) {}

    ngOnInit() {
        this.getYobiNoticeData();
    }
    private getYobiNoticeData(): void {
        this.yobiNoticeCheckServer.getYobiNotice().subscribe((yobiNoticeData: any) => {
            const localLastNoticeTime = this.localStorageService.get('last-notice-time') || -1;
            if ( yobiNoticeData.elements.length > 0 ) {
                this.lastNoticeTime = yobiNoticeData.elements[0].lastUpdatedAt;
                if ( localLastNoticeTime === -1 ) {
                    this.hasNewNotice = true;
                } else {
                    if  ( this.lastNoticeTime > localLastNoticeTime ) {
                        this.hasNewNotice = true;
                    } else {
                        this.hasNewNotice = false;
                    }
                }
            }
        }, (error: IServerErrorFormat) => {
            this.hasNewNotice = false;
        });
    }
    onOpenConfigurationPopup(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_CONFIGURATION_POPUP);
        this.dynamicPopupService.openPopup({
            component: ConfigurationPopupContainerComponent
        });
    }
    onOpenYobi(): void {
        this.localStorageService.set('last-notice-time', this.lastNoticeTime);
        this.windowRefService.nativeWindow.open('https://yobi.navercorp.com/Labs-public_pinpoint-issues/posts');
    }
}
