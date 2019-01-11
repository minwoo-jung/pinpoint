import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserConfigurationDataService } from './user-configuration-data.service';

@Injectable()
export class UserConfigurationResolverService implements Resolve<IUserConfiguration> {
    constructor(
        private router: Router,
        private userConfigurationDataService: UserConfigurationDataService
    ) {}
    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IUserConfiguration> {
        return this.userConfigurationDataService.getUserConfiguration().pipe(
            catchError((error: any) => {
                this.router.navigate(['/error']);
                return EMPTY;
            })
        );
    }
}
