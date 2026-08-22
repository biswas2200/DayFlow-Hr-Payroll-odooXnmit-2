package com.dayflow.hrmtool.employee;

/**
 * Status dot for employee card display in the directory grid.
 * GREEN = present in office today
 * AIRPLANE = on approved leave today
 * YELLOW = absent (no attendance, no leave applied)
 */
public enum StatusDot {
    GREEN,
    AIRPLANE,
    YELLOW
}
