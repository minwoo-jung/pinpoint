import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class RoleListInteractionService {
    private outUserRoleListChange = new Subject<string[]>();
    private outUserRoleListUpdate = new Subject<string>();

    onUserRoleListChange$: Observable<string[]>;
    onUserRoleListUpdate$: Observable<string>;

    constructor() {
        this.onUserRoleListChange$ = this.outUserRoleListChange.asObservable();
        this.onUserRoleListUpdate$ = this.outUserRoleListUpdate.asObservable();
    }

    notifyUserRoleListChange(roleList: string[]): void {
        this.outUserRoleListChange.next(roleList);
    }

    notifyUserRoleListUpdate(userId: string): void {
        this.outUserRoleListUpdate.next(userId);
    }
}
