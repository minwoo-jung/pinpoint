import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { WindowRefService } from 'app/shared/services';

@Component({
    selector: 'pp-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    constructor(
        private windowRefService: WindowRefService,
        private translateService: TranslateService
    ) {
        const supportLanguages = ['en', 'ko'];
        const defaultLang = 'en';
        const currentLang = this.windowRefService.nativeWindow.navigator.language.substring(0, 2);
        this.translateService.addLangs(supportLanguages);
        this.translateService.setDefaultLang(defaultLang);
        if (supportLanguages.findIndex((lang: string) => {
            return lang === currentLang;
        })) {
            this.translateService.use(currentLang);
        } else {
            this.translateService.use(defaultLang);
        }
        this.translateService.getTranslation(currentLang).subscribe((translate) => {
            if (currentLang === 'ko') {
                translate.CONFIGURATION.COMMON.ROLE = '권한';
                translate.CONFIGURATION.AUTH = {
                    SERVER_MAP: 'ServerMap 데이터 비노출',
                    API_META: 'API 데이터 비노출',
                    PARAM_META: 'Parameter 데이터 비노출',
                    SQL_META: 'SQL 데이터 비노출'
                };
                translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['문제 발생시 핀포인트 개발팀에 문의 해 주세요. 해결 해 드립니다.'];

                translate.CONFIGURATION.PERMISSION = {};
                translate.CONFIGURATION.PERMISSION.ADMIN_MENU_TITLE = 'Admin 메뉴';
                translate.CONFIGURATION.PERMISSION.VIEW_ADMIN_MENU = 'Admin menu 보기';
                translate.CONFIGURATION.PERMISSION.EDIT_USER_TITLE = 'User 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_USER = 'User 추가 삭제, Role 할당';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE_TITLE = 'Role 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE = 'Role 추가 삭제, Permission 수정';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY_TITLE = '최초 할당';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY = 'User Group 최초 할당 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_TITLE = 'Authentication 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_FOR_EVERYTHING = '모든 Authentication 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_ONLY_MANAGER = 'Position이 Manager인 User Group에 소속된 경우에만 Authentication 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_TITLE = 'Alarm 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_FOR_EVERYTHING = '모든 Alarm 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_ONLY_GROUP_MEMBER = 'Position이 Manager인 User Group에 소속된 Member만 Alarm 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_TITLE = 'User Group 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_FOR_EVERYTHING = 'User Group 생성 및 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_ONLY_GROUP_MEMBER = 'User Group의 Member 만 생성 및 수정 가능';
            } else {
                translate.CONFIGURATION.COMMON.ROLE = 'Role';
                translate.CONFIGURATION.AUTH = {
                    SERVER_MAP: 'Hide ServerMap data',
                    API_META: 'Hide API data',
                    PARAM_META: 'Hide Parameter data',
                    SQL_META: 'Hide SQL data'
                };
                translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['Please contact the pinpoint development team in case of problems. We will solve it.'];

                translate.CONFIGURATION.PERMISSION = {};
                translate.CONFIGURATION.PERMISSION.ADMIN_MENU_TITLE = 'Admin Menu';
                translate.CONFIGURATION.PERMISSION.VIEW_ADMIN_MENU = 'View admin menu';
                translate.CONFIGURATION.PERMISSION.EDIT_USER_TITLE = 'User Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_USER = 'Edit User Info';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE_TITLE = 'Role Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE = 'Edit Role Info';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY_TITLE = 'Preoccupancy';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY = 'Can have the authority first';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_TITLE = 'Authentication Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_FOR_EVERYTHING = 'Edit author for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_ONLY_MANAGER = 'Edit Author only manager';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_TITLE = 'Alarm Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_FOR_EVERYTHING = 'Edit alarm for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_ONLY_GROUP_MEMBER = 'Edit alarm only group member';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_TITLE = 'User Group Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_FOR_EVERYTHING = 'Edit group for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_ONLY_GROUP_MEMBER = 'Edit Group only group member';
            }
        });
    }
    ngOnInit() {}
    // 동작 안함.
    // private setTranslateForNaver(): void {
    //     this.translateService.setTranslation('ko', {
    //         CONFIGURATION: {
    //             COMMON: {
    //                 ROLE: 'Role'
    //             }
    //         }
    //     }, true);
    //     this.translateService.setTranslation('en', {
    //         CONFIGURATION: {
    //             COMMON: {
    //                 ROLE: 'Role'
    //             }
    //         }
    //     }, true);
    // }
}
