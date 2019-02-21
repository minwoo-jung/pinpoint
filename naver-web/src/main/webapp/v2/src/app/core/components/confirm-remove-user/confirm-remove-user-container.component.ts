import { Component, OnInit, Inject } from '@angular/core';

import { ConfirmRemoveUserInteractionService } from './confirm-remove-user-interaction.service';
import { ConfirmRemoveUserDataService } from './confirm-remove-user-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-confirm-remove-user-container',
    templateUrl: './confirm-remove-user-container.component.html',
    styleUrls: ['./confirm-remove-user-container.component.css']
})
export class ConfirmRemoveUserContainerComponent implements OnInit {
    errorMessage: string;
    isUserRemoved: boolean;

    constructor(
        @Inject('userInfo') public userInfo: any,
        private confirmRemoveUserInteractionService: ConfirmRemoveUserInteractionService,
        private confirmRemoveUserDataService: ConfirmRemoveUserDataService,
    ) {}

    ngOnInit() {}
    onUserRemove(userId: string): void {
        this.confirmRemoveUserDataService.removeUser(userId)
            .subscribe((result: IUserRequestSuccessResponse | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                    : (this.isUserRemoved = true, this.confirmRemoveUserInteractionService.notifyUserRemove(result.userId));
            });
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
