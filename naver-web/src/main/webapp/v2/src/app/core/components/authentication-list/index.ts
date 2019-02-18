
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'app/shared';
import { AuthenticationListComponent } from './authentication-list.component';
import { AuthenticationListContainerComponent } from './authentication-list-container.component';
import { AuthenticationCreateAndUpdateComponent } from './authentication-create-and-update.component';
import { AuthenticationDataService } from './authentication-data.service';
import { AuthenticationInteractionService } from './authentication-interaction.service';

@NgModule({
    declarations: [
        AuthenticationListComponent,
        AuthenticationListContainerComponent,
        AuthenticationCreateAndUpdateComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        SharedModule
    ],
    exports: [
        AuthenticationListContainerComponent,
        AuthenticationCreateAndUpdateComponent
    ],
    providers: [
        AuthenticationDataService,
        AuthenticationInteractionService
    ]
})
export class AuthenticationListModule { }
