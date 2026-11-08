package com.example.integratedmanagementsystem.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.ApplicationStatus;
import com.example.integratedmanagementsystem.model.User;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<Application, Integer> {
  // Search for applications by UserId
  public List<Application> findByUser_UserId(Integer UserId);

  // Search for applications by list of subordinates (User Objects) and application status
  @EntityGraph(attributePaths = {"user"})
  public List<Application> findByUserInAndApplicationStatusIn(List<User> User, List<ApplicationStatus> status);

  // For Personal filtering by application status
  @EntityGraph(attributePaths = {"user"})
  public List<Application> findByUser_UserIdAndApplicationStatusIn(Integer userId, List<ApplicationStatus> status);

  // For Manager filtering by application status, username 
  @EntityGraph(attributePaths = {"user"})
  public List<Application> findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(List<User> User, List<ApplicationStatus> status, String username);

  // Search whether there is an existing application by UserId and Application Id
  @EntityGraph(attributePaths = {"user"})
  Optional<Application> findByUser_UserIdAndApplicationId(Integer UserId, Integer ApplicationId);

  // Search and fetches for leave applications (excluding the current one being processed) that have application status that are either 'APPLIED' or 'UPDATED' or 'APPROVED' and are overlapping with each other
  // no overlap condition:
  // existing app start date falls after the current app end date (l.startDate > :endDate) OR
  // existing app end date falls before the current app start date (l.endDate < :startDate)
  // overlap condition would be the inverse of no overlap condition
  @EntityGraph(attributePaths = {"user", "user.manager"})
  @Query("""
    SELECT l FROM Application l WHERE 
    l.applicationId != :currentApplicationId AND
    l.applicationStatus IN ('APPLIED', 'UPDATED', 'APPROVED') AND l.startDate <= :endDate AND l.endDate >= :startDate""")
  List<Application> findOverlaps(@Param("currentApplicationId") Integer currentApplicationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  // Search for leave applications based on user id, with application status either 'APPLIED' or 'UPDATED' or 'APPROVED' and overlaps with the selected year (Filtering applications that are within the selected year as well as those that crossover)
  // no overlap condition:
  // existing app start date falls after the last day of selected year (l.startDate > :endDateOfSelectedYear) OR
  // existing app end date falls before the first day of selected year (l.endDate < :startDateOfSelectedYear)
  // overlap condition would be the inverse of no overlap condition
  @Query("""
    Select l from Application l WHERE
    l.user.userId = :userId AND
    l.applicationStatus NOT IN ('DELETED', 'REJECTED', 'CANCELLED') AND
    l.startDate <= :endDateOfSelectedYear AND
    l.endDate >= :startDateOfSelectedYear""")
  List<Application> findAnyDeductingCrossYearOrWithinYearApplications(Integer userId, @Param("startDateOfSelectedYear") LocalDate startDateOfSelectedYear, @Param("endDateOfSelectedYear") LocalDate endDateOfSelectedYear);
}
