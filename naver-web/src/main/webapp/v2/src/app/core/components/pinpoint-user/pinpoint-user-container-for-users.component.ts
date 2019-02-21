import { Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { Observable, iif, of, Subject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { map, switchMap, takeUntil } from 'rxjs/operators';

import { WebAppSettingDataService, TranslateReplaceService, UserPermissionCheckService } from 'app/shared/services';
import { PinpointUserForUsersDataService } from './pinpoint-user-for-users-data.service';
import { UserProfileInteractionService } from 'app/core/components/user-profile/user-profile-interaction.service';
import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';
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
    loggedInUserId$: Observable<string>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private pinpointUserForUsersDataService: PinpointUserForUsersDataService,
        private userPermissionCheckService: UserPermissionCheckService,
        private userProfileInteractionService: UserProfileInteractionService,
    ) {}

    ngOnInit() {
        // this.hasUserEditPerm = this.userPermissionCheckService.canEditUser();
        this.hasUserEditPerm = true;
        this.searchGuideText$ = this.translateService.get('COMMON.MIN_LENGTH').pipe(
            map((text: string) => this.translateReplaceService.replace(text, MinLength.SEARCH))
        );
        this.loggedInUserId$ = this.webAppSettingDataService.getUserId();
        this.userProfileInteractionService.onUserProfileUpdate$.pipe(
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
    }

    onReload(): void {
        this.getPinpointUserList(this.searchQuery);
        this.outClear.emit();
    }

    onAddUser(): void {
        this.outAddUser.emit();
    }

    onSelectUser(id: string): void {
        this.outSelectUser.emit(id);
    }

    onRemoveUser(id: string): void {
        this.outRemoveUser.emit(id);
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
