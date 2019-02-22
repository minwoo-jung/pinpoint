import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class ConfigurationUserInfoInteractionService {
    private outUserCreate = new Subject<void>();

    onUserCreate$: Observable<void>;

    constructor() {
        this.onUserCreate$ = this.outUserCreate.asObservable();
    }

    notifyUserCreate(): void {
        this.outUserCreate.next();
    }
}
