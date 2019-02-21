import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { retry } from 'rxjs/operators';

@Injectable()
export class RoleListDataService {
    private url = 'roles.pinpoint';
    constructor(private http: HttpClient) {}
    getRoleList(): Observable<string[]> {
        return of(['admin', 'user']);
        // return this.http.get<string[]>(this.url).pipe(
        //     retry(3)
        // );
    }
}
