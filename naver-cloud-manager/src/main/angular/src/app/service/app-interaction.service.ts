import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AppInteractionService {
    private outAppSelected = new BehaviorSubject<string>('');
    private outAgentSelected = new BehaviorSubject<string>('');

    onAppSelected$: Observable<string>;
    onAgentSelected$: Observable<string>;

    constructor() {
        this.onAppSelected$ = this.outAppSelected.asObservable();
        this.onAgentSelected$ = this.outAgentSelected.asObservable();
    }

    onAppSelect(app: string): void {
        this.outAppSelected.next(app);
    }

    onAgentSelect(agent: string): void {
        this.outAgentSelected.next(agent);
    }
}
