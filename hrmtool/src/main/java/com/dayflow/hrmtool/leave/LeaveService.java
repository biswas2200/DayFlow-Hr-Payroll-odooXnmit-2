package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.leave.dto.*;
import com.dayflow.hrmtool.leave.event.LeaveAppliedEvent;
import com.dayflow.hrmtool.leave.event.LeaveDecidedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveAllocationRepository leaveAllocationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<LeaveTypeDto> listTypes(Long companyId) {
        return leaveTypeRepository.findByCompanyId(companyId).stream().map(type -> {
            LeaveTypeDto dto = new LeaveTypeDto();
            dto.setId(type.getId());
            dto.setCompanyId(type.getCompanyId());
            dto.setName(type.getName());
            dto.setRequiresAttachment(type.isRequiresAttachment());
            dto.setPaid(type.isPaid());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<LeaveBalanceDto> getMyBalances(Long employeeId, Long companyId, int year) {
        List<LeaveAllocation> allocations = leaveAllocationRepository.findByEmployeeIdAndYear(employeeId, year);
        List<LeaveType> types = leaveTypeRepository.findByCompanyId(companyId);

        return allocations.stream().map(allocation -> {
            LeaveBalanceDto dto = new LeaveBalanceDto();
            dto.setLeaveTypeId(allocation.getLeaveTypeId());
            types.stream().filter(t -> t.getId().equals(allocation.getLeaveTypeId())).findFirst().ifPresent(t -> dto.setLeaveTypeName(t.getName()));
            dto.setAllocatedDays(allocation.getAllocatedDays());
            dto.setUsedDays(allocation.getUsedDays());
            dto.setRemainingDays(allocation.getAllocatedDays().subtract(allocation.getUsedDays()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDto apply(Long employeeId, Long companyId, ApplyLeaveRequest req) {
        LeaveType type = leaveTypeRepository.findById(req.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave Type not found"));

        if (type.isPaid()) {
            LeaveAllocation allocation = leaveAllocationRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    employeeId, req.getLeaveTypeId(), req.getStartDate().getYear()
            ).orElseThrow(() -> new IllegalStateException("Leave Allocation not found for this paid type."));

            BigDecimal remaining = allocation.getAllocatedDays().subtract(allocation.getUsedDays());
            if (remaining.compareTo(req.getNumDays()) < 0) {
                throw new IllegalStateException("Insufficient leave balance."); // Equivalent to BusinessException
            }
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveTypeId(req.getLeaveTypeId());
        request.setStartDate(req.getStartDate());
        request.setEndDate(req.getEndDate());
        request.setNumDays(req.getNumDays());
        request.setAttachmentUrl(req.getAttachmentUrl());
        request.setStatus(LeaveStatus.PENDING);
        request = leaveRequestRepository.save(request);

        eventPublisher.publishEvent(new LeaveAppliedEvent(
                request.getId(), request.getEmployeeId(), request.getLeaveTypeId(),
                request.getStartDate(), request.getEndDate(), request.getNumDays()
        ));

        return toDto(request);
    }

    public LeaveCalendarDto getMyCalendar(Long employeeId, Long companyId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        List<LeaveRequestDto> requests = leaveRequestRepository
                .findByEmployeeIdAndStartDateBetween(employeeId, startOfYear, endOfYear)
                .stream().map(this::toDto).collect(Collectors.toList());

        List<PublicHolidayDto> holidays = publicHolidayRepository.findByDateBetween(startOfYear, endOfYear)
                .stream().map(h -> {
                    PublicHolidayDto dto = new PublicHolidayDto();
                    dto.setId(h.getId());
                    dto.setDate(h.getDate());
                    dto.setName(h.getName());
                    return dto;
                }).collect(Collectors.toList());

        LeaveCalendarDto dto = new LeaveCalendarDto();
        dto.setRequests(requests);
        dto.setHolidays(holidays);
        return dto;
    }

    public Page<LeaveRequestDto> listAllForApproval(LeaveStatus status, Pageable pageable) {
        return leaveRequestRepository.findByStatus(status, pageable).map(this::toDto);
    }

    @Transactional
    public void approve(Long requestId, Long approverId, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave Request not found"));

        request.setStatus(LeaveStatus.APPROVED);
        request.setApproverId(approverId);
        request.setApproverComment(comment);
        request.setDecidedAt(Instant.now());

        LeaveType type = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Leave Type not found"));

        if (type.isPaid()) {
            LeaveAllocation allocation = leaveAllocationRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    request.getEmployeeId(), request.getLeaveTypeId(), request.getStartDate().getYear()
            ).orElseThrow(() -> new IllegalStateException("Leave Allocation not found"));

            allocation.setUsedDays(allocation.getUsedDays().add(request.getNumDays()));
            leaveAllocationRepository.save(allocation);
        }

        leaveRequestRepository.save(request);

        eventPublisher.publishEvent(new LeaveDecidedEvent(
                request.getId(), request.getEmployeeId(), request.getApproverId(), request.getStatus()
        ));
    }

    @Transactional
    public void reject(Long requestId, Long approverId, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave Request not found"));

        request.setStatus(LeaveStatus.REJECTED);
        request.setApproverId(approverId);
        request.setApproverComment(comment);
        request.setDecidedAt(Instant.now());
        leaveRequestRepository.save(request);

        eventPublisher.publishEvent(new LeaveDecidedEvent(
                request.getId(), request.getEmployeeId(), request.getApproverId(), request.getStatus()
        ));
    }

    public List<LeaveAllocationDto> getAllocation(Long employeeId, Long companyId, int year) {
        return leaveAllocationRepository.findByEmployeeIdAndYear(employeeId, year).stream().map(a -> {
            LeaveAllocationDto dto = new LeaveAllocationDto();
            dto.setId(a.getId());
            dto.setEmployeeId(a.getEmployeeId());
            dto.setLeaveTypeId(a.getLeaveTypeId());
            dto.setYear(a.getYear());
            dto.setAllocatedDays(a.getAllocatedDays());
            dto.setUsedDays(a.getUsedDays());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<LeaveAllocationDto> setAllocation(Long employeeId, List<LeaveAllocationDto> dtos) {
        List<LeaveAllocation> saved = dtos.stream().map(dto -> {
            LeaveAllocation a = leaveAllocationRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    employeeId, dto.getLeaveTypeId(), dto.getYear()
            ).orElseGet(LeaveAllocation::new);
            a.setEmployeeId(employeeId);
            a.setLeaveTypeId(dto.getLeaveTypeId());
            a.setYear(dto.getYear());
            a.setAllocatedDays(dto.getAllocatedDays());
            if (a.getUsedDays() == null) a.setUsedDays(BigDecimal.ZERO);
            return leaveAllocationRepository.save(a);
        }).collect(Collectors.toList());

        return saved.stream().map(a -> {
            LeaveAllocationDto d = new LeaveAllocationDto();
            d.setId(a.getId());
            d.setEmployeeId(a.getEmployeeId());
            d.setLeaveTypeId(a.getLeaveTypeId());
            d.setYear(a.getYear());
            d.setAllocatedDays(a.getAllocatedDays());
            d.setUsedDays(a.getUsedDays());
            return d;
        }).collect(Collectors.toList());
    }

    public List<PublicHolidayDto> getPublicHolidays(Long companyId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        return publicHolidayRepository.findByDateBetween(startOfYear, endOfYear).stream().map(h -> {
            PublicHolidayDto dto = new PublicHolidayDto();
            dto.setId(h.getId());
            dto.setDate(h.getDate());
            dto.setName(h.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    private LeaveRequestDto toDto(LeaveRequest request) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(request.getId());
        dto.setEmployeeId(request.getEmployeeId());
        dto.setLeaveTypeId(request.getLeaveTypeId());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setNumDays(request.getNumDays());
        dto.setAttachmentUrl(request.getAttachmentUrl());
        dto.setStatus(request.getStatus());
        dto.setApproverId(request.getApproverId());
        dto.setApproverComment(request.getApproverComment());
        dto.setDecidedAt(request.getDecidedAt());
        return dto;
    }
}
