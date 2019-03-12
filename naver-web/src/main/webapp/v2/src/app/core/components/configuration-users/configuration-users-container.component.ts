import { Component, OnInit, Injector, ReflectiveInjector } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { ConfigurationUserInfoContainerComponent } from 'app/core/components/configuration-user-info/configuration-user-info-container.component';
import { ConfirmRemoveUserContainerComponent } from 'app/core/components/confirm-remove-user/confirm-remove-user-container.component';
import { ConfigurationUsersDataService, IUserInfo } from './configuration-users-data.service';
import { UserPermissionCheckService } from 'app/shared/services';
import { isThatType } from 'app/core/utils/util';

enum ViewType {
    USER_INFO,
    CONFIRM_REMOVE,
    NOTHING
}

@Component({
    selector: 'pp-configuration-users-container',
    templateUrl: './configuration-users-container.component.html',
    styleUrls: ['./configuration-users-container.component.css'],
})
export class ConfigurationUsersContainerComponent implements OnInit {
    viewType = ViewType;
    activeView: ViewType = ViewType.NOTHING;

    userInfoComponent = ConfigurationUserInfoContainerComponent;
    confirmRemoveUserComponent = ConfirmRemoveUserContainerComponent;
    userInfoInjector: Injector;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    hasUserEditPerm: boolean;
    guide$: Observable<string>;

    constructor(
        private injector: Injector,
        private configurationUsersDataService: ConfigurationUsersDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private translateService: TranslateService,
    ) {}

    ngOnInit() {
        this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.guide$ = this.translateService.get('CONFIGURATION.USERS.GUIDE');
    }

    onAddUser(): void {
        this.setErrorMessageEmpty();
        this.showUserInfoView();
        this.setInjector();
    }

    onSelectUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showProcessing();
        this.configurationUsersDataService.selectUser(userId).subscribe((data: IUserInfo | IServerErrorShortFormat) => {
            this.hideProcessing();
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : (this.showUserInfoView(), this.setInjector(data));
        });
    }

    onRemoveUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showProcessing();
        this.configurationUsersDataService.selectUser(userId).subscribe((data: IUserInfo | IServerErrorShortFormat) => {
            this.hideProcessing();
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : (this.showConfirmRemoveUserView(), this.setInjector(data));
        });
    }

    onClear(): void {
        this.activeView = ViewType.NOTHING;
    }

    onCloseErrorMessage(): void {
        this.setErrorMessageEmpty();
    }

    private setErrorMessageEmpty(): void {
        this.errorMessage = '';
    }

    private setInjector(info?: IUserInfo): void {
        this.userInfoInjector = ReflectiveInjector.resolveAndCreate([
            {
                provide: 'userInfo',
                useValue: info
            }
        ], this.injector);
    }

    private showConfirmRemoveUserView(): void {
        this.activeView = ViewType.CONFIRM_REMOVE;
    }

    private showUserInfoView(): void {
        this.activeView = ViewType.USER_INFO;
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
