import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-role-info',
    templateUrl: './role-info.component.html',
    styleUrls: ['./role-info.component.css']
})
export class RoleInfoComponent implements OnInit {
    @Input() roleInfo: IPermissions;
    @Input() i18nText: {[key: string]: string};
    disableFormControl = true;
    permsGroupUserGroup = 'false';

    constructor() {}
    ngOnInit() {
    }
}
