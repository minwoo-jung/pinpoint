import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry, tap } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { AppState, Actions } from 'app/shared/store';
import { Application } from 'app/core/models';
import { MessageQueueService, MESSAGE_TO } from 'app/shared/services/message-queue.service';

@Injectable()
export class UserConfigurationDataService {
    private url = 'users/user/permissionAndConfiguration.pinpoint';
    constructor(
        private http: HttpClient,
        private store: Store<AppState>,
        private messageQueueService: MessageQueueService,
    ) {}
    getUserConfiguration(): Observable<IUserConfiguration> {
        return this.http.get<IUserConfiguration>(this.url).pipe(
            retry(3),
            tap((res: IUserConfiguration) => {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.FAVORITE_APP_LIST_FROM_SERVER,
                    param: [res.configuration.favoriteApplications.map(({applicationName, serviceType, code}) => {
                        return new Application(applicationName, serviceType, code);
                    })]
                });
                this.store.dispatch(new Actions.UpdatePermissions(res.permission));
            })
        );
    }
}
