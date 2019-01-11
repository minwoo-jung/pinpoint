import { Action } from '@ngrx/store';

const UPDATE_USER_PERMISSIONS = 'UPDATE_USER_PERMISSIONS';

export class UpdateUserPermissions implements Action {
    readonly type = UPDATE_USER_PERMISSIONS;
    constructor(public payload: IPermissions) {}
}

export function Reducer(state: IPermissions, action: UpdateUserPermissions): IPermissions {
    switch (action.type) {
        case UPDATE_USER_PERMISSIONS:
            return (state === action.payload) ? state : action.payload;
        default:
            return state;
    }
}
