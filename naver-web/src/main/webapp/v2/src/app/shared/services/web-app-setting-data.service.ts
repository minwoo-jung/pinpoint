import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { LocalStorageService } from 'angular-2-local-storage';
import 'moment-timezone';
import * as moment from 'moment-timezone';
import { pluck } from 'rxjs/operators';

import { AppState, Actions } from 'app/shared/store';
// from 'app/shared/services';
// 방식을 사용하지 말 것.
// Circular dependency 발생함.
import { ComponentDefaultSettingDataService } from 'app/shared/services/component-default-setting-data.service';
import { DynamicPopupService } from 'app/shared/services/dynamic-popup.service';
import { Application, Period } from 'app/core/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

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
        TRANSACTION_LIST_GUTTER_POSITION: 'transactionListGutterPosition'
    };
    private userConfigurationURL = 'userConfiguration.pinpoint';
    private favoriteApplicationList: IFavoriteApplication[] = [];
    private IMAGE_PATH = './assets/img/';
    private IMAGE_EXT = '.png';
    private SERVER_MAP_PATH = 'servermap/';
    private ICON_PATH = 'icons/';
    private LOGO_IMG_NAME = 'logo.png';
    constructor(
        private http: HttpClient,
        private store: Store<AppState>,
        private activatedRoute: ActivatedRoute,
        private localStorageService: LocalStorageService,
        private componentDefaultSettingDataService: ComponentDefaultSettingDataService,
        private dynamicPopupService: DynamicPopupService
    ) {
        this.loadFavoriteList();
        this.store.dispatch(new Actions.ChangeTimezone(this.getTimezone()));
        this.store.dispatch(new Actions.ChangeDateFormat(this.getDateFormat()));
    }
    private getConfigurationData(): Observable<any> {
        return this.activatedRoute.children[0].children[0].data;
    }
    getSecurityGuideUrl(): Observable<string> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'securityGuideUrl')
        );
    }
    useActiveThreadChart(): Observable<boolean> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'showActiveThread')
        );
    }
    getUserId(): Observable<string | undefined> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'userId')
        );
    }
    getUserDepartment(): Observable<string | undefined> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'userDepartment')
        );
    }
    useUserEdit(): Observable<boolean> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'editUserInfo')
        );
    }
    isDataUsageAllowed(): Observable<boolean> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'sendUsage')
        );
    }
    getVersion(): Observable<string> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'version')
        );
    }
    isApplicationInspectorActivated(): Observable<boolean> {
        return this.getConfigurationData().pipe(
            pluck('configuration', 'showApplicationStat')
        );
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
    getSystemDefaultInbound(): string {
        return this.componentDefaultSettingDataService.getSystemDefaultInbound();
    }
    getSystemDefaultOutbound(): string {
        return this.componentDefaultSettingDataService.getSystemDefaultOutbound();
    }
    getSystemDefaultPeriod(): Period {
        return this.componentDefaultSettingDataService.getSystemDefaultPeriod();
    }
    getSystemDefaultTransactionViewPeriod(): Period {
        return this.componentDefaultSettingDataService.getSystemDefaultTransactionViewPeriod();
    }
    getInboundList(): string[] {
        return this.componentDefaultSettingDataService.getInboundList();
    }
    getOutboundList(): string[] {
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
    private loadFavoriteList(): void {
        this.http.get<any>(this.userConfigurationURL).subscribe((userConfigurationData: IUserConfiguration) => {
            this.favoriteApplicationList = userConfigurationData.favoriteApplications;
            this.store.dispatch(new Actions.AddFavoriteApplication(this.getFavoriteApplicationList()));
        }, (error: IServerErrorFormat) => {

        });
    }
    private saveFavoriteList(newFavoriateApplicationList: IFavoriteApplication[], application: IFavoriteApplication): void {
        this.http.put<{ favoriteApplications: IFavoriteApplication[] }>(this.userConfigurationURL, {
            favoriteApplications: newFavoriateApplicationList
        }).subscribe((result: any) => {
            if (result.result === 'SUCCESS') {
                if (this.favoriteApplicationList.length > newFavoriateApplicationList.length) {
                    this.store.dispatch(new Actions.RemoveFavoriteApplication([application as IApplication]));
                } else {
                    this.store.dispatch(new Actions.AddFavoriteApplication([application as IApplication]));
                }
                this.favoriteApplicationList = newFavoriateApplicationList;
            }
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Server Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            });
        });
    }
    addFavoriteApplication(application: IApplication): void {
        const newApplication = {
            applicationName: application.getApplicationName(),
            code: application.getCode(),
            serviceType: application.getServiceType()
        };
        this.saveFavoriteList([...this.favoriteApplicationList, newApplication], newApplication);
    }
    removeFavoriteApplication(application: IApplication): void {
        const removeApplication = {
            applicationName: application.getApplicationName(),
            code: application.getCode(),
            serviceType: application.getServiceType()
        }
        const removedList = this.favoriteApplicationList.filter((data: IFavoriteApplication) => {
            return !new Application(data.applicationName, data.serviceType, data.code).equals(application);
        });
        this.saveFavoriteList(removedList, removeApplication);
    }
    private getFavoriteApplicationList(): IApplication[] {
        return this.favoriteApplicationList.map(({applicationName, serviceType, code}) => {
            return new Application(applicationName, serviceType, code);
        });
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
    setUserDefaultInbound(value: string): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.USER_DEFAULT_INBOUND, value);
    }
    getUserDefaultInbound(): string {
        return this.localStorageService.get<string>(WebAppSettingDataService.KEYS.USER_DEFAULT_INBOUND) || this.getSystemDefaultInbound();
    }
    setUserDefaultOutbound(value: string): void {
        this.localStorageService.set(WebAppSettingDataService.KEYS.USER_DEFAULT_OUTBOUND, value);
    }
    getUserDefaultOutbound(): string {
        return this.localStorageService.get<string>(WebAppSettingDataService.KEYS.USER_DEFAULT_OUTBOUND) || this.getSystemDefaultOutbound();
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
}
