
import { NgModule, ModuleWithProviders } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClipboardModule } from 'ngx-clipboard';

import { ClickOutsideModule } from 'ng-click-outside';
import { TranslateReplaceService } from './services/translate-replace.service';
import { ServerTimeDataService } from './services/server-time-data.service';
import { WebAppSettingDataService } from './services/web-app-setting-data.service';
import { ComponentDefaultSettingDataService } from './services/component-default-setting-data.service';
import { RouteInfoCollectorService } from './services/route-info-collector.service';
import { ServerTimeResolverService } from './services/server-time-resolver.service';
import { NewUrlStateNotificationService } from './services/new-url-state-notification.service';
import { UrlRouteManagerService } from './services/url-route-manager.service';
import { SystemConfigurationDataService } from './services/system-configuration-data.service';
import { SystemConfigurationResolverService } from './services/system-configuration-resolver.service';
import { SplitRatioService } from './services/split-ratio.service';
import { GutterEventService } from './services/gutter-event.service';
import { ApplicationListResolverService } from './services/application-list-resolver.service';
import { AnalyticsService } from './services/analytics.service';
import { BrowserSupportCheckService } from './services/browser-support-check.service';
import { AgentHistogramDataService } from './services/agent-histogram-data.service';
import { TransactionDetailDataService } from './services/transaction-detail-data.service';
import { StoreHelperService } from './services/store-helper.service';
import { UrlValidateGuard } from './services/url-validate.guard';

import { HeaderLogoComponent } from './components/header-logo';
import { EmptyContentsComponent, NoneComponent } from './components/empty-contents';
import { UrlRedirectorComponent } from './components/url-redirector';
import { LoadingComponent } from './components/loading';
import { FilmForDisableComponent } from './components/film-for-disable';
import { SimpleProgressSliderComponent } from './components/simple-progress-slider';
import { FormFieldErrorMessageComponent } from './components/form-field-error-message';
import { ServerErrorMessageComponent } from './components/server-error-message';
import { ContextPopupDirective } from './directives/context-popup.directive';
import { SplitterDirective } from './directives/splitter.directive';
import { SearchInputDirective } from './directives/search-input.directive';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { JSONTextParserPipe } from './pipes/json-text-parser.pipe';
import { DynamicPopupService } from 'app/shared/services/dynamic-popup.service';
import { MessageQueueService } from './services/message-queue.service';
import { WindowRefService } from './services/window-ref.service';
import { ApplicationListDataService } from './services/application-list-data.service';
import { UserConfigurationDataService } from './services/user-configuration-data.service';
import { UserConfigurationResolverService } from './services/user-configuration-resolver.service';
import { UserPermissionCheckService } from './services/user-permission-check.service';
import { FavoriteApplicationListDataService } from './services/favorite-application-list-data.service';
import { SafeStylePipe } from './pipes/safe-style.pipe';
import { RetryComponent } from './components/retry';
import { HandleObsPipe } from './pipes/handle-obs.pipe';

@NgModule({
    declarations: [
        NoneComponent,
        HeaderLogoComponent,
        EmptyContentsComponent,
        UrlRedirectorComponent,
        LoadingComponent,
        RetryComponent,
        FilmForDisableComponent,
        SimpleProgressSliderComponent,
        FormFieldErrorMessageComponent,
        ServerErrorMessageComponent,
        SafeHtmlPipe,
        SafeStylePipe,
        JSONTextParserPipe,
        HandleObsPipe,
        ContextPopupDirective,
        SplitterDirective,
        SearchInputDirective
    ],
    imports: [
        CommonModule,
        RouterModule,
        ClickOutsideModule
    ],
    exports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ClipboardModule,
        ClickOutsideModule,
        HeaderLogoComponent,
        EmptyContentsComponent,
        UrlRedirectorComponent,
        LoadingComponent,
        RetryComponent,
        FilmForDisableComponent,
        SimpleProgressSliderComponent,
        FormFieldErrorMessageComponent,
        ServerErrorMessageComponent,
        SafeHtmlPipe,
        SafeStylePipe,
        JSONTextParserPipe,
        HandleObsPipe,
        ContextPopupDirective,
        SplitterDirective,
        SearchInputDirective
    ],
    providers: []
})
export class SharedModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: SharedModule,
            providers: [
                TranslateReplaceService,
                ServerTimeDataService,
                ServerTimeResolverService,
                ComponentDefaultSettingDataService,
                RouteInfoCollectorService,
                WebAppSettingDataService,
                NewUrlStateNotificationService,
                UrlRouteManagerService,
                SystemConfigurationDataService,
                SystemConfigurationResolverService,
                SplitRatioService,
                GutterEventService,
                ApplicationListResolverService,
                AnalyticsService,
                BrowserSupportCheckService,
                AgentHistogramDataService,
                TransactionDetailDataService,
                StoreHelperService,
                UrlValidateGuard,
                DynamicPopupService,
                MessageQueueService,
                ApplicationListDataService,
                WindowRefService,
                UserConfigurationDataService,
                UserConfigurationResolverService,
                UserPermissionCheckService,
                FavoriteApplicationListDataService
            ]
        }
    }
}
