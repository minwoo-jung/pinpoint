import { Component, OnInit } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

import { UserPermissionCheckService, NewUrlStateNotificationService, UrlRouteManagerService, TRACKED_EVENT_LIST, AnalyticsService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css'],
    animations: [
        trigger('collapseSpread', [
            state('collapsed', style({
                maxHeight: 0,
                overflow: 'hidden'
            })),
            state('spreaded', style({
                maxHeight: '300px'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.5s')
            ])
        ]),
        trigger('rightDown', [
            state('collapsed', style({
                transform: 'none'
            })),
            state('spreaded', style({
                transform: 'rotate(90deg)'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.3s')
            ])
        ])
    ]
})
export class ConfigPageComponent implements OnInit {
    canViewAdminMenu: boolean;
    isCollapsed = false;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private userPermissionCheckService: UserPermissionCheckService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.canViewAdminMenu = this.userPermissionCheckService.canViewAdminMenu();
    }

    onClickExit(): void {
        const { startPath, pathParams, queryParams } = this.newUrlStateNotificationService.getPrevPageUrlInfo();
        const url = startPath === UrlPath.CONFIG ? [UrlPath.MAIN] : [startPath, ...[ ...pathParams.values() ]];
        const queryParam = [ ...queryParams.entries() ].reduce((acc: object, [key, value]: string[]) => {
            return { ...acc, [key]: value };
        }, {});

        this.urlRouteManagerService.moveOnPage({ url, queryParam });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_PAGE_EXIT_BUTTON);
    }

    toggleMenu(): void {
        this.isCollapsed = !this.isCollapsed;
    }

    getCollapsedState(): string {
        return this.isCollapsed ? 'collapsed' : 'spreaded';
    }

    isActive(linkElement: HTMLAnchorElement): boolean {
        const listItem = linkElement.parentElement;

        return Array.from(listItem.nextElementSibling.querySelectorAll('.l-link')).some((element: HTMLElement) => {
            return element.classList.contains('active');
        });
    }

    onMenuClick(menu: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_MENU, menu);
    }
}
