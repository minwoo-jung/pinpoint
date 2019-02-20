import { Component, OnInit } from '@angular/core';
import { combineLatest } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, UserPermissionCheckService, UserConfigurationDataService } from 'app/shared/services';
import { UserGroupInteractionService } from './user-group-interaction.service';
import { UserGroupDataService, IUserGroup, IUserGroupCreated, IUserGroupDeleted } from './user-group-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-group-container',
    templateUrl: './user-group-container.component.html',
    styleUrls: ['./user-group-container.component.css']
})
export class UserGroupContainerComponent implements OnInit {
    private searchQuery = '';
    private userId = '';
    canAddUserGroup: boolean;
    i18nLabel = {
        NAME_LABEL: '',
    };
    i18nGuide: { [key: string]: IFormFieldErrorType };
    i18nText = {
        SEARCH_INPUT_GUIDE: ''
    };
    USER_GROUP_NAME_MIN_LENGTH = 3;
    SEARCH_MIN_LENGTH = 2;
    searchUseEnter = true;
    userGroupList: IUserGroup[] = [];
    useDisable = true;
    showLoading = true;
    showCreate = false;
    errorMessage: string;
    selectedUserGroupId = '';
    constructor(
        private userConfigurationDataService: UserConfigurationDataService,
        private userGroupDataService: UserGroupDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userPermissionCheckService: UserPermissionCheckService,
        private userGroupInteractionService: UserGroupInteractionService
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.userId = this.userConfigurationDataService.getUserId();
        if (this.userPermissionCheckService.canEditAllUserGroup()) {
            this.getUserGroupList();
        } else {
            this.getUserGroupList({userId: this.userId});
        }
        this.canAddUserGroup = this.userPermissionCheckService.canAddUserGroup();
    }
    private getI18NText(): void {
        combineLatest(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.NAME')
        ).subscribe(([minLengthMessage, requiredMessage, nameLabel]: string[]) => {
            this.i18nGuide = {
                userGroupName: {
                    required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                    minlength: this.translateReplaceService.replace(minLengthMessage, this.USER_GROUP_NAME_MIN_LENGTH)
                }
            };

            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(minLengthMessage, this.SEARCH_MIN_LENGTH);
            this.i18nLabel.NAME_LABEL = nameLabel;
        });
    }
    private getUserGroupList(params?: any): void  {
        this.userGroupDataService.retrieve(params).subscribe((data: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : this.userGroupList = data as IUserGroup[];
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    private makeUserGroupQuery(): any {
        return this.searchQuery === '' ? {
            userId: this.userId
        } : {
            userGroupId: this.searchQuery
        };
    }
    onRemoveUserGroup(id: string): void {
        this.showProcessing();
        this.userGroupDataService.remove(id, this.userId).subscribe((response: IUserGroupDeleted | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                if (response.result === 'SUCCESS') {
                    this.userGroupInteractionService.setSelectedUserGroup('');
                    this.getUserGroupList(this.makeUserGroupQuery());
                } else {
                    this.hideProcessing();
                }
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onCreateUserGroup(newUserGroupName: string): void {
        this.showProcessing();
        this.userGroupDataService.create(newUserGroupName, this.userId).subscribe((data: IUserGroupCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.errorMessage = data.errorMessage;
            } else {
                this.userGroupList.push({
                    id: newUserGroupName,
                    number: data.number
                });
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onCloseCreateUserPopup(): void {
        this.showCreate = false;
    }
    onShowCreateUserPopup(): void {
        this.showCreate = true;
    }
    onSelectUserGroup(userGroupId: string): void {
        this.selectedUserGroupId = userGroupId;
        this.userGroupInteractionService.setSelectedUserGroup(userGroupId);
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    onReload(): void {
        this.showProcessing();
        this.getUserGroupList(this.makeUserGroupQuery());
    }
    onSearch(query: string): void {
        this.showProcessing();
        this.searchQuery = query;
        this.getUserGroupList(this.makeUserGroupQuery());
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
