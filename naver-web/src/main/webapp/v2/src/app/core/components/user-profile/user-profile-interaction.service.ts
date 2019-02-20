import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { IUserProfile } from 'app/core/components/user-profile/user-profile-data.service';

export interface IChangedProfileState {
    isValid: boolean;
    profile?: IUserProfile;
}

@Injectable()
export class UserProfileInteractionService {
    private outUserProfileChange = new Subject<IChangedProfileState>();
    private outUserProfileUpdate = new Subject<string>();

    onUserProfileChange$: Observable<IChangedProfileState>;
    onUserProfileUpdate$: Observable<string>;

    constructor() {
        this.onUserProfileChange$ = this.outUserProfileChange.asObservable();
        this.onUserProfileUpdate$ = this.outUserProfileUpdate.asObservable();
    }

    notifyUserProfileChange(change: IChangedProfileState): void {
        this.outUserProfileChange.next(change);
    }

    notifyUserProfileUpdate(userId: string): void {
        this.outUserProfileUpdate.next(userId);
    }
}
