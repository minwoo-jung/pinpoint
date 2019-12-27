import { Component, OnInit, ViewContainerRef, ViewChild, ComponentRef, ComponentFactoryResolver, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { ConfigurationUserInfoContainerComponent } from 'app/core/components/configuration-user-info/configuration-user-info-container.component';
import { ConfirmRemoveUserContainerComponent } from 'app/core/components/confirm-remove-user/confirm-remove-user-container.component';
import { ConfigurationUsersDataService, IUserInfo } from './configuration-users-data.service';
import { UserPermissionCheckService } from 'app/shared/services';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-configuration-users-container',
    templateUrl: './configuration-users-container.component.html',
    styleUrls: ['./configuration-users-container.component.css'],
})
export class ConfigurationUsersContainerComponent implements OnInit, OnDestroy {
    @ViewChild('templateContainer', { read: ViewContainerRef, static: true }) templateContainer: ViewContainerRef;

    private componentRef: ComponentRef<any>;

    showTemplate = false;
    errorMessage: string;
    useDisable = false;
    showLoading = false;
    hasUserEditPerm: boolean;
    guide$: Observable<string>;

    constructor(
        private resolver: ComponentFactoryResolver,
        private configurationUsersDataService: ConfigurationUsersDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private translateService: TranslateService,
    ) {}

    ngOnInit() {
        this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.guide$ = this.translateService.get('CONFIGURATION.USERS.GUIDE');
    }

    ngOnDestroy() {
        this.clearContainer();
    }

    onAddUser(): void {
        this.setErrorMessageEmpty();
        this.showTemplate = true;
        this.handleTemplate(ConfigurationUserInfoContainerComponent, null);
    }

    onSelectUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showProcessing();
        this.configurationUsersDataService.selectUser(userId).subscribe((data: IUserInfo | IServerErrorShortFormat) => {
            this.hideProcessing();
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : (
                    this.showTemplate = true,
                    this.handleTemplate(ConfigurationUserInfoContainerComponent, data)
                );
        });
    }

    onRemoveUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showProcessing();
        this.configurationUsersDataService.selectUser(userId).subscribe((data: IUserInfo | IServerErrorShortFormat) => {
            this.hideProcessing();
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : (
                    this.showTemplate = true,
                    this.handleTemplate(ConfirmRemoveUserContainerComponent, data)
                );
        });
    }

    onClear(): void {
        this.showTemplate = false;
        this.clearContainer();
    }

    onCloseErrorMessage(): void {
        this.setErrorMessageEmpty();
    }

    private setErrorMessageEmpty(): void {
        this.errorMessage = '';
    }

    private initComponent(component: any, data: IUserInfo | null): void {
        const componentFactory = this.resolver.resolveComponentFactory(component);
            
        this.componentRef = this.templateContainer.createComponent(componentFactory);
        this.bindInputProps(this.componentRef.instance, data);
    }

    private bindInputProps(component: any, data: IUserInfo | null): void {
        component.userInfo = data;
    }

    private clearContainer(): void {
        this.templateContainer.clear();
        if (this.componentRef) {
            this.componentRef.destroy();
        }
        this.componentRef = null;
    }

    private handleTemplate(component: any, data: IUserInfo | null): void {
        if (!this.componentRef) {
            this.initComponent(component, data);
        } else if (this.componentRef && this.componentRef.instance instanceof component) {
            this.bindInputProps(this.componentRef.instance, data);
        } else {
            this.templateContainer.clear();
            this.componentRef.destroy();
            this.componentRef = null;
            this.initComponent(component, data);
        }
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
