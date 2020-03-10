import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

// TODO: **에 대해 PageNotFound처리.
const routes: Routes = [
    {
        path: 'main',
        loadChildren: () => import('./page/main/index').then(m => m.MainPageModule)
    },
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'main'
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
