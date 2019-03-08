import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UrlPath } from 'app/shared/models';
import { SystemConfigurationResolverService, ApplicationListResolverService, UserConfigurationResolverService } from 'app/shared/services';
import { PageNotFoundComponent } from 'app/shared/components/page-not-found';

const appRoutes: Routes = [
    {
        path: '',
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService,
            userConfiguration: UserConfigurationResolverService,
        },
        children: [
            {
                path: UrlPath.CONFIG,
                loadChildren: './routes/config-page/index#ConfigPageModule'
            },
            {
                path: UrlPath.ADMIN,
                loadChildren: './routes/admin-page/index#AdminPageModule'
            },
            {
                path: UrlPath.ERROR,
                loadChildren: './routes/error-page/index#ErrorPageModule'
            },
            {
                path: UrlPath.BROWSER_NOT_SUPPORT,
                loadChildren: './routes/browser-support-page/index#BrowserSupportPageModule'
            },
            {
                path: UrlPath.SCATTER_FULL_SCREEN_MODE,
                loadChildren: './routes/scatter-full-screen-mode-page/index#ScatterFullScreenModePageModule'
            },
            {
                path: UrlPath.THREAD_DUMP,
                loadChildren: './routes/thread-dump-page/index#ThreadDumpPageModule'
            },
            {
                path: UrlPath.REAL_TIME,
                loadChildren: './routes/real-time-page/index#RealTimePageModule'
            },
            {
                path: UrlPath.TRANSACTION_VIEW,
                loadChildren: './routes/transaction-view-page/index#TransactionViewPageModule'
            },
            {
                path: UrlPath.TRANSACTION_DETAIL,
                loadChildren: './routes/transaction-detail-page/index#TransactionDetailPageModule'
            },
            {
                path: UrlPath.TRANSACTION_LIST,
                loadChildren: './routes/transaction-list-page/index#TransactionListPageModule'
            },
            {
                path: UrlPath.INSPECTOR,
                loadChildren: './routes/inspector-page/index#InspectorPageModule'
            },
            {
                path: UrlPath.FILTERED_MAP,
                loadChildren: './routes/filtered-map-page/index#FilteredMapPageModule'
            },
            {
                path: UrlPath.MAIN,
                loadChildren: './routes/main-page/index#MainPageModule'
            },
            {
                path: '',
                redirectTo: '/' + UrlPath.MAIN,
                pathMatch: 'full'
            },
            {
                path: '**',
                component: PageNotFoundComponent
            }
        ]
    }
];

@NgModule({
    imports: [
        RouterModule.forRoot(appRoutes, { enableTracing: false })
    ],
    exports: [
        RouterModule
    ]
})
export class AppRoutingModule {}
