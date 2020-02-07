import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';

import { WebAppSettingDataService, StoreHelperService, TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { Application } from 'app/core/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-favorite-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css']
})
export class FavoriteApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private favoriteAppList: IApplication[];

    filteredAppList: IApplication[];
    funcImagePath: Function;
    iconBtnClassName = 'far fa-trash-alt';
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: '',
        EMPTY: ''
    };
    isEmpty: boolean;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
    ) {}

    ngOnInit() {
        this.initList();
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initList(): void {
        this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).subscribe((favoriteAppList: IApplication[]) => {
            this.favoriteAppList = favoriteAppList;
            this.filterList();
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

    private filterList(): void {
        if (this.query !== '') {
            this.filteredAppList = this.favoriteAppList.filter((app: IApplication) => {
                return app.getApplicationName().toLowerCase().indexOf(this.query.toLowerCase()) !== -1;
            });
        } else {
            this.filteredAppList = this.favoriteAppList;
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
        this.webAppSettingDataService.removeFavoriteApplication(app).subscribe(({applicationName, serviceType, code}: IFavoriteApplication) => {
            this.storeHelperService.dispatch(new Actions.RemoveFavoriteApplication([new Application(applicationName, serviceType, code)]));
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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_FAVORITE_APPLICATION);
    }
}
