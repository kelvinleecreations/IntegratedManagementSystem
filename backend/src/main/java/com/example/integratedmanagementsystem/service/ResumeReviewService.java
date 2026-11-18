package com.example.integratedmanagementsystem.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.integratedmanagementsystem.model.CandidateReviewAnalysisDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeReviewService {

  private final ResumeEvaluationService resumeEvaluationService;
  private final RateLimitingService rateLimitingService;

  public ResumeReviewService (ResumeEvaluationService resumeEvaluationService, RateLimitingService rateLimitingService) {
    this.resumeEvaluationService = resumeEvaluationService;
    this.rateLimitingService = rateLimitingService;
  }

  public List<CandidateReviewAnalysisDTO> rankResume(String jobDescription, List<MultipartFile> files, String username) throws IOException {
 
    // check if job description field is empty
    if (jobDescription == null || jobDescription.trim().isEmpty()) {
      throw new IllegalArgumentException("From Resume Review Service: Job Description is required for analysis.");
    }

    // check if there is any file uploaded
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("From Resume Review Service: Please upload at least one PDF resume.");
    }

    // Limit AI Resume Reviewer attempts
    Integer maxRequests = 3;
    Long cdDuration = (Long) (long) 86400;

    if (!rateLimitingService.hasNotExceeded("[AI Usage] " + username, maxRequests, cdDuration)) {
      throw new IllegalStateException("AI Usage Limit Reached: Please retry tomorrow.");
    }  

    System.out.println("---Received " + files.size() + " resume(s)---");

    List<CandidateReviewAnalysisDTO> reviewResult = new ArrayList<>();
    
    for (MultipartFile f : files) {
      // check if it is a PDF
      validateFileVersion(f);

      CandidateReviewAnalysisDTO eval = resumeEvaluationService.evaluateResume(jobDescription, f.getBytes());

      String fileName = f.getOriginalFilename();
      eval.setPdfFileName(fileName);

      // if no candidate name
      if (eval.getCandidateName() == null || eval.getCandidateName().isEmpty()) {
        eval.setCandidateName(fileName);
      }

      reviewResult.add(eval);
    }

    for (int i = 0; i < reviewResult.size(); i++) {
      for (int j = i + 1; j < reviewResult.size(); j++) {
        if (reviewResult.get(i).getScore() < reviewResult.get(j).getScore()) {
          CandidateReviewAnalysisDTO temp = reviewResult.get(i);
          reviewResult.set(i, reviewResult.get(j));
          reviewResult.set(j, temp);
        }
      }
    }

    System.out.println("[COMPLETE] All resumes have been reviewed and ranked.");
    System.out.println("Displaying the rankings and results...");
    System.out.println("---End---");

    return reviewResult;
  }

  private void validateFileVersion(MultipartFile file) {
    String fileName = file.getOriginalFilename();
    String contentType = file.getContentType();

    boolean isPdfExtension = fileName != null && fileName.toLowerCase().endsWith(".pdf");
    // additional layer of check by comparing the Content Type header attached to the file by browser 
    boolean isPdfMimeType = "application/pdf".equalsIgnoreCase(contentType);

    if (!isPdfExtension || !isPdfMimeType) {
      throw new IllegalArgumentException("From Resume Review Service: File Format Error -" + " [" + fileName + "] " + "Please upload only PDF files.");
    }
  }
}


