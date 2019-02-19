import { Component, OnInit } from '@angular/core';

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

    errorMessage: string;

    constructor() {}
    ngOnInit() {}
    onAddUser(): void {
        this.setErrorMessageEmpty();
        this.showUserInfoView();
    }

    onSelectUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showUserInfoView();
        // TODO: Fetch the selected user info
    }

    onRemoveUser(userId: string): void {
        this.setErrorMessageEmpty();
        this.showConfirmRemoveUserView();
        // TODO: Fetch the selected user info
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

    private showConfirmRemoveUserView(): void {
        this.activeView = ViewType.CONFIRM_REMOVE;
    }

    private showUserInfoView(): void {
        this.activeView = ViewType.USER_INFO;
    }
}
