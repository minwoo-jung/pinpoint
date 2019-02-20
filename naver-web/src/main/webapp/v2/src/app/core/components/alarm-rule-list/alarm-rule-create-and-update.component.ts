import { Component, OnInit, Input } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AlarmInteractionService, CMD_TYPE } from './alarm-interaction.service';

export enum NOTIFICATION_TYPE {
    ALL = 'all',
    EMAIL = 'email',
    SMS = 'sms'
}
export class Alarm {
    public ruleId: string;
    public smsSend: boolean;
    public emailSend: boolean;
    constructor(
        public applicationId: string,
        public serviceType: string,
        public checkerName: string,
        public userGroupId: string,
        public threshold: number,
        public type: string,
        public notes?: string
    ) {
        this.setTypeInternalStatus();
    }
    setTypeInternalStatus(): void {
        this.smsSend = this.type === NOTIFICATION_TYPE.ALL || this.type === NOTIFICATION_TYPE.SMS ? true : false;
        this.emailSend = this.type === NOTIFICATION_TYPE.ALL || this.type === NOTIFICATION_TYPE.EMAIL ? true : false;
    }
}

export interface IAlarmForm {
    applicationId: string;
    serviceType: string;
    userGroupList: string[];
    checkerList: string[];
    data?: Alarm;
}
export interface IAlarmCommandForm {
    type: string;
    data?: IAlarmForm | Alarm;
}

export interface ILabel {
    CHECKER_LABEL: string;
    USER_GROUP_LABEL: string;
    THRESHOLD_LABEL: string;
    TYPE_LABEL: string;
    NOTES_LABEL: string;
}

export interface IGuide {
    CHECKER_REQUIRED: string;
    USER_GROUP_REQUIRED: string;
    THRESHOLD_REQUIRED: string;
    TYPE_REQUIRED: string;
}

@Component({
    selector: 'pp-alarm-rule-create-and-update',
    templateUrl: './alarm-rule-create-and-update.component.html',
    styleUrls: ['./alarm-rule-create-and-update.component.css']
})
export class AlarmRuleCreateAndUpdateComponent implements OnInit {
    @Input() i18nLabel: any;
    @Input() i18nGuide: any;
    title = 'Alarm';
    showForm: boolean;
    userGroupList: string[];
    checkerList: string[];
    alarmForm: FormGroup;
    actionParam: IAlarmCommandForm;

    constructor(private alarmInteractionService: AlarmInteractionService) {}
    ngOnInit() {
        this.alarmForm = new FormGroup({
            'checkerName': new FormControl('', [Validators.required]),
            'userGroupId': new FormControl('', [Validators.required]),
            'threshold': new FormControl(``, [Validators.required, Validators.min(1)]),
            'type': new FormControl('', []),
            'notes': new FormControl('', [])
        });
        this.alarmInteractionService.onShowInput$.subscribe((param: IAlarmCommandForm) => {
            this.actionParam = param;
            switch (param.type) {
                case CMD_TYPE.CREATE:
                    this.createForm(param.data as IAlarmForm);
                    break;
                case CMD_TYPE.UPDATE:
                    this.updateForm(param.data as IAlarmForm);
                    break;
            }
            this.showForm = true;
        });
    }
    private createForm(param: IAlarmForm): void {
        this.checkerList = param.checkerList;
        this.userGroupList = param.userGroupList;
        this.setValue('', '', 1, NOTIFICATION_TYPE.ALL, '');
    }
    private updateForm(param: IAlarmForm): void {
        this.checkerList = param.checkerList;
        this.userGroupList = param.userGroupList;
        this.setValue(
            param.data.checkerName,
            param.data.userGroupId,
            param.data.threshold,
            param.data.type,
            param.data.notes
        );
    }
    private setValue(checkerName: string, userGroupId: string, threshold: number, type: string, notes: string): void {
        this.alarmForm.get('checkerName').setValue(checkerName);
        this.alarmForm.get('userGroupId').setValue(userGroupId);
        this.alarmForm.get('threshold').setValue(threshold);
        this.alarmForm.get('type').setValue(type);
        this.alarmForm.get('notes').setValue(notes);
    }
    onCreateOrUpdate() {
        const alarm = new Alarm(
            this.actionParam.data.applicationId,
            this.actionParam.data.serviceType,
            this.alarmForm.get('checkerName').value,
            this.alarmForm.get('userGroupId').value,
            this.alarmForm.get('threshold').value,
            this.alarmForm.get('type').value,
            this.alarmForm.get('notes').value
        );
        if (this.actionParam.type === CMD_TYPE.UPDATE) {
            alarm.ruleId = ((this.actionParam.data as IAlarmForm).data as Alarm).ruleId;
        }
        this.alarmInteractionService.completeAction(this.actionParam.type, alarm);
        this.onClose();
    }
    onClose() {
        this.alarmForm.reset();
        this.showForm = false;
        this.alarmInteractionService.closeInput();
    }
    get checkerName() {
        return this.alarmForm.get('checkerName');
    }
    get userGroupId() {
        return this.alarmForm.get('userGroupId');
    }
    get threshold() {
        return this.alarmForm.get('threshold');
    }
    get type() {
        return this.alarmForm.get('type');
    }
    get notes() {
        return this.alarmForm.get('notes');
    }
}
