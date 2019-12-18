import { Component, OnInit, Injector, ComponentFactoryResolver, OnDestroy } from '@angular/core';
import { Subject, Observable, combineLatest } from 'rxjs';
import { withLatestFrom, filter, map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { UrlPath, UrlPathId } from 'app/shared/models';
import {
    UrlRouteManagerService,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    StoreHelperService
} from 'app/shared/services';
import { ApplicationAuthAndAlarmDataService } from './application-auth-and-alarm-data.service';
import { ApplicationAuthAndAlarmPopupComponent } from './application-auth-and-alarm-popup.component';

@Component({
    selector: 'pp-application-auth-and-alarm-info-container',
    templateUrl: './application-auth-and-alarm-info-container.component.html',
    styleUrls: ['./application-auth-and-alarm-info-container.component.css']
})
export class ApplicationAuthAndAlarmInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    selectedUserGroupId: string;
    rowData$: Observable<IApplicationAuthInfo[]>;
    i18nText: {
        SERVER_MAP: string;
        API_META: string;
        PARAM_META: string;
        SQL_META: string;
    };

    constructor(
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private dynamicPopupService: DynamicPopupService,
        private messageQueueService: MessageQueueService,
        private applicationAuthAndAlarmDataService: ApplicationAuthAndAlarmDataService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private analyticsService: AnalyticsService,
        private storeHelperService: StoreHelperService
    ) {}

    ngOnInit() {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP).subscribe((userGroupId: string) => {
            this.selectedUserGroupId = userGroupId;
            this.rowData$ = this.applicationAuthAndAlarmDataService.getData(this.selectedUserGroupId).pipe(
                withLatestFrom(this.storeHelperService.getApplicationList(this.unsubscribe)),
                map(([dataList, appList]: [IApplicationAuthInfo[], IApplication[]]) => {
                    return dataList.filter(({applicationId}: IApplicationAuthInfo) => {
                        return appList.some((app: IApplication) => app.getApplicationName() === applicationId);
                    });
                })
            );
        });
        this.getI18NText();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText(): void {
        combineLatest(
            this.translateService.get('CONFIGURATION.AUTH.SERVER_MAP'),
            this.translateService.get('CONFIGURATION.AUTH.API_META'),
            this.translateService.get('CONFIGURATION.AUTH.PARAM_META'),
            this.translateService.get('CONFIGURATION.AUTH.SQL_META'),
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                SERVER_MAP: i18n[0],
                API_META: i18n[1],
                PARAM_META: i18n[2],
                SQL_META: i18n[3]
            };
        });
    }

    onCellClicked(value: any): void {
        switch (value.type) {
            case 'configuration':
                this.showConfiguration(value);
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_DETAIL_ON_APPLICATION_AUTH_AND_ALARM_INFO);
                break;
            case 'edit':
                this.urlRouteManagerService.moveOnPage({
                    url: [
                        UrlPath.CONFIG,
                        UrlPathId.ALARM
                    ],
                    queryParam: {
                        applicationId: value.applicationId,
                        position: value.position
                    }
                });
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_MORE_ON_APPLICATION_AUTH_AND_ALARM_INFO);
                break;
        }
    }

    showConfiguration(value: any): void {
        const {left, top, width, height} = value.coord;

        this.dynamicPopupService.openPopup({
            data: {
                applicationId: value.applicationId,
                configuration: {
                    [this.i18nText.SERVER_MAP]: value.configuration.serverMapData,
                    [this.i18nText.API_META]: value.configuration.apiMetaData,
                    [this.i18nText.PARAM_META]: value.configuration.paramMetaData,
                    [this.i18nText.SQL_META]: value.configuration.sqlMetaData,
                }
            } ,
            coord: {
                coordX: left + width / 2 - 240,
                coordY: top + height / 2 - 130
            },
            component: ApplicationAuthAndAlarmPopupComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
