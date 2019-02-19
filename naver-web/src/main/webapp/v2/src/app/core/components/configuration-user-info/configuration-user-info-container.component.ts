import { Component, OnInit, Inject } from '@angular/core';

@Component({
    selector: 'pp-configuration-user-info-container',
    templateUrl: './configuration-user-info-container.component.html',
    styleUrls: ['./configuration-user-info-container.component.css']
})
export class ConfigurationUserInfoContainerComponent implements OnInit {
    constructor(
        @Inject('userInfo') public userInfo: any,
    ) {}

    ngOnInit() {}
}
