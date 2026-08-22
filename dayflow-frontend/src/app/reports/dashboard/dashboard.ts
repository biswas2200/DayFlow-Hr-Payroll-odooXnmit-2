import { Component, OnInit, inject, signal } from '@angular/core';
import { ReportService } from '../../core/services/report.service';
import { DashboardSummary } from '../../core/models/report.model';

/** Admin analytics dashboard — SRS §3.8: attendance %, leave trends, headcount by department. */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly reportService = inject(ReportService);

  readonly loading = signal(true);
  readonly summary = signal<DashboardSummary | null>(null);

  ngOnInit(): void {
    this.reportService.getDashboard().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  maxTrend(trends: { month: string; count: number }[]): number {
    return Math.max(1, ...trends.map((t) => t.count));
  }

  maxHeadcount(rows: { department: string; count: number }[]): number {
    return Math.max(1, ...rows.map((r) => r.count));
  }
}
