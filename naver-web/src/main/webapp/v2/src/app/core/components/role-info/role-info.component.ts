import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-role-info',
    templateUrl: './role-info.component.html',
    styleUrls: ['./role-info.component.css']
})
export class RoleInfoComponent implements OnInit {
    @Input() roleInfo: IPermissions;
    constructor() {}
    ngOnInit() {}
}
