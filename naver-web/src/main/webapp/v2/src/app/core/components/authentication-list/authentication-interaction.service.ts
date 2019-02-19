import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { IAuthorityForm, IAuthorityCommandForm, IAuthorityData } from './authentication-create-and-update.component';

export enum CMD_TYPE {
    CREATE = 'CREATE',
    UPDATE = 'UPDATE',
    CLOSE = 'CLOSE'
}

@Injectable()
export class AuthenticationInteractionService {
    private outShowInput = new Subject<IAuthorityCommandForm>();
    private outComplete = new Subject<IAuthorityCommandForm>();
    onShowInput$: Observable<IAuthorityCommandForm>;
    onComplete$: Observable<IAuthorityCommandForm>;

    constructor() {
        this.onShowInput$ = this.outShowInput.asObservable();
        this.onComplete$ = this.outComplete.asObservable();
    }
    showCreate(param: IAuthorityForm): void {
        this.outShowInput.next({
            type: CMD_TYPE.CREATE,
            data: param
        });
    }
    showUpdate(param: IAuthorityForm): void {
        this.outShowInput.next({
            type: CMD_TYPE.UPDATE,
            data: param
        });
    }
    completeCreate(param: IAuthorityData): void {
        this.outComplete.next({
            type: CMD_TYPE.CREATE,
            data: param
        });
    }
    completeUpdate(param: IAuthorityData): void {
        this.outComplete.next({
            type: CMD_TYPE.UPDATE,
            data: param
        });
    }
    completeAction(type: string, param: IAuthorityData): void {
        this.outComplete.next({
            type: type,
            data: param
        });
    }
    closeInput(): void {
        this.outComplete.next({
            type: CMD_TYPE.CLOSE
        });
    }
}
