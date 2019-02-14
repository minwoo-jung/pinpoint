import { Component, OnInit } from '@angular/core';
import { Subject, Observable, of, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { UrlRouteManagerService, DynamicPopupService } from 'app/shared/services';
import { UserGroupInteractionService } from 'app/core/components/user-group/user-group-interaction.service';
import { ApplicationAuthAndAlarmDataService } from './application-auth-and-alarm-data.service';
import { ApplicationAuthAndAlarmPopupComponent } from './application-auth-and-alarm-popup.component';
import { RowColumnDefinition } from 'gojs';

@Component({
    selector: 'pp-application-auth-and-alarm-info-container',
    templateUrl: './application-auth-and-alarm-info-container.component.html',
    styleUrls: ['./application-auth-and-alarm-info-container.component.css']
})
export class ApplicationAuthAndAlarmInfoContainerComponent implements OnInit {
    private unsubscribe: Subject<null> = new Subject();
    selectedUserGroupId: string;
    rowData: Observable<IApplicationAuthInfo[]>;
    i18nText: {
        SERVER_MAP: string;
        API_META: string;
        PARAM_META: string;
        SQL_META: string;
    };
    constructor(
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private dynamicPopupService: DynamicPopupService,
        private userGroupInteractionService: UserGroupInteractionService,
        private applicationAuthAndAlarmDataService: ApplicationAuthAndAlarmDataService
    ) {}
    ngOnInit() {
        this.userGroupInteractionService.onSelect$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((id: string) => {
            this.selectedUserGroupId = id;
            this.rowData = this.applicationAuthAndAlarmDataService.getData(id);
        });
        this.getI18NText();
    }
    private getI18NText(): void {
        combineLatest(
            this.translateService.get('CONFIGURATION.AUTH.SERVER_MAP'),
            this.translateService.get('CONFIGURATION.AUTH.API_META'),
            this.translateService.get('CONFIGURATION.AUTH.PARAM_META'),
            this.translateService.get('CONFIGURATION.AUTH.SQL_META'),
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                SERVER_MAP: i18n[0],
                API_META: i18n[1],
                PARAM_META: i18n[2],
                SQL_META: i18n[3]
            };
        });
    }
    onCellClicked(value: any): void {
        switch (value.type) {
            case 'configuration':
                this.showConfiguration(value);
                break;
            case 'edit':
                this.urlRouteManagerService.moveOnPage({
                    url: [
                        UrlPath.CONFIG,
                        UrlPathId.ALARM
                    ],
                    queryParam: {
                        applicationId: value.applicationId,
                        position: value.position
                    }
                });
                break;
        }
    }
    showConfiguration(value: any): void {
        const {left, top, width, height} = value.coord;

        this.dynamicPopupService.openPopup({
            data: {
                applicationId: value.applicationId,
                configuration: {
                    [this.i18nText.SERVER_MAP]: value.configuration.serverMapData,
                    [this.i18nText.API_META]: value.configuration.apiMetaData,
                    [this.i18nText.PARAM_META]: value.configuration.paramMetaData,
                    [this.i18nText.SQL_META]: value.configuration.sqlMetaData,
                }
            } ,
            coord: {
                coordX: left + width / 2 - 240,
                coordY: top + height / 2 - 130
            },
            component: ApplicationAuthAndAlarmPopupComponent
        });
    }
}
