import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../core/services/employee.service';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeCard } from '../../core/models/employee.model';
import { StatusDot } from '../../shared/status-dot/status-dot';

/** Employees landing directory — SRS §3.2.1: searchable card grid + New button. */
@Component({
  selector: 'app-employee-grid',
  imports: [RouterLink, FormsModule, StatusDot],
  templateUrl: './employee-grid.html',
  styleUrl: './employee-grid.scss',
})
export class EmployeeGrid implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly auth = inject(AuthService);

  readonly isAdmin = this.auth.isAdmin;
  readonly cards = signal<EmployeeCard[]>([]);
  readonly loading = signal(true);
  readonly search = signal('');

  ngOnInit(): void {
    this.load();
  }

  onSearchChange(value: string): void {
    this.search.set(value);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.employeeService.getDirectory(this.search()).subscribe({
      next: (page) => {
        this.cards.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
