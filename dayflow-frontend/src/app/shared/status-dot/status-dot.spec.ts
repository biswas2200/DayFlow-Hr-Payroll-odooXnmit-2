import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusDot as StatusDotValue } from '../../core/models/employee.model';
import { StatusDot } from './status-dot';

@Component({
  selector: 'app-host',
  imports: [StatusDot],
  template: `<app-status-dot [status]="status" />`,
})
class HostComponent {
  status: StatusDotValue = 'GREEN';
}

describe('StatusDot', () => {
  let fixture: ComponentFixture<HostComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HostComponent] });
    fixture = TestBed.createComponent(HostComponent);
  });

  function dotClass(): string | null {
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).querySelector('.dot')?.className ?? null;
  }

  it('renders a green dot for GREEN (present in office)', () => {
    fixture.componentInstance.status = 'GREEN';
    expect(dotClass()).toContain('dot-green');
  });

  it('renders the airplane dot for AIRPLANE (on leave)', () => {
    fixture.componentInstance.status = 'AIRPLANE';
    expect(dotClass()).toContain('dot-airplane');
  });

  it('renders a yellow dot for YELLOW (unaccounted absence)', () => {
    fixture.componentInstance.status = 'YELLOW';
    expect(dotClass()).toContain('dot-yellow');
  });
});
