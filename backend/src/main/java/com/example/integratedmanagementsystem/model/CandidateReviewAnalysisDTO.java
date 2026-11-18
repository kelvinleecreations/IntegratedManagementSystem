package com.example.integratedmanagementsystem.model;

import java.util.List;

public class CandidateReviewAnalysisDTO {
  private String candidateName;
  private String pdfFileName;

  private Double score;
  private Double jobDescriptionMatchingPercentage;
  private Double experienceScore;

  private String summary;
  private List<String> strengths;
  private List<String> weaknesses;
  private List<String> matchingWords;
  private List<String> missingWords;

  // no args constructor for Spring AI
  public CandidateReviewAnalysisDTO() {
  }

  public CandidateReviewAnalysisDTO(String candidateName, String pdfFileName, Double score,
      Double jobDescriptionMatchingPercentage, Double experienceScore, String summary, List<String> strengths,
      List<String> weaknesses, List<String> matchingWords, List<String> missingWords) {
    this.candidateName = candidateName;
    this.pdfFileName = pdfFileName;
    this.score = score;
    this.jobDescriptionMatchingPercentage = jobDescriptionMatchingPercentage;
    this.experienceScore = experienceScore;
    this.summary = summary;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.matchingWords = matchingWords;
    this.missingWords = missingWords;
  }

  public String getCandidateName() {
    return candidateName;
  }

  public void setCandidateName(String candidateName) {
    this.candidateName = candidateName;
  }

  public String getPdfFileName() {
    return pdfFileName;
  }

  public void setPdfFileName(String pdfFileName) {
    this.pdfFileName = pdfFileName;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public Double getJobDescriptionMatchingPercentage() {
    return jobDescriptionMatchingPercentage;
  }

  public void setJobDescriptionMatchingPercentage(Double jobDescriptionMatchingPercentage) {
    this.jobDescriptionMatchingPercentage = jobDescriptionMatchingPercentage;
  }

  public Double getExperienceScore() {
    return experienceScore;
  }

  public void setExperienceScore(Double experienceScore) {
    this.experienceScore = experienceScore;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public List<String> getStrengths() {
    return strengths;
  }

  public void setStrengths(List<String> strengths) {
    this.strengths = strengths;
  }

  public List<String> getWeaknesses() {
    return weaknesses;
  }

  public void setWeaknesses(List<String> weaknesses) {
    this.weaknesses = weaknesses;
  }

  public List<String> getMatchingWords() {
    return matchingWords;
  }

  public void setMatchingWords(List<String> matchingWords) {
    this.matchingWords = matchingWords;
  }

  public List<String> getMissingWords() {
    return missingWords;
  }

  public void setMissingWords(List<String> missingWords) {
    this.missingWords = missingWords;
  }

  

}
