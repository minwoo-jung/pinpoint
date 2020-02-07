import { Component, OnInit, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { UrlQuery } from 'app/shared/models';
import {
    WebAppSettingDataService,
    StoreHelperService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
} from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';
import { FOCUS_TYPE } from './application-list-for-header.component';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-application-list-for-configuration-alarm-container',
    templateUrl: './application-list-for-configuration-alarm-container.component.html',
    styleUrls: ['./application-list-for-configuration-alarm-container.component.css'],
})
export class ApplicationListForConfigurationAlarmContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private originalAppList: IApplication[];
    private applicationQuery: string;

    filteredAppList: IApplication[];
    funcImagePath: Function;
    selectedApp: IApplication;
    showTitle = false;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    restCount = 0;
    focusIndex = -1;
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: '',
        EMPTY: ''
    };
    isEmpty: boolean;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlQuery.APPLICATION_ID))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationQuery = urlService.getQueryValue(UrlQuery.APPLICATION_ID);
        });

        // TODO: Test: applicationQuery가 더 늦게떨어지면 어떻게되지?
        this.initList();
        this.initI18nText();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
        this.applicationListInteractionForConfigurationService.setSelectedApplication(null);
    }

    private initList(): void {
        this.storeHelperService.getApplicationList(this.unsubscribe).subscribe((appList: IApplication[]) => {
            this.originalAppList = appList;
            this.filterList();
            if (!this.applicationQuery) {
                return;
            }

            const selectedApp = this.filteredAppList.find((app: IApplication) => this.applicationQuery === app.getApplicationName());

            if (selectedApp) {
                this.onSelectApp(selectedApp);
            }
        });
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('COMMON.INPUT_APP_NAME_PLACE_HOLDER'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe(([placeholderText, emptyText]: string[]) => {
            this.i18nText.SEARCH_INPUT_GUIDE = placeholderText;
            this.i18nText.EMPTY = emptyText;
        });
    }

    private selectApp(app: IApplication): void {
        if (!app) {
            return;
        }

        this.selectedApp = app;
    }

    private filterList(): void {
        if (this.query === '') {
            this.filteredAppList = this.originalAppList;
        } else {
            this.filteredAppList = this.originalAppList.filter((app: IApplication) => {
                return new RegExp(this.query, 'i').test(app.getApplicationName());
            });
        }

        this.isEmpty = isEmpty(this.filteredAppList);
    }

    private set query(query: string) {
        this._query = query;
        this.filterList();
    }

    private get query(): string {
        return this._query;
    }

    onSearch(query: string): void {
        if (this.query === query) {
            return;
        }

        this.query = query;
    }

    onCancel(): void {
        this.query = '';
    }

    onSelectApp(app: IApplication): void {
        if (app.equals(this.selectedApp)) {
            return;
        }

        this.selectApp(app);
        this.applicationListInteractionForConfigurationService.setSelectedApplication(app);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_FOR_ALARM);
    }

    onFocused(index: number): void {
        this.focusIndex = index;
        this.focusType = FOCUS_TYPE.MOUSE;
    }
}
