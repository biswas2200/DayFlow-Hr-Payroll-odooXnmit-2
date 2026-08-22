package com.dayflow.hrmtool.payroll.dto;

import java.util.List;
import com.dayflow.hrmtool.payroll.SalaryStructure;
import com.dayflow.hrmtool.payroll.SalaryComponent;

public class SalaryStructureRequest {
    private SalaryStructure structure;
    private List<SalaryComponent> components;

    public SalaryStructure getStructure() { return structure; }
    public void setStructure(SalaryStructure structure) { this.structure = structure; }
    public List<SalaryComponent> getComponents() { return components; }
    public void setComponents(List<SalaryComponent> components) { this.components = components; }
}
