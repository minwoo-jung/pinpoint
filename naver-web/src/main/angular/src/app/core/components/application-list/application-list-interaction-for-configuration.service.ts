import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable()
export class ApplicationListInteractionForConfigurationService {
    private outSelectApplication = new BehaviorSubject<IApplication>(null);

    onSelectApplication$: Observable<IApplication>;

    constructor() {
        this.onSelectApplication$ = this.outSelectApplication.asObservable();
    }
    setSelectedApplication(application: IApplication): void {
        this.outSelectApplication.next(application);
    }
}

