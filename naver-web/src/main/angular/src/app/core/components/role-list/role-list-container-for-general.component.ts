import { Component, OnInit, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-role-list-container-for-general',
    templateUrl: './role-list-container-for-general.component.html',
    styleUrls: ['./role-list-container-for-general.component.css']
})
export class RoleListContainerForGeneralComponent implements OnInit {
    @Input() userRoleList: string[];
    emptyText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
    ) {}

    ngOnInit() {
        this.emptyText$ = this.translateService.get('COMMON.EMPTY');
    }

}
