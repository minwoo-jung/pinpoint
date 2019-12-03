import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';

import { WebAppSettingDataService, StoreHelperService, TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { Application } from 'app/core/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-favorite-application-list-for-configuration-container',
    templateUrl: './favorite-application-list-for-configuration-container.component.html',
    styleUrls: ['./favorite-application-list-for-configuration-container.component.css']
})
export class FavoriteApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    favoriteApplicationList: IApplication[];
    applicationList: IApplication[];
    emptyText: string;
    funcImagePath: Function;
    iconBtnClassName = 'far fa-trash-alt';
    query = '';
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: ''
    };
    useDisable = true;
    showLoading = true;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
    ) {}

    ngOnInit() {
        this.initList();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.initEmptyText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initList(): void {
        this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).subscribe((favoriteApplicationList: IApplication[]) => {
            this.favoriteApplicationList = favoriteApplicationList;
            this.filterApplicationList();
        });
    }

    private filterApplicationList(): void {
        if (this.query !== '') {
            this.applicationList = this.favoriteApplicationList.filter((app: IApplication) => {
                return app.getApplicationName().toLowerCase().indexOf(this.query.toLowerCase()) !== -1;
            });
        } else {
            this.applicationList = this.favoriteApplicationList;
        }
        this.hideProcessing();
    }

    private initEmptyText(): void {
        forkJoin(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.NO_CONTENT_TO_DISPLAY')
        ).subscribe(([minLengthMessage, emptyText]: string[]) => {
            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(minLengthMessage, this.SEARCH_MIN_LENGTH);
            this.emptyText = emptyText;
        });
    }

    onClearSearch(input: HTMLInputElement): void {
        if (this.query === '') {
            return;
        }

        this.query = '';
        input.value = '';
        this.filterApplicationList();
        input.focus();
    }

    onSearch(query: string): void {
        if (this.query === query) {
            return;
        }

        this.query = query;
        this.filterApplicationList();
    }

    onSelectApp(app: IApplication): void {
        this.showProcessing();
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
            this.hideProcessing();
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_FAVORITE_APPLICATION);
    }

    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }

    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
