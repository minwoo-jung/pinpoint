import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { IAlarmForm, IAlarmCommandForm, Alarm } from './alarm-rule-create-and-update.component';

export enum CMD_TYPE {
    CREATE = 'CREATE',
    UPDATE = 'UPDATE',
    CLOSE = 'CLOSE'
}

@Injectable()
export class AlarmInteractionService {
    private outShowInput = new Subject<IAlarmCommandForm>();
    private outComplete = new Subject<IAlarmCommandForm>();
    onShowInput$: Observable<IAlarmCommandForm>;
    onComplete$: Observable<IAlarmCommandForm>;

    constructor() {
        this.onShowInput$ = this.outShowInput.asObservable();
        this.onComplete$ = this.outComplete.asObservable();
    }
    showCreate(param: IAlarmForm): void {
        this.outShowInput.next({
            type: CMD_TYPE.CREATE,
            data: param
        });
    }
    showUpdate(param: IAlarmForm): void {
        this.outShowInput.next({
            type: CMD_TYPE.UPDATE,
            data: param
        });
    }
    completeCreate(param: Alarm): void {
        this.outComplete.next({
            type: CMD_TYPE.CREATE,
            data: param
        });
    }
    completeUpdate(param: Alarm): void {
        this.outComplete.next({
            type: CMD_TYPE.UPDATE,
            data: param
        });
    }
    completeAction(type: string, param: Alarm): void {
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
