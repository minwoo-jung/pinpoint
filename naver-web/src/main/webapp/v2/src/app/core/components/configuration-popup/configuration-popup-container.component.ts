import { Component, OnInit, Output, EventEmitter, AfterViewInit, Input, ElementRef } from '@angular/core';

import { DynamicPopup, WindowRefService, PopupConstant, UrlRouteManagerService, UserPermissionCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-popup-container',
    templateUrl: './configuration-popup-container.component.html',
    styleUrls: ['./configuration-popup-container.component.css']
})
export class ConfigurationPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() coord: ICoordinate;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    private posX: number;

    canViewAdminMenu: boolean;


    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private windowRefService: WindowRefService,
        private el: ElementRef,
        private userPermissionCheckService: UserPermissionCheckService,
    ) {}

    ngOnInit() {
        this.posX = this.windowRefService.nativeWindow.innerWidth - this.el.nativeElement.offsetWidth;
        this.canViewAdminMenu = this.userPermissionCheckService.canViewAdminMenu();
    }

    ngAfterViewInit() {
        this.outCreated.emit({
            coordX: this.posX,
            coordY: this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT
        });
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const { coordX } = this.coord;

        return `${coordX - this.posX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    onInputChange(): void {
        this.outClose.emit();
    }

    onMenuClick(type: string): void {
        this.urlRouteManagerService.moveToConfigPage(type);
        this.outClose.emit();
    }

    onOpenLink(): void {
        this.windowRefService.nativeWindow.open('https://yobi.navercorp.com/Labs-public_pinpoint-issues/posts');
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
