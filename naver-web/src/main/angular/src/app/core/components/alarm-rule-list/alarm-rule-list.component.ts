import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { IAlarmRule } from './alarm-rule-data.service';

@Component({
    selector: 'pp-alarm-rule-list',
    templateUrl: './alarm-rule-list.component.html',
    styleUrls: ['./alarm-rule-list.component.css']
})
export class AlarmRuleListComponent implements OnInit {
    @Input() alarmRuleList: IAlarmRule[];
    @Input() hasUpdateAndRemoveAlarm: boolean;
    @Output() outRemove = new EventEmitter<string>();
    @Output() outEdit = new EventEmitter<string>();

    private removeConformId = '';

    constructor() {}
    ngOnInit() {}
    getNotificationType(emailSend: boolean, smsSend: boolean): string {
        return !emailSend && !smsSend ? 'None'
            : emailSend && !smsSend ? 'Email'
            : !emailSend && smsSend ? 'SMS'
            : 'Email, SMS';
    }

    onRemove(ruleId: string): void {
        this.removeConformId = ruleId;
    }

    onEdit(ruleId: string): void {
        this.outEdit.emit(ruleId);
    }

    onCancelRemove(): void {
        this.removeConformId = '';
    }

    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConformId);
        this.removeConformId = '';
    }

    isRemoveTarget(ruleId: string): boolean {
        return this.removeConformId === ruleId;
    }
}
