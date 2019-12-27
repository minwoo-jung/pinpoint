import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { RoleListInteractionService } from './role-list-interaction.service';
import { IUserRole, RoleListDataService } from './role-list-data.service';
import { isThatType } from 'app/core/utils/util';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-role-list-container-for-users',
    templateUrl: './role-list-container-for-users.component.html',
    styleUrls: ['./role-list-container-for-users.component.css']
})
export class RoleListContainerForUsersComponent implements OnInit {
    @Input() hasUserEditPerm: boolean;
    @Input() userId: string;
    @Input()
    set userRole(role: IUserRole) {
        this.userRoleList = role ? role.roleList : [];
    }

    set userRoleList(list: string[]) {
        this._userRoleList = list;
        this.isUpdated = false;
        this.roleListInteractionService.notifyUserRoleListChange(list);
    }

    get userRoleList(): string[] {
        return this._userRoleList;
    }

    roleList: string[];
    isValid: boolean;
    isUpdated = false;
    _userRoleList: string[];
    emptyText$: Observable<string>;
    errorMessage: string;
    buttonText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private roleListInteractionService: RoleListInteractionService,
        private roleListDataService: RoleListDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.buttonText$ = this.translateService.get('COMMON.SUBMIT');
        this.roleListDataService.getRoleList().subscribe((roleList: string[]) => {
            this.roleList = roleList;
        });
        this.emptyText$ = this.translateService.get('COMMON.EMPTY');
        this.isValid = true;
    }

    onAssignRole(role: string): void {
        this.userRoleList = [ ...this.userRoleList, role ];
    }

    onUnAssignRole(role: string): void {
        this.userRoleList = this.userRoleList.filter((r: string) => r !== role);
    }

    get restRoleList(): string[] {
        return this.roleList.filter((role: string) => {
            return !this.userRoleList.map((role: string) => role.toLowerCase()).includes(role.toLowerCase());
        });
    }

    onClickUpdateButton(): void {
        this.roleListDataService.update(this.userId, this.userRoleList)
            .subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                    : (
                        this.isUpdated = true,
                        this.roleListInteractionService.notifyUserRoleListUpdate(this.userId),
                        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_USER_ROLE)
                    );
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
