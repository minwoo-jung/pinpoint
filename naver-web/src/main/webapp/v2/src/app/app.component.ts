import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

import { WindowRefService, RouteInfoCollectorService } from 'app/shared/services';

@Component({
    selector: 'pp-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    constructor(
        private windowRefService: WindowRefService,
        private translateService: TranslateService,
        private routeInfoCollectorService: RouteInfoCollectorService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.setLang();
        this.listenToRouter();
    }

    private setLang(): void {
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
                translate.CONFIGURATION.COMMON.POSITION = '포지션';
                translate.CONFIGURATION.AUTH = {
                    SERVER_MAP: 'ServerMap 데이터 비노출',
                    API_META: 'API 데이터 비노출',
                    PARAM_META: 'Parameter 데이터 비노출',
                    SQL_META: 'SQL 데이터 비노출'
                };
                translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['문제 발생시 핀포인트 개발팀에 문의 해 주세요. 해결 해 드립니다.'];

                translate.CONFIGURATION.COMMON.CURRENT_PASSWORD = '기존 비밀번호';
                translate.CONFIGURATION.COMMON.NEW_PASSWORD = '신규 비밀번호';
                translate.CONFIGURATION.COMMON.CONFIRM_NEW_PASSWORD = '신규 비밀번호 재확인';
                translate.CONFIGURATION.COMMON.PASSWORD = '비밀번호';
                translate.CONFIGURATION.COMMON.PASSWORD_MISMATCH = '비밀번호가 일치하지 않습니다.';
                translate.CONFIGURATION.USERS = {
                    GUIDE: '사용자를 추가, 조회 또는 삭제할 수 있습니다.'
                };
                translate.CONFIGURATION.ROLE = {
                    SELECT: 'Role 을 선택하여 정보를 조회하거나 추가, 삭제할 수 있습니다.',
                    WILL_REMOVE: '선택한 Role을 삭제합니다.',
                    INPUT_NAME: 'Role의 이름을 입력하세요.'
                };
                translate.CONFIGURATION.PERMISSION = {};
                translate.CONFIGURATION.PERMISSION.ADMIN_MENU_TITLE = 'Admin 메뉴';
                translate.CONFIGURATION.PERMISSION.VIEW_ADMIN_MENU = 'Admin menu 보기';
                translate.CONFIGURATION.PERMISSION.EDIT_USER_TITLE = 'User 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_USER = 'User 추가 삭제, Role 할당';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE_TITLE = 'Role 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE = 'Role 추가 삭제, Permission 수정';
                translate.CONFIGURATION.PERMISSION.CALL_ADMIN_API_TITLE = 'Application, Agent 정보 관리 API';
                translate.CONFIGURATION.PERMISSION.CALL_ADMIN_API = 'API 호출 가능';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY_TITLE = '최초 할당';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY = 'User Group 최초 할당 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_TITLE = 'Authentication 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_FOR_EVERYTHING = '모든 Authentication 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_ONLY_MANAGER = 'Position이 Manager인 User Group에 소속된 경우에만 Authentication 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_TITLE = 'Alarm 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_FOR_EVERYTHING = '모든 Alarm 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_ONLY_MANAGER = 'Position이 Manager인 User Group에 소속된 Member만 Alarm 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_TITLE = 'User Group 수정';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_FOR_EVERYTHING = 'User Group 생성 및 수정 가능';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_ONLY_GROUP_MEMBER = 'User Group의 Member 만 생성 및 수정 가능';
            } else {
                translate.CONFIGURATION.COMMON.ROLE = 'Role';
                translate.CONFIGURATION.COMMON.POSITION = 'Poistion';
                translate.CONFIGURATION.AUTH = {
                    SERVER_MAP: 'Hide ServerMap data',
                    API_META: 'Hide API data',
                    PARAM_META: 'Hide Parameter data',
                    SQL_META: 'Hide SQL data'
                };
                translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['Please contact the pinpoint development team in case of problems. We will solve it.'];

                translate.CONFIGURATION.COMMON.CURRENT_PASSWORD = 'Current Password';
                translate.CONFIGURATION.COMMON.NEW_PASSWORD = 'New Password';
                translate.CONFIGURATION.COMMON.CONFIRM_NEW_PASSWORD = 'Confirm New Password';
                translate.CONFIGURATION.COMMON.PASSWORD = 'Password';
                translate.CONFIGURATION.COMMON.PASSWORD_MISMATCH = 'Password does not match.';
                translate.CONFIGURATION.USERS = {
                    GUIDE: 'Add, query or delete users.'
                };
                translate.CONFIGURATION.ROLE = {
                    SELECT: 'Select role or add, delete role',
                    WILL_REMOVE: 'Remove selected role.',
                    INPUT_NAME: 'Input role name'
                };
                translate.CONFIGURATION.PERMISSION = {};
                translate.CONFIGURATION.PERMISSION.ADMIN_MENU_TITLE = 'Admin Menu';
                translate.CONFIGURATION.PERMISSION.VIEW_ADMIN_MENU = 'View admin menu';
                translate.CONFIGURATION.PERMISSION.EDIT_USER_TITLE = 'User Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_USER = 'Edit User Info';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE_TITLE = 'Role Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_ROLE = 'Edit Role Info';
                translate.CONFIGURATION.PERMISSION.CALL_ADMIN_API_TITLE = 'Application, Agent Management API';
                translate.CONFIGURATION.PERMISSION.CALL_ADMIN_API = 'API access available';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY_TITLE = 'Preoccupancy';
                translate.CONFIGURATION.PERMISSION.PREOCCUPANCY = 'Can have the authority first';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_TITLE = 'Authentication Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_FOR_EVERYTHING = 'Edit author for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_AUTHOR_ONLY_MANAGER = 'Edit Author only manager';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_TITLE = 'Alarm Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_FOR_EVERYTHING = 'Edit alarm for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_ALARM_ONLY_MANAGER = 'Edit alarm only group member';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_TITLE = 'User Group Modification';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_FOR_EVERYTHING = 'Edit group for everything';
                translate.CONFIGURATION.PERMISSION.EDIT_GROUP_ONLY_GROUP_MEMBER = 'Edit Group only group member';
            }
        });
    }

    private listenToRouter(): void {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            this.routeInfoCollectorService.collectUrlInfo(this.activatedRoute);
        });
    }
}
