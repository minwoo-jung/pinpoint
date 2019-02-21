import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class ConfigurationUserInfoInteractionService {
    private outUserCreate = new Subject<string>();

    onUserCreate$: Observable<string>;

    constructor() {
        this.onUserCreate$ = this.outUserCreate.asObservable();
    }

    notifyUserCreate(userId: string): void {
        this.outUserCreate.next(userId);
    }
}
