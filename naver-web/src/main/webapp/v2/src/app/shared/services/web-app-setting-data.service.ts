import { Injectable, Injector, ComponentFactoryResolver } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Store, select } from '@ngrx/store';
import { LocalStorageService } from 'angular-2-local-storage';
import 'moment-timezone';
import * as moment from 'moment-timezone';

// from 'app/shared/services';
// 방식을 사용하지 말 것.
// Circular dependency 발생함.
import { AppState, Actions, STORE_KEY } from 'app/shared/store';
import { ComponentDefaultSettingDataService } from 'app/shared/services/component-default-setting-data.service';
import { Application, Period } from 'app/core/models';
import { DynamicPopupService } from 'app/shared/services/dynamic-popup.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';
import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';
import { UserConfigurationDataService } from 'app/shared/services/user-configuration-data.service';

interface IMinMax {
    min: number;
    max: number;
}

@Injectable()
export class WebAppSettingDataService {
    static KEYS = {
        FAVORLIITE_APPLICATION_LIST: 'favoriteApplicationList',
        TIMEZONE: 'timezone',
        DATE_FORMAT: 'dateFormat',
        LIST_HANDLE_POSITION: 'listHandlePosition',
        LAYER_HEIGHT: 'layerHeight',
        USER_DEFAULT_INBOUND: 'userDefaultInbound',
        USER_DEFAULT_OUTBOUND: 'userDefaultOutbound',
        USER_DEFAULT_PERIOD: 'userDefaultPeriod',
        TRANSACTION_LIST_GUTTER_POSITION: 'transactionListGutterPosition',
        CHART_NUM_PER_ROW: 'chartNumPerRow'
    };
    private unsubscribe: Subject<null> = new Subject();
    private favoriteApplicationList: IFavoriteApplication[] = [];
    private IMAGE_PATH = './assets/img/';
    private IMAGE_EXT = '.png';
    private SERVER_MAP_PATH = 'servermap/';
    private ICON_PATH = 'icons/';
    private LOGO_IMG_NAME = 'logo.png';
    constructor(
        private store: Store<AppState>,
        private localStorageService: LocalStorageService,
        private componentDefaultSettingDataService: ComponentDefaultSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private userConfigurationDataService: UserConfigurationDataService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {
        this.store.dispatch(new Actions.ChangeTimezone(this.getTimezone()));
        this.store.dispatch(new Actions.ChangeDateFormat(this.getDateFormat()));
        this.store.pipe(
            select(STORE_KEY.FAVORITE_APPLICATION_LIST),
            takeUntil(this.unsubscribe)
        ).subscribe((list: IApplication[]) => {
            this.favoriteApplicationList = list;
        });
    }
    getSecurityGuideUrl(): Observable<string> {
        return this.newUrlStateNotificationService.getConfiguration('securityGuideUrl');
    }
    useActiveThreadChart(): Observable<boolean> {
        return this.newUrlStateNotificationService.getConfiguration('showActiveThread');
    }
    getUserDepartment(): Observable<string | undefined> {
        return this.newUrlStateNotificationService.getConfiguration('userDepartment');
    }
    useUserEdit(): Observable<boolean> {
        return this.newUrlStateNotificationService.getConfiguration('editUserInfo');
    }
    isDataUsageAllowed(): Observable<boolean> {
        return this.newUrlStateNotificationService.getConfiguration('sendUsage');
    }
    getVersion(): Observable<string> {
        return this.newUrlStateNotificationService.getConfiguration('version');
    }
    isApplicationInspectorActivated(): Observable<boolean> {
        return this.newUrlStateNotificationService.getConfiguration('showApplicationStat');
    }
    getImagePath(): string {
        return this.IMAGE_PATH;
    }
    getServerMapImagePath(): string {
        return this.getImagePath() + this.SERVER_MAP_PATH;
    }
    getIconImagePath(): string {
        return this.getImagePath() + this.ICON_PATH;
    }
    getImageExt(): string {
        return this.IMAGE_EXT;
    }
    getLogoPath(): string {
        return this.getImagePath() + this.LOGO_IMG_NAME;
    }
    getSystemDefaultInbound(): number {
        return this.componentDefaultSettingDataService.getSystemDefaultInbound();
    }
    getSystemDefaultOutbound(): number {
        return this.componentDefaultSettingDataService.getSystemDefaultOutbound();
    }
    getSystemDefaultPeriod(): Period {
        return this.componentDefaultSettingDataService.getSystemDefaultPeriod();
    }
    getSystemDefaultTransactionViewPeriod(): Period {
        return this.componentDefaultSettingDataService.getSystemDefaultTransactionViewPeriod();
    }
    getSystemDefaultChartLayoutOption(): number {
        return this.componentDefaultSettingDataService.getSystemDefaultChartLayoutOption();
    }
    getInboundList(): number[] {
        return this.componentDefaultSettingDataService.getInboundList();
    }
    getOutboundList(): number[] {
        return this.componentDefaultSettingDataService.getOutboundList();
    }
    getPeriodList(path: string): Period[] {
        return this.componentDefaultSettingDataService.getPeriodList(path);
    }
    getMaxPeriodTime(): number {
        return this.componentDefaultSettingDataService.getMaxPeriodTime();
    }
    getColorByRequest(): string[] {
        return this.componentDefaultSettingDataService.getColorByRequest();
    }
    private saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[], application: IFavoriteApplication): void {
        this.userConfigurationDataService.saveFavoriteList(newFavoriateApplicationList).subscribe((result: any) => {
            if (result.result === 'SUCCESS') {
                if (this.favoriteApplicationList.length > newFavoriateApplicationList.length) {
                    this.store.dispatch(new Actions.RemoveFavoriteApplication([
                        new Application(application.applicationName, application.serviceType, application.code)
                    ]));
                } else {
                    this.store.dispatch(new Actions.AddFavoriteApplication([
                        new Application(application.applicationName, application.serviceType, application.code)
                    ]));
                }
            }
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Server Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }
    addFavoriteApplication(application: IApplication): void {
        const newApplication = {
            applicationName: application.getApplicationName(),
            code: application.getCode(),
            serviceType: application.getServiceType()
        };
        this.saveFavoriteList([...this.favoriteApplicationList, newApplication].sort((a, b) => {
            const aName = a.applicationName.toUpperCase();
            const bName = b.applicationName.toUpperCase();
            return aName < bName ? -1 : aName > bName ? 1 : 0;
        }), newApplication);
    }
    removeFavoriteApplication(application: IApplication): void {
        const removeApplication = {
            applicationName: application.getApplicationName(),
            code: application.getCode(),
            serviceType: application.getServiceType()
        };
        const removedList = this.favoriteApplicationList.filter((data: IFavoriteApplication) => {
            return !new Application(data.applicationName, data.serviceType, data.code).equals(application);
        });
        this.saveFavoriteList(removedList, removeApplication);
    }
    getScatterY(key: string): IMinMax {
        return this.localStorageService.get<IMinMax>(key) || { min: 0, max: 10000 };
    }
    setScatterY(key: string, value: IMinMax): void {
        this.localStorageService.set(key, value);
    }
    setTimezone(value: string): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.TIMEZONE, value);
    }
    private getTimezone(): string {
        return this.localStorageService.get(WebAppSettingDataService.KEYS.TIMEZONE) || this.getDefaultTimezone();
    }
    getDefaultTimezone(): string {
        return moment.tz.guess();
    }
    setDateFormat(value: number): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.DATE_FORMAT, value);
    }
    private getDateFormat(): number {
        return this.localStorageService.get(WebAppSettingDataService.KEYS.DATE_FORMAT) || 0;
    }
    getDefaultDateFormat(): string[] {
        return this.componentDefaultSettingDataService.getDefaultDateFormat();
    }
    getDateFormatList(): string[][] {
        return this.componentDefaultSettingDataService.getDateFormatList();
    }
    setListHandlePosition(value: number[]): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.LIST_HANDLE_POSITION, value);
    }
    getListHandlePosition(): number[] {
        return this.localStorageService.get(WebAppSettingDataService.KEYS.LIST_HANDLE_POSITION) || [30, 70];
    }
    setLayerHeight(value: number): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.LAYER_HEIGHT, value);
    }
    getLayerHeight(): number {
        return Number.parseInt(this.localStorageService.get(WebAppSettingDataService.KEYS.LAYER_HEIGHT), 10);
    }
    setUserDefaultInbound(value: number): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.USER_DEFAULT_INBOUND, value);
    }
    getUserDefaultInbound(): number {
        return this.localStorageService.get<number>(WebAppSettingDataService.KEYS.USER_DEFAULT_INBOUND) || this.getSystemDefaultInbound();
    }
    setUserDefaultOutbound(value: number): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.USER_DEFAULT_OUTBOUND, value);
    }
    getUserDefaultOutbound(): number {
        return this.localStorageService.get<number>(WebAppSettingDataService.KEYS.USER_DEFAULT_OUTBOUND) || this.getSystemDefaultOutbound();
    }
    setUserDefaultPeriod(value: Period): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.USER_DEFAULT_PERIOD, value.getValue());
    }
    getUserDefaultPeriod(): Period {
        const userDefaultPeriodInMinute = this.localStorageService.get<number>(WebAppSettingDataService.KEYS.USER_DEFAULT_PERIOD);

        return userDefaultPeriodInMinute ? new Period(userDefaultPeriodInMinute) : this.getSystemDefaultPeriod();
    }
    getServerMapIconPathMakeFunc(): Function {
        return (name: string) => {
            return this.IMAGE_PATH + this.SERVER_MAP_PATH + name + this.IMAGE_EXT;
        };
    }
    getIconPathMakeFunc(): Function {
        return (name: string) => {
            return this.IMAGE_PATH + this.ICON_PATH + name + this.IMAGE_EXT;
        };
    }
    getImagePathMakeFunc(): Function {
        return (name: string) => {
            return this.IMAGE_PATH + name + this.IMAGE_EXT;
        };
    }
    setChartLayoutOption(chartNumPerRow: number): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.CHART_NUM_PER_ROW, chartNumPerRow);
    }
    getChartLayoutOption(): number {
        return this.localStorageService.get<number>(WebAppSettingDataService.KEYS.CHART_NUM_PER_ROW) || this.getSystemDefaultChartLayoutOption();
    }
}
