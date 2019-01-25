import { Action } from '@ngrx/store';

const CHANGE_ROLE_SELECTION = 'CHANGE_ROLE_SELECTION';

export class ChangeRoleSelection implements Action {
    readonly type = CHANGE_ROLE_SELECTION;
    constructor(public payload: string) {}
}

export function Reducer(state: string, action: ChangeRoleSelection): string {
    switch (action.type) {
        case CHANGE_ROLE_SELECTION:
            return (state === action.payload) ? state : action.payload;
        default:
            return state;
    }
}
