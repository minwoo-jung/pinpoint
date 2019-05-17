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
    ) {
        this.setLang();
    }
    ngOnInit() {
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

        // @TODO Naver쪽 assets/ko.json, assets/en.json 에 셋팅하기
        // https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/2605
        // this.translateService.getTranslation(currentLang).subscribe((translate) => {
        //     if (currentLang === 'ko') {
        //         translate.CONFIGURATION.COMMON.ROLE = '권한';
        //         translate.CONFIGURATION.COMMON.POSITION = '포지션';
        //         translate.CONFIGURATION.COMMON.CURRENT_PASSWORD = '기존 비밀번호';
        //         translate.CONFIGURATION.COMMON.NEW_PASSWORD = '신규 비밀번호';
        //         translate.CONFIGURATION.COMMON.CONFIRM_NEW_PASSWORD = '신규 비밀번호 재확인';
        //         translate.CONFIGURATION.COMMON.PASSWORD = '비밀번호';
        //         translate.CONFIGURATION.COMMON.PASSWORD_MISMATCH = '비밀번호가 일치하지 않습니다.';
        //         translate.CONFIGURATION.COMMON.PASSWORD_VALIDATION = '영문, 숫자, 특수문자(!@#$%^&*())가 각각 1개이상 포함되어야 합니다.(8~24)';

        //         translate.CONFIGURATION.AUTH = {
        //             SERVER_MAP: 'ServerMap 데이터 비노출',
        //             API_META: 'API 데이터 비노출',
        //             PARAM_META: 'Parameter 데이터 비노출',
        //             SQL_META: 'SQL 데이터 비노출'
        //         };
        //         translate.CONFIGURATION.USERS = {
        //             GUIDE: '사용자를 추가, 조회 또는 삭제할 수 있습니다.'
        //         };
        //         translate.CONFIGURATION.ROLE = {
        //             SELECT: 'Role 을 선택하여 정보를 조회하거나 추가, 삭제할 수 있습니다.',
        //             WILL_REMOVE: '선택한 Role을 삭제합니다.',
        //             INPUT_NAME: 'Role의 이름을 입력하세요.',
        //             VALIDATION_GUIDE: '영문자와 숫자 "-_" 특수 문자를 입력 할 수 있습니다.(3 ~ 24)'
        //         };
        //         translate.CONFIGURATION.PERMISSION = {
        //             ADMIN_MENU_TITLE: 'Admin 메뉴',
        //             VIEW_ADMIN_MENU: 'Admin menu 보기',
        //             EDIT_USER_TITLE: 'User 수정',
        //             EDIT_USER: 'User 추가 삭제, Role 할당',
        //             EDIT_ROLE_TITLE: 'Role 수정',
        //             EDIT_ROLE: 'Role 추가 삭제, Permission 수정',
        //             CALL_ADMIN_API_TITLE: 'Application, Agent 정보 관리 API',
        //             CALL_ADMIN_API: 'API 호출 가능',
        //             PREOCCUPANCY_TITLE: '최초 할당',
        //             PREOCCUPANCY: 'User Group 최초 할당 가능',
        //             EDIT_AUTHOR_TITLE: 'Authentication 수정',
        //             EDIT_AUTHOR_FOR_EVERYTHING: '모든 Authentication 수정 가능',
        //             EDIT_AUTHOR_ONLY_MANAGER: 'Position이 Manager인 User Group에 소속된 경우에만 Authentication 수정 가능',
        //             OBTAIN_ALL_TITLE: 'Authentication 획득',
        //             OBTAIN_ALL_AUTHORIZATION: 'Application의 Authentication 설정 상관없이 모든 application의 권한 획득',
        //             EDIT_ALARM_TITLE: 'Alarm 수정',
        //             EDIT_ALARM_FOR_EVERYTHING: '모든 Alarm 수정 가능',
        //             EDIT_ALARM_ONLY_MANAGER: 'Position이 Manager인 User Group에 소속된 Member만 Alarm 수정 가능',
        //             EDIT_GROUP_TITLE: 'User Group 수정',
        //             EDIT_GROUP_FOR_EVERYTHING: 'User Group 생성 및 수정 가능',
        //             EDIT_GROUP_ONLY_GROUP_MEMBER: 'User Group의 Member 만 생성 및 수정 가능'
        //         };
        //         translate.CONFIGURATION.AGENT_MANAGEMENT = {
        //             REMOVE_APPLICATION: '선택한 Application을 삭제 합니다.',
        //             REMOVE_AGENT: '선택한 Agent를 삭제합니다.'
        //         };
        //         translate.CONFIGURATION.AGENT_STATISTIC = {
        //             LOAD_GUIDE: 'Agent의 통계 정보를 가져오는 작업은 많은 시간을 소요합니다.',
        //             LOADING: '데이터 가져오기',
        //             RELOAD: '데이터 다시 가져오기'
        //         };
        //         translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['문제 발생시 핀포인트 개발팀에 문의 해 주세요. 해결 해 드립니다.'];
        //     } else {
        //         translate.CONFIGURATION.COMMON.ROLE = 'Role';
        //         translate.CONFIGURATION.COMMON.POSITION = 'Poistion';
        //         translate.CONFIGURATION.COMMON.CURRENT_PASSWORD = 'Current Password';
        //         translate.CONFIGURATION.COMMON.NEW_PASSWORD = 'New Password';
        //         translate.CONFIGURATION.COMMON.CONFIRM_NEW_PASSWORD = 'Confirm New Password';
        //         translate.CONFIGURATION.COMMON.PASSWORD = 'Password';
        //         translate.CONFIGURATION.COMMON.PASSWORD_MISMATCH = 'Password does not match.';
        //         translate.CONFIGURATION.COMMON.PASSWORD_VALIDATION = 'Password must contain a mix of letters, numbers, and special characters(!@#$%^&*()).(8~24)';

        //         translate.CONFIGURATION.AUTH = {
        //             SERVER_MAP: 'Hide ServerMap data',
        //             API_META: 'Hide API data',
        //             PARAM_META: 'Hide Parameter data',
        //             SQL_META: 'Hide SQL data'
        //         };
        //         translate.CONFIGURATION.USERS = {
        //             GUIDE: 'Add, query or delete users.'
        //         };
        //         translate.CONFIGURATION.ROLE = {
        //             SELECT: 'Select role or add, delete role',
        //             WILL_REMOVE: 'Remove selected role.',
        //             INPUT_NAME: 'Input role name',
        //             VALIDATION_GUIDE: 'You can enter only numbers, alphabets, and "-_" characters.(3 ~ 24)'
        //         };
        //         translate.CONFIGURATION.PERMISSION = {
        //             ADMIN_MENU_TITLE: 'Admin Menu',
        //             VIEW_ADMIN_MENU: 'View admin menu',
        //             EDIT_USER_TITLE: 'User Modification',
        //             EDIT_USER: 'Edit User Info',
        //             EDIT_ROLE_TITLE: 'Role Modification',
        //             EDIT_ROLE: 'Edit Role Info',
        //             CALL_ADMIN_API_TITLE: 'Application, Agent Management API',
        //             CALL_ADMIN_API: 'API access available',
        //             PREOCCUPANCY_TITLE: 'Preoccupancy',
        //             PREOCCUPANCY: 'Can have the authority first',
        //             EDIT_AUTHOR_TITLE: 'Authentication Modification',
        //             EDIT_AUTHOR_FOR_EVERYTHING: 'Edit author for everything',
        //             EDIT_AUTHOR_ONLY_MANAGER: 'Edit Author only manager',
        //             OBTAIN_ALL_TITLE: 'Authentication Obtain',
        //             OBTAIN_ALL_AUTHORIZATION: 'Obtain authorization for all applications regardless of the authentication setting',
        //             EDIT_ALARM_TITLE: 'Alarm Modification',
        //             EDIT_ALARM_FOR_EVERYTHING: 'Edit alarm for everything',
        //             EDIT_ALARM_ONLY_MANAGER: 'Edit alarm only group member',
        //             EDIT_GROUP_TITLE: 'User Group Modification',
        //             EDIT_GROUP_FOR_EVERYTHING: 'Edit group for everything',
        //             EDIT_GROUP_ONLY_GROUP_MEMBER: 'Edit Group only group member'
        //         };
        //         translate.CONFIGURATION.AGENT_MANAGEMENT = {
        //             REMOVE_APPLICATION: 'Remove selected application.',
        //             REMOVE_AGENT: 'Remove selected agent.'
        //         };
        //         translate.CONFIGURATION.AGENT_STATISTIC = {
        //             LOAD_GUIDE: 'It takes a long time to load the agent\'s statistical data.',
        //             LOADING: 'Load data',
        //             RELOAD: 'Reload'
        //         };
        //         translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['Please contact the pinpoint development team in case of problems. We will solve it.'];
        //     }
        // });
    }

    private listenToRouter(): void {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            this.routeInfoCollectorService.collectUrlInfo(this.activatedRoute);
        });
    }
}
