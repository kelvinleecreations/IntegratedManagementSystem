package com.example.integratedmanagementsystem;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.integratedmanagementsystem.model.Application;
import com.example.integratedmanagementsystem.model.ApplicationStatus;
import com.example.integratedmanagementsystem.model.LeaveDuration;
import com.example.integratedmanagementsystem.model.LeaveType;
import com.example.integratedmanagementsystem.model.Role;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.LeaveApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LeaveApplicationRepositoryTests {
  @Autowired 
  private LeaveApplicationRepository leaveApplicationRepository;

  @Autowired
  private TestEntityManager entityManager;

  private User u1, u2, u3;
  private Application a1, a2, a3, a4, a5;

  @BeforeEach
  void setUpData() {
    // Create users
    u1 = new User("Yvonne", "yvonne", "yvonne@email.com", "DefaultPassword", Role.ROLE_FINANCE_MANAGER, null, null, null, null, null, null, null, null, null, null);
    u2 = new User("Brandon", "brandon", "brandon@email.com", "DefaultPassword", Role.ROLE_FINANCE_MANAGER, null, null, null, null, null, null, null, null, null, null);
    u3 = new User("user3", "user3", "user3@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, null, null, null, null, null, null, null, null, null, null);
    entityManager.persist(u1);
    entityManager.persist(u2);
    entityManager.persist(u3);
    
    // Create some applications associated with the user
    a1 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,1), LocalDate.of(2026,11,2), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);
    a2 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,3), LocalDate.of(2026,11,3), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);
    a3 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,10), LocalDate.of(2026,11,10), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);
    a4 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,20), LocalDate.of(2026,11,22), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);
    a5 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,20), LocalDate.of(2026,11,22), ApplicationStatus.REJECTED, "DefaultUserReason", null, null);

    // Assign applications to users
    a1.setUser(u1);
    a2.setUser(u1);
    a3.setUser(u2);
    a4.setUser(u2);
    a5.setUser(u2);

    entityManager.persist(a1);
    entityManager.persist(a2);
    entityManager.persist(a3);
    entityManager.persist(a4);
    entityManager.persist(a5);

    entityManager.flush();
    entityManager.clear();
  }

  @Test 
  @DisplayName("Returns a list of appplications successfully based on user id")
  void testFindByUser_UserId() {

    List<Application> applicationList1 = leaveApplicationRepository.findByUser_UserId(u1.getUserId());

    List<Application> applicationList2 = leaveApplicationRepository.findByUser_UserId(u2.getUserId());

    assertThat(applicationList1).isNotNull();
    assertThat(applicationList1).hasSize(2);
    assertThat(applicationList1).containsExactlyInAnyOrder(a1, a2);
    assertThat(applicationList1).doesNotContain(a3);

    assertThat(applicationList2).isNotNull();
    assertThat(applicationList2).hasSize(3);
    assertThat(applicationList2).containsExactlyInAnyOrder(a3, a4, a5);
    assertThat(applicationList2).doesNotContain(a2);
  }

  @Test 
  @DisplayName("Returns an empty list of applications successfully when there are no applications under the user id")
  void testFindEmptyByUser_UserId() {
    
    List<Application> applicationList3 = leaveApplicationRepository.findByUser_UserId(u3.getUserId());

    assertThat(applicationList3).isEmpty();    
  }

  @Test 
  @DisplayName("Returns list of applications successfully based on list of user and application status")
  void testFindByUserInAndApplicationStatusIn() {
    List<User> userList1 = List.of(u1, u2);
    List<ApplicationStatus> statusList1 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.APPROVED);

    List<User> userList2 = List.of(u1, u2);
    List<ApplicationStatus> statusList2 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.CANCELLED);

    List<User> userList3 = List.of(u1, u3);
    List<ApplicationStatus> statusList3 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.APPROVED);

    List<Application> applicationList1 = leaveApplicationRepository.findByUserInAndApplicationStatusIn(userList1, statusList1);
    List<Application> applicationList2 = leaveApplicationRepository.findByUserInAndApplicationStatusIn(userList2, statusList2);
    List<Application> applicationList3 = leaveApplicationRepository.findByUserInAndApplicationStatusIn(userList3, statusList3);


    assertThat(applicationList1).isNotNull();
    assertThat(applicationList1).hasSize(4);
    assertThat(applicationList1).containsExactlyInAnyOrder(a1, a2, a3, a4);

    assertThat(applicationList2).isNotNull();
    assertThat(applicationList2).hasSize(2);
    assertThat(applicationList2).containsExactlyInAnyOrder(a1, a3);

    assertThat(applicationList3).isNotNull();
    assertThat(applicationList3).hasSize(2);
    assertThat(applicationList3).containsExactlyInAnyOrder(a1, a2);
    assertThat(applicationList3).doesNotContain(a3, a4, a5);
  }

  @Test 
  @DisplayName("Returns an empty list of applications successfully when there are no applications based on list of user and application status")
  void testFindEmptyByUserInAndApplicationStatusIn() {
    // empty user list with list of application status
    List<User> userList4 = List.of();
    List<ApplicationStatus> statusList4 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.APPROVED);

    List<Application> applicationList4 = leaveApplicationRepository.findByUserInAndApplicationStatusIn(userList4, statusList4);

    assertThat(applicationList4).isEmpty();

    // user list with list of application status not found within the user list
    List<User> userList5 = List.of(u1, u2);
    List<ApplicationStatus> statusList5 = List.of(ApplicationStatus.UPDATED, ApplicationStatus.CANCELLED);

    List<Application> applicationList5 = leaveApplicationRepository.findByUserInAndApplicationStatusIn(userList5, statusList5);

    assertThat(applicationList5).isEmpty();
  }

  @Test 
  @DisplayName("Returns a list of applications successfully based on logged in user and application status")
  void testFindByUser_UserIdAndApplicationStatusIn() {
    // create more applications
    Application a6 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,1), LocalDate.of(2026,12,2), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);
    Application a7 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,3), LocalDate.of(2026,12,5), ApplicationStatus.CANCELLED, "DefaultUserReason", null, null);
    Application a8 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,6), LocalDate.of(2026,12,8), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);
    Application a9 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,10), LocalDate.of(2026,12,11), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);
    Application a10 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,12), LocalDate.of(2026,12,15), ApplicationStatus.REJECTED, "DefaultUserReason", null, null);
    Application a11 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,20), LocalDate.of(2026,12,22), ApplicationStatus.DELETED, "DefaultUserReason", null, null);

    a6.setUser(u3);
    a7.setUser(u3);
    a8.setUser(u3);
    a9.setUser(u3);
    a10.setUser(u3);
    a11.setUser(u3);

    entityManager.persist(a6);
    entityManager.persist(a7);
    entityManager.persist(a8);
    entityManager.persist(a9);
    entityManager.persist(a10);
    entityManager.persist(a11);

    entityManager.flush();
    entityManager.clear();

    // assign them under user 3
    List<ApplicationStatus> statusList6 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.UPDATED, ApplicationStatus.DELETED);
    List<ApplicationStatus> statusList7 = List.of(ApplicationStatus.APPROVED, ApplicationStatus.REJECTED);
    List<ApplicationStatus> statusList8 = List.of(ApplicationStatus.CANCELLED);

    List<Application> applicationList6 = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(u3.getUserId(), statusList6);
    List<Application> applicationList7 = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(u3.getUserId(), statusList7);
    List<Application> applicationList8 = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(u3.getUserId(), statusList8);

    assertThat(applicationList6).isNotNull();
    assertThat(applicationList6).hasSize(3);
    assertThat(applicationList6).containsExactlyInAnyOrder(a9, a6, a11);

    assertThat(applicationList7).isNotNull();
    assertThat(applicationList7).hasSize(2);
    assertThat(applicationList7).containsExactlyInAnyOrder(a10, a8);

    assertThat(applicationList8).isNotNull();
    assertThat(applicationList8).hasSize(1);
    assertThat(applicationList8).containsExactly(a7);
  }

  @Test
  @DisplayName("Returns an empty list of applications successfully when there are no applications made by the logged in user")
  void testFindEmptyByUser_UserIdAndApplicationStatusIn() {
    // taking user 3 with no applications made
    List<ApplicationStatus> statusList9 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.UPDATED);
    List<Application> applicationList9 = leaveApplicationRepository.findByUser_UserIdAndApplicationStatusIn(u3.getUserId(), statusList9);

    assertThat(applicationList9).isEmpty();
  }

  @Test 
  @DisplayName("Returns a list of applications successfully based on list of users, list of application statuses and username filtering")
  void testFindByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase() {
    // assuming that the List<User> User are already populated with subordinates objects of the manager,
    // take user 1 and user 2 as the subordinates
    List<User> User = List.of(u1, u2);
    List<ApplicationStatus> statusList9 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.APPROVED);

    // filter
    // Scenario 1 - 4: No blank spaces and capitalisation.
    String filter1 = "Y";
    String filter2 = "y";
    String filter3 = "a";
    String filter4 = "n";

    List<Application> applicationList10 = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(User, statusList9, filter1);
    List<Application> applicationList11 = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(User, statusList9, filter2);
    List<Application> applicationList12 = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(User, statusList9, filter3);
    List<Application> applicationList13 = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(User, statusList9, filter4);

    assertThat(applicationList10).isNotNull();
    assertThat(applicationList10).hasSize(2);
    assertThat(applicationList10).containsExactlyInAnyOrder(a1, a2);

    assertThat(applicationList11).isNotNull();
    assertThat(applicationList11).hasSize(2);
    assertThat(applicationList11).containsExactlyInAnyOrder(a1, a2);

    assertThat(applicationList12).isNotNull();
    assertThat(applicationList12).hasSize(2);
    assertThat(applicationList12).containsExactlyInAnyOrder(a3, a4);

    assertThat(applicationList13).isNotNull();
    assertThat(applicationList13).hasSize(4);
    assertThat(applicationList13).containsExactlyInAnyOrder(a3, a4, a2, a1);
  }

  @Test 
  @DisplayName("Returns an empty list of applications successfully when the searched username does not match any username")
  void testFindEmptyByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase() {
    // assuming that the List<User> User are already populated with subordinates objects of the manager,
    // take user 1 and user 2 as the subordinates
    List<User> User = List.of(u1, u2);
    List<ApplicationStatus> statusList9 = List.of(ApplicationStatus.APPLIED, ApplicationStatus.APPROVED);

    // filter
    // Scenario 5
    String filter5 = "z";

    List<Application> applicationList14 = leaveApplicationRepository.findByUserInAndApplicationStatusInAndUser_UsernameContainingIgnoreCase(User, statusList9, filter5);

    assertThat(applicationList14).isEmpty();
  }

  @Test 
  @DisplayName("Returns an application successfully by logged in user id and application id when the application exist")
  void testFindByUser_UserIdAndApplicationId() {
    // using user 1
    Optional<Application> application1 = leaveApplicationRepository.findByUser_UserIdAndApplicationId(u1.getUserId(), a1.getApplicationId());

    assertThat(application1).isPresent();
    assertThat(application1.get()).isEqualTo(a1);
  }

  @Test
  @DisplayName("Returns an empty application successfully by logged in user id and application id when the application exist but does not belong to the user id")
  void testFindEmptyByUser_UserIdAndApplicationId() {
    // test for url manipulation case
    // using user 3
    Optional<Application> application2 = leaveApplicationRepository.findByUser_UserIdAndApplicationId(u3.getUserId(), a1.getApplicationId());

    assertThat(application2).isEmpty();
  }

  @Test 
  @DisplayName("Returns an empty application successfully by logged in user id and application id when the application does not exist")
  void testFindEmptyByUser_UserIdAndApplicationIdDoesNotExist() {
    // using user 1
    Optional<Application> application3 = leaveApplicationRepository.findByUser_UserIdAndApplicationId(u1.getUserId(), 999);

    assertThat(application3).isEmpty();
  }

  @Test 
  @DisplayName("Returns a list of applications clashing with currently processed application successfully, based on selected application status, start dates and end dates")
  void testFindOverlaps() {
    // create a new set of applications with varying status, assign start date and end dates for the different possible clashing scenarios

    // ignore case (based on AM/PM for both half days leave sharing the same half day slot - not in this Query, handled in service layer)

    // Set 1st application leave from 1/11/2026 to 11/11/2026 (The processed application, should be ignored)
    Application clashTestA1 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,1), LocalDate.of(2026,11,11), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 2nd application leave from 2/11/2026 to 05/11/2026 Full Inner Overlap (Start Date >= :Processed Start Date && End Date <= :Processed End Date)
    Application clashTestA2 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,2), LocalDate.of(2026,11,5), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);

    // Set 3rd application leave from 28/10/2026 to 3/11/2026 Partial Overlap (End Date >= :Processed Start Date)
    Application clashTestA3 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,10,28), LocalDate.of(2026,11,3), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 4th application leave from 10/11/2026 to 12/11/2026 Partial Overlap (Start Date >= :Processed Start Date)
    Application clashTestA4 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,10), LocalDate.of(2026,11,12), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 5th application leave from 20/10/2026 to 20/11/2026 Full Outer Overlap (Start Date <= :Processed Start Date && End Date >= :Processed End Date)
    Application clashTestA5 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,10,20), LocalDate.of(2026,11,20), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);

    // Set 6th application leave from 1/11/2026 to 11/11/2026 Exact Same Overlap (Start Date = :Processed Start Date && End Date =:Processed End Date)
    Application clashTestA6 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,1), LocalDate.of(2026,11,11), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);

    // Set 7th application leave from 28/10/2026 to 01/11/2026 Partial Overlap with same date (End Date = :Processed Start Date)
    Application clashTestA7 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,10,28), LocalDate.of(2026,11,1), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 8th application leave from 11/11/2026 to 15/11/2026 Partial Overlap with same Date (Start Date = :Processed End Date)
    Application clashTestA8 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,11), LocalDate.of(2026,11,15), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);

    // Set 9th application leave from 22/12/2026 to 25/12/2026 No Overlap (Out of Range)
    Application clashTestA9 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,22), LocalDate.of(2026,12,25), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // (Ignored Case) Set 10th, 11th, 12th application leave with random clashing dates, test to check they are excluded
    Application clashTestA10 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,2), LocalDate.of(2026,11,8), ApplicationStatus.DELETED, "DefaultUserReason", null, null);
    Application clashTestA11 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,10), LocalDate.of(2026,11,20), ApplicationStatus.REJECTED, "DefaultUserReason", null, null);
    Application clashTestA12 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,10,10), LocalDate.of(2026,11,1), ApplicationStatus.CANCELLED, "DefaultUserReason", null, null);

 
    entityManager.persist(clashTestA1);
    entityManager.persist(clashTestA2);
    entityManager.persist(clashTestA3);
    entityManager.persist(clashTestA4);
    entityManager.persist(clashTestA5);
    entityManager.persist(clashTestA6);
    entityManager.persist(clashTestA7);
    entityManager.persist(clashTestA8);
    entityManager.persist(clashTestA9);
    entityManager.persist(clashTestA10);
    entityManager.persist(clashTestA11);
    entityManager.persist(clashTestA12);

    entityManager.flush();
    entityManager.clear();

    List<Application> overlapList1 = leaveApplicationRepository.findOverlaps(clashTestA1.getApplicationId(), clashTestA1.getStartDate(), clashTestA1.getEndDate());

    assertThat(overlapList1).isNotNull();
    assertThat(overlapList1).hasSize(10); // 7 + 3 (BeforeEach)
    assertThat(overlapList1).containsExactlyInAnyOrder(clashTestA2, clashTestA3, clashTestA4, clashTestA5, clashTestA6, clashTestA7, clashTestA8, a1, a2, a3);
    assertThat(overlapList1).doesNotContain(clashTestA1, clashTestA9, clashTestA10, clashTestA11, clashTestA12);

    // Check for no overlaps, returning empty
    // Using A9 as reference
    List<Application> overlapList2 = leaveApplicationRepository.findOverlaps(clashTestA9.getApplicationId(), clashTestA9.getStartDate(), clashTestA9.getEndDate());

    assertThat(overlapList2).isEmpty();
  }

  @Test 
  @DisplayName("Returns a list of applications involved / clashing with the selected year (within or crossover) based on user id, selected application status, start date of selected year and end date of selected year")
  void testFindAnyDeductingCrossYearOrWithinYearApplications() {
    // create a new set of applications with varying status, assign start date and end dates and determine whether they fall within the selected year, or fall in between 2 selected years
    // if an application fall in between 2 years, the application must be included in the 2 years

    // Assuming year 2026
    LocalDate startDate = LocalDate.of(2026,1,1);
    LocalDate endDate = LocalDate.of(2026,12,31);

    // Set 1st application leave within the year
    Application yearTestA1 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,1), LocalDate.of(2026,11,11), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 2nd application leave that cross over to 2027
    Application yearTestA2 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,28), LocalDate.of(2027,1,5), ApplicationStatus.APPROVED, "DefaultUserReason", null, null);

    // Set 3rd application leave that cross over from 2025 to 2026
    Application yearTestA3 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2025,12,29), LocalDate.of(2026,1,2), ApplicationStatus.APPLIED, "DefaultUserReason", null, null);

    // Set 4nd application leave with cancelled status within the year
    Application yearTestA4 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,28), LocalDate.of(2027,11,29), ApplicationStatus.CANCELLED, "DefaultUserReason", null, null);

    // Set 5nd application leave with deleted status that cross over to 2027
    Application yearTestA5 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,12,30), LocalDate.of(2027,1,5), ApplicationStatus.DELETED, "DefaultUserReason", null, null);

    // Set 6nd application leave with rejected status that cross over from 2025 to 2026
    Application yearTestA6 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2025,12,30), LocalDate.of(2026,1,5), ApplicationStatus.REJECTED, "DefaultUserReason", null, null);

    // Set 7th application leave after 2026
    Application yearTestA7 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2027,2,1), LocalDate.of(2027,2,11), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);

    // Set 8th application leave before 2026
    Application yearTestA8 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2025,11,5), LocalDate.of(2025,11,11), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);

    // Set 1st - 8th application belonging to user 1
    yearTestA1.setUser(u1);
    yearTestA2.setUser(u1);
    yearTestA3.setUser(u1);
    yearTestA4.setUser(u1);
    yearTestA5.setUser(u1);
    yearTestA6.setUser(u1);
    yearTestA7.setUser(u1);
    yearTestA8.setUser(u1);
 
    // Set 9th application belonging to user 2
    Application yearTestA9 = new Application(LeaveType.ANNUAL, LeaveDuration.FULLDAY, LocalDate.of(2026,11,20), LocalDate.of(2026,11,22), ApplicationStatus.UPDATED, "DefaultUserReason", null, null);

    yearTestA9.setUser(u2);

    entityManager.persist(yearTestA1);
    entityManager.persist(yearTestA2);
    entityManager.persist(yearTestA3);
    entityManager.persist(yearTestA4);
    entityManager.persist(yearTestA5);
    entityManager.persist(yearTestA6);
    entityManager.persist(yearTestA7);
    entityManager.persist(yearTestA8);
    entityManager.persist(yearTestA9);

    entityManager.flush();
    entityManager.clear();

    List<Application> yearOverlapList1 = leaveApplicationRepository.findAnyDeductingCrossYearOrWithinYearApplications(u1.getUserId(), startDate, endDate);

    assertThat(yearOverlapList1).isNotNull();
    assertThat(yearOverlapList1).hasSize(5); // 3 + 2 (BeforeEach)
    assertThat(yearOverlapList1).containsExactlyInAnyOrder(yearTestA1, yearTestA2, yearTestA3, a1, a2);
    assertThat(yearOverlapList1).doesNotContain(yearTestA9);
  }

  @AfterEach 
	void tearDownData() {
    u1 = u2 = u3 = null;
    a1 = a2 = a3 = a4 = a5 = null;
  }
}
