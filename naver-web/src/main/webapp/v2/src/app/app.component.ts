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
            } else {
                translate.CONFIGURATION.COMMON.ROLE = 'Role';
                translate.CONFIGURATION.AUTH = {
                    SERVER_MAP: 'Hide ServerMap data',
                    API_META: 'Hide API data',
                    PARAM_META: 'Hide Parameter data',
                    SQL_META: 'Hide SQL data'
                };
                translate.INSPECTOR.APPLICAITION_NAME_ISSUE.ISSUE_SOLUTIONS = ['Please contact the pinpoint development team in case of problems. We will solve it.'];
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
