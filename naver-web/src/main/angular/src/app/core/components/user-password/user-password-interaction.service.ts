import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { IUserPassword } from 'app/core/components/user-password/user-password-data.service';

export interface IChangedPasswordState {
    isValid: boolean;
    password?: IUserPassword;
}

@Injectable()
export class UserPasswordInteractionService {
    private outUserPasswordChange = new Subject<IChangedPasswordState>();
    private outUserPasswordUpdate = new Subject<string>();

    onUserPasswordChange$: Observable<IChangedPasswordState>;
    onUserPasswordUpdate$: Observable<string>;

    constructor() {
        this.onUserPasswordChange$ = this.outUserPasswordChange.asObservable();
        this.onUserPasswordUpdate$ = this.outUserPasswordUpdate.asObservable();
    }

    notifyUserPasswordChange(password: IChangedPasswordState): void {
        this.outUserPasswordChange.next(password);
    }

    notifyUserPasswordUpdate(userId: string): void {
        this.outUserPasswordUpdate.next(userId);
    }
}
