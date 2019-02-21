import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class ConfirmRemoveUserInteractionService {
    private outUserRemove = new Subject<string>();

    onUserRemove$: Observable<string>;

    constructor() {
        this.onUserRemove$ = this.outUserRemove.asObservable();
    }

    notifyUserRemove(userId: string): void {
        this.outUserRemove.next(userId);
    }
}
