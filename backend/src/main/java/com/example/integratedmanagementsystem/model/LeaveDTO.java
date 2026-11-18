package com.example.integratedmanagementsystem.model;

public class LeaveDTO {
  private Integer selectedYear;

  private Double entitledAnnualLeave;
  private Double budgetedAnnualLeave;

  private Double entitledHospitalisationLeave;
  private Double budgetedHospitalisationLeave;

  private Double entitledChildcareLeave;
  private Double budgetedChildcareLeave;

  public LeaveDTO() {
  }

  public LeaveDTO(Integer selectedYear, Double entitledAnnualLeave, Double budgetedAnnualLeave, Double entitledHospitalisationLeave, Double budgetedHospitalisationLeave, Double entitledChildcareLeave, Double budgetedChildcareLeave) {
    this.selectedYear = selectedYear;
    this.entitledAnnualLeave = entitledAnnualLeave;
    this.budgetedAnnualLeave = budgetedAnnualLeave;
    this.entitledHospitalisationLeave = entitledHospitalisationLeave;
    this.budgetedHospitalisationLeave = budgetedHospitalisationLeave;
    this.entitledChildcareLeave = entitledChildcareLeave;
    this.budgetedChildcareLeave = budgetedChildcareLeave;
  }

  public Integer getSelectedYear() {
    return selectedYear;
  }

  public void setSelectedYear(Integer selectedYear) {
    this.selectedYear = selectedYear;
  }

  public Double getEntitledAnnualLeave() {
    return entitledAnnualLeave;
  }

  public void setEntitledAnnualLeave(Double entitledAnnualLeave) {
    this.entitledAnnualLeave = entitledAnnualLeave;
  }

  public Double getBudgetedAnnualLeave() {
    return budgetedAnnualLeave;
  }

  public void setBudgetedAnnualLeave(Double budgetedAnnualLeave) {
    this.budgetedAnnualLeave = budgetedAnnualLeave;
  }

  public Double getEntitledHospitalisationLeave() {
    return entitledHospitalisationLeave;
  }

  public void setEntitledHospitalisationLeave(Double entitledHospitalisationLeave) {
    this.entitledHospitalisationLeave = entitledHospitalisationLeave;
  }

  public Double getBudgetedHospitalisationLeave() {
    return budgetedHospitalisationLeave;
  }

  public void setBudgetedHospitalisationLeave(Double budgetedHospitalisationLeave) {
    this.budgetedHospitalisationLeave = budgetedHospitalisationLeave;
  }

  public Double getEntitledChildcareLeave() {
    return entitledChildcareLeave;
  }

  public void setEntitledChildcareLeave(Double entitledChildcareLeave) {
    this.entitledChildcareLeave = entitledChildcareLeave;
  }

  public Double getBudgetedChildcareLeave() {
    return budgetedChildcareLeave;
  }

  public void setBudgetedChildcareLeave(Double budgetedChildcareLeave) {
    this.budgetedChildcareLeave = budgetedChildcareLeave;
  }

}
