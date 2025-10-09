import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, finalize, Observable, take, tap, throwError } from 'rxjs';
import { AuthResponseData, AuthService } from './auth.service';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent {
  isLoginMode = true;
  isLoading = false;
  error: string | null = null;
  token?: string;

  form: FormGroup = new FormGroup({
    username: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) { }

  onSubmit(): void {
    if (this.form.invalid) return;

    const { username, password } = this.form.value;

    this.login(username, password).subscribe();
  }

  private login(name: string, password: string): Observable<AuthResponseData> {
    this.isLoading = true;

    return this.authService.login(name, password).pipe(
      take(1),
      tap((resData: AuthResponseData) => {
        this.token = resData.token;
        this.router.navigate(['/products']);
      }),
      catchError((error) => {
        this.error = error;
        return throwError(() => error);
      }),
      finalize(() => {
        this.isLoading = false;
        this.form.reset(); // reset del form alla fine dello stream
      })
    );
  }
}
