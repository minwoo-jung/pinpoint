import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { UserPermissionCheckService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-general-container',
    templateUrl: './configuration-general-container.component.html',
    styleUrls: ['./configuration-general-container.component.css'],
})
export class ConfigurationGeneralContainerComponent implements OnInit {
    desc$: Observable<string>;
    showUserInfo: boolean;

    constructor(
        private translateService: TranslateService,
        private userPermissionCheckService: UserPermissionCheckService
    ) {}

    ngOnInit() {
        this.initDescText();
        this.showUserInfo = this.userPermissionCheckService.canViewAdminMenu();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.GENERAL.DESC');
    }
}
