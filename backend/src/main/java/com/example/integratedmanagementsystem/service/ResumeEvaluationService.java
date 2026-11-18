package com.example.integratedmanagementsystem.service;

import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.integratedmanagementsystem.model.CandidateReviewAnalysisDTO;

@Service
public class ResumeEvaluationService {

  private final ChatClient chatClient;

  public ResumeEvaluationService (ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  // intercepts evaluateResume method call from ResumeReviewService
  // check if the jobDescription and pdfBytes pair have been evaluated previously
  // creates "resumeEvaluation", a name for the storage where successful dtos are stored for easier and faster retrieval, reducing processing times
  // #result looks at the returned dto object from evaluateResume method
  // if evaluation is unsuccessful especially due to unexpected error, Spring will not store it in the cache and it will be re-evaluated completely the next time
  @Cacheable(value="resumeEvaluation", unless="#result.summary.startsWith('From Resume Review Service: Resume Review Failed')")
  public CandidateReviewAnalysisDTO evaluateResume(String jobDescription, byte[] pdfBytes) {
    System.out.println("[CACHE MISS] PDF Parsing and AI Reviewing in progress...");

    try {

      PDDocument doc = Loader.loadPDF(pdfBytes);
      String resumeText = new PDFTextStripper().getText(doc);

      if (resumeText == null || resumeText.trim().isEmpty()) {
        System.out.println("[SUCCESS] Blank PDF detected, assigning score of 0.0");

        return new CandidateReviewAnalysisDTO ("", "", 0.0, 0.0, 0.0, "From Resume Review Service: Parsing Failed - No text found.", List.of(), List.of(), List.of(), List.of());
      }

      CandidateReviewAnalysisDTO dto = chatClient.prompt()
        .system("""
          You are an AI Model that assists HR Recruiters in reviewing resume and a batch of resumes, matching them against the job description and provide a list with rankings, together with a summarised analysis for each candidate.
          
          Under jobDescriptionMatchingPercentage, provide an integer from 0 to 100, based on how closely matched the resume is with the requirements of the job description.

          To determine the matching percentage, identify all important words found within the job description. They will form the list of matching and missing words. From the resume, if the words match, list down in matchingWords and vice versa.

          In your own words, provide matching skills under strengths and missing skills under weaknesses. Keep them short and concised.

          Please provide a score for experienceScore from 0 to 100, based on your judgement on the level of related experience the candidate has.

          Under summary, please provide your evaluation based on the information from the resume. Do not hallucinate and provide your summary that are completely out of context (not from the resume PDFs or the job description). Keep your evaluation summarised, within 3 sentences per resume. 
          
          If you feel that there are additional bonus skills found in the candidate's resume that should be noticed or worth mentioning or can bring value to the role based on the job description, but not found in the job description, do highlight it out as well in the summary.""")
        .user(u -> u.text("""
          This is the job description: {jd} and resume: {resumePDF}
          """)
          .param("jd", jobDescription)
          .param("resumePDF", resumeText))
        .call()
        .entity(CandidateReviewAnalysisDTO.class);
    
      // calculate compatibility percentage
      Double matching = dto.getMatchingWords() != null ? dto.getMatchingWords().size() : 0.0;
      Double missing = dto.getMissingWords() != null ? dto.getMissingWords().size() : 0.0;
      Double total = matching + missing;
      Double matchPercentage = (total > 0.0) ? (matching / total) * 100 : 0.0;

      dto.setJobDescriptionMatchingPercentage(matchPercentage);

      // calculate score
      Double experienceScore = dto.getExperienceScore() != null ? dto.getExperienceScore() : 0.0;
      // equal weights
      Double finalScore = (0.5 * matchPercentage) + (0.5 * experienceScore);

      dto.setScore(finalScore);
      
      System.out.println("[SUCCESS] Completed Review for " + dto.getCandidateName() + " assigned score " + dto.getScore());
      return dto;

    } catch (Exception e) {
        System.out.println("[ERROR] Failure to Evaluate Resume: " + e.getMessage());

        // ensure that even if 1 resume fails evaluation, the remaining can still be reviewed and ranked
        return new CandidateReviewAnalysisDTO ("", "", 0.0, 0.0, 0.0, "From Resume Review Service: Resume Review Failed - Please try again later.", List.of(), List.of(), List.of(), List.of());
    }
  }

}
