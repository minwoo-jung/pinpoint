
import { Routes } from '@angular/router';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { MainContentsContainerComponent } from 'app/core/components/main-contents/main-contents-container.component';
import { EmptyContentsComponent, NoneComponent } from 'app/shared/components/empty-contents';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector';
// import { UrlValidateGuard } from 'app/shared/services';
import { UserConfigurationResolverService, SystemConfigurationResolverService, ApplicationListResolverService, ServerTimeResolverService } from 'app/shared/services';
import { MainPageComponent } from './main-page.component';

export const routing: Routes = [
    {
        path: '',
        component: MainPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            userConfiguration: UserConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: '',
                component: EmptyContentsComponent,
                data: {
                    showRealTimeButton: false,
                    enableRealTimeMode: false
                },
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                children: [
                    {
                        path: '',
                        data: {
                            path: UrlPath.MAIN
                        },
                        component: UrlRedirectorComponent
                    },
                    {
                        path: UrlPath.REAL_TIME,
                        resolve: {
                            serverTime: ServerTimeResolverService
                        },
                        children: [
                            {
                                path: '',
                                component: MainContentsContainerComponent,
                                data: {
                                    showRealTimeButton: true,
                                    enableRealTimeMode: true
                                }
                            }
                        ]
                    },
                    {
                        path: ':' + UrlPathId.PERIOD,
                        children: [
                            {
                                path: '',
                                data: {
                                    path: UrlPath.MAIN
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                children: [
                                    {
                                        path: '',
                                        component: MainContentsContainerComponent,
                                        data: {
                                            showRealTimeButton: true,
                                            enableRealTimeMode: false
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    }
];
