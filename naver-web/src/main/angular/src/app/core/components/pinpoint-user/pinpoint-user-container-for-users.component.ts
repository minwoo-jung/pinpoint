import { Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { Observable, iif, of, merge, Subject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { map, switchMap, takeUntil } from 'rxjs/operators';

import { WebAppSettingDataService, TranslateReplaceService, UserPermissionCheckService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { PinpointUserForUsersDataService } from './pinpoint-user-for-users-data.service';
import { UserProfileInteractionService } from 'app/core/components/user-profile/user-profile-interaction.service';
import { ConfirmRemoveUserInteractionService } from 'app/core/components/confirm-remove-user/confirm-remove-user-interaction.service';
import { ConfigurationUserInfoInteractionService } from 'app/core/components/configuration-user-info/configuration-user-info-interaction.service';
import { isThatType } from 'app/core/utils/util';

enum MinLength {
    SEARCH = 2
}

@Component({
    selector: 'pp-pinpoint-user-container-for-users',
    templateUrl: './pinpoint-user-container-for-users.component.html',
    styleUrls: ['./pinpoint-user-container-for-users.component.css']
})
export class PinpointUserContainerForUsersComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private searchQuery: string;

    @Output() outAddUser = new EventEmitter<void>();
    @Output() outSelectUser = new EventEmitter<string>();
    @Output() outRemoveUser = new EventEmitter<string>();
    @Output() outClear = new EventEmitter<void>();

    hasUserEditPerm: boolean;
    searchGuideText$: Observable<string>;
    pinpointUserList: IUserProfile[] = [];
    errorMessage: string;
    searchUseEnter = true;
    minLengthConst = MinLength;
    useDisable = true;
    showLoading = true;
    loggedInUserId: string;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private pinpointUserForUsersDataService: PinpointUserForUsersDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private userProfileInteractionService: UserProfileInteractionService,
        private confirmRemoveUserInteractionService: ConfirmRemoveUserInteractionService,
        private configurationUserInfoInteractionService: ConfigurationUserInfoInteractionService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.searchGuideText$ = this.translateService.get('COMMON.MIN_LENGTH').pipe(
            map((text: string) => this.translateReplaceService.replace(text, MinLength.SEARCH))
        );
        this.webAppSettingDataService.getUserId().subscribe((userId: string) => {
            this.loggedInUserId = userId;
        });

        merge(
            this.configurationUserInfoInteractionService.onUserCreate$,
            this.userProfileInteractionService.onUserProfileUpdate$,
            this.confirmRemoveUserInteractionService.onUserRemove$
        ).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => {
            this.getPinpointUserList(this.searchQuery);
        });

        this.getPinpointUserList();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getPinpointUserList(query?: string): void  {
        this.showProcessing();
        iif(() => !!query,
            of(query),
            this.webAppSettingDataService.getUserDepartment()
        ).pipe(
            switchMap((department?: string) => this.pinpointUserForUsersDataService.retrieve(department))
        ).subscribe((result: IUserProfile[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.pinpointUserList = result;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
            this.hideProcessing();
        });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    onSearch(query: string): void {
        this.searchQuery = query;
        this.getPinpointUserList(this.searchQuery);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_USER_IN_USERS);
    }

    onReload(): void {
        this.getPinpointUserList(this.searchQuery);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_USER_LIST_IN_USERS);
        this.outClear.emit();
    }

    onAddUser(): void {
        this.outAddUser.emit();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_CREATION_FORM);
    }

    onSelectUser(id: string): void {
        this.outSelectUser.emit(id);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_UPDATE_FORM);
    }

    onRemoveUser(id: string): void {
        this.outRemoveUser.emit(id);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_REMOVE_CONFIRM_VIEW);
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
