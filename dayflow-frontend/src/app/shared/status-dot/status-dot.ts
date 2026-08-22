import { Component, input } from '@angular/core';
import { StatusDot as StatusDotValue } from '../../core/models/employee.model';

/**
 * Employee card status indicator — SRS §3.2.1:
 * green = present, airplane = on leave, yellow = absent/unaccounted.
 */
@Component({
  selector: 'app-status-dot',
  template: `
    @switch (status()) {
      @case ('GREEN') {
        <span class="dot dot-green" title="Present in office"></span>
      }
      @case ('AIRPLANE') {
        <span class="dot dot-airplane" title="On leave">✈</span>
      }
      @default {
        <span class="dot dot-yellow" title="Absent — no time-off applied"></span>
      }
    }
  `,
  styles: `
    .dot {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 2px solid var(--dt-surface);
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
    }
    .dot-green {
      background: var(--dt-status-present);
    }
    .dot-yellow {
      background: var(--dt-status-absent);
    }
    .dot-airplane {
      background: var(--dt-status-leave);
      font-size: 9px;
      color: #fff;
      border-radius: 50%;
    }
  `,
})
export class StatusDot {
  readonly status = input.required<StatusDotValue>();
}
