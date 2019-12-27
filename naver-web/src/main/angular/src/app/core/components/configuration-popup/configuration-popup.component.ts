import { Component, OnInit, HostBinding, Output, EventEmitter, Input } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
    selector: 'pp-configuration-popup',
    templateUrl: './configuration-popup.component.html',
    styleUrls: ['./configuration-popup.component.css'],
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
                animate('0.3s')
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
                animate('0.1s')
            ])
        ])
    ]
})
export class ConfigurationPopupComponent implements OnInit {
    @HostBinding('class.font-opensans') fontFamily = true;
    @Output() outMenuClick = new EventEmitter<string>();
    @Output() outOpenLink = new EventEmitter<void>();
    @Input() canViewAdminMenu = false;

    isAdminCollapsed = false;
    isSettingCollapsed = false;

    constructor() {}
    ngOnInit() {}
    onMenuClick(type: string): void {
        this.outMenuClick.emit(type);
    }

    onOpenLink(): void {
        this.outOpenLink.emit();
    }

    toggleAdminMenu(): void {
        this.isAdminCollapsed = !this.isAdminCollapsed;
    }

    toggleSettingMenu(): void {
        this.isSettingCollapsed = !this.isSettingCollapsed;
    }

    getAdminCollapsedState(): string {
        return this.isAdminCollapsed ? 'collapsed' : 'spreaded';
    }

    getSettingCollapsedState(): string {
        return this.isSettingCollapsed ? 'collapsed' : 'spreaded';
    }

    isActive(linkElement: HTMLAnchorElement): boolean {
        const listItem = linkElement.parentElement;

        return Array.from(listItem.nextElementSibling.querySelectorAll('.l-link')).some((element: HTMLElement) => {
            return element.classList.contains('active');
        });
    }
}
