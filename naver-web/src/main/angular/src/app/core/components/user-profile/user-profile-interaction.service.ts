import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface IChangedProfileState {
    isValid: boolean;
    profile?: IUserProfile;
}

@Injectable()
export class UserProfileInteractionService {
    private outUserProfileChange = new Subject<IChangedProfileState>();
    private outUserProfileUpdate = new Subject<void>();

    onUserProfileChange$: Observable<IChangedProfileState>;
    onUserProfileUpdate$: Observable<void>;

    constructor() {
        this.onUserProfileChange$ = this.outUserProfileChange.asObservable();
        this.onUserProfileUpdate$ = this.outUserProfileUpdate.asObservable();
    }

    notifyUserProfileChange(change: IChangedProfileState): void {
        this.outUserProfileChange.next(change);
    }

    notifyUserProfileUpdate(): void {
        this.outUserProfileUpdate.next();
    }
}
