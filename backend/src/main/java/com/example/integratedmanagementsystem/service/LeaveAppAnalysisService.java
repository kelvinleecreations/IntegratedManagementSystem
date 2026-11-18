package com.example.integratedmanagementsystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.LeaveAppAnalysisDTO;
import com.example.integratedmanagementsystem.model.LeaveDuration;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;

@Service
public class LeaveAppAnalysisService {

  private final LeaveApplicationRepository leaveApplicationRepository;

  public LeaveAppAnalysisService (LeaveApplicationRepository leaveApplicationRepository) {
    this.leaveApplicationRepository = leaveApplicationRepository;
  }

  public List<LeaveAppAnalysisDTO> filterClashingSubordinatesApp (Application current, Integer managerId) {
    // Get all applications that overlaps with the current one
    List<Application> overlaps = leaveApplicationRepository.findOverlaps(current.getApplicationId(), current.getStartDate(), current.getEndDate());

    List<LeaveAppAnalysisDTO> subordinateClashes = new ArrayList<>();

    // Keep applications that belong to his/her subordinates
    for (Application o : overlaps) {
      // if the application has a manager and the manager id equals to the argument manager id
      if (o.getUser().getManager() != null & o.getUser().getManager().getUserId().equals(managerId)) {
        // check for clashes by leave duration
        if (isClashing(current.getLeaveDuration(), o.getLeaveDuration())) {

          // convert to DTO format
          LeaveAppAnalysisDTO analysisDTO = new LeaveAppAnalysisDTO(o.getUser().getName(), o.getStartDate().toString(), o.getEndDate().toString(), o.getLeaveDuration().name(), o.getApplicationStatus().name());
          // add the DTO into the empty list, to be returned to JS
          subordinateClashes.add(analysisDTO);
        }
      }
    }
    return subordinateClashes;
  }

  // check for clashes between two leave durations
  private boolean isClashing(LeaveDuration current, LeaveDuration existing) {
    // it clashes when either application has full day or both share the same leave slot for half days
    return current == LeaveDuration.FULLDAY || existing == LeaveDuration.FULLDAY || current == existing;
  }
}
