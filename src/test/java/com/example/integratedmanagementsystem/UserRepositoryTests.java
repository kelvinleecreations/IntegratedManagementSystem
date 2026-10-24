package com.example.integratedmanagementsystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.integratedmanagementsystem.model.Role;
import com.example.integratedmanagementsystem.model.User;
import com.example.integratedmanagementsystem.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	private User u1, u2, u3, u4, u5, u6, u7, u8, u9, u10, u11, u12, u13, u14, u15;
	private Integer u1Id, u2Id, u3Id, u4Id, u5Id, u6Id, u7Id, u8Id, u9Id, u10Id, u11Id, u12Id, u13Id, u14Id, u15Id;

	@BeforeEach 
	void setUpData() {

		u1 = new User("John", "johnny", "john@email.com", "DefaultPassword", Role.ROLE_CEO, null);
		u2 = new User("Sarah", "sera", "sarah@email.com", "DefaultPassword", Role.ROLE_HR_DIRECTOR, u1);
		u3 = new User("Christina", "chrissy", "christina@email.com", "DefaultPassword", Role.ROLE_FINANCE_DIRECTOR, u1);
		u4 = new User("Natalie", "nat", "natalie@email.com", "DefaultPassword", Role.ROLE_HR_MANAGER_I, u2);
		u5 = new User("Natty", "natty", "natty@email.com", "DefaultPassword", Role.ROLE_HR_MANAGER_R, u2);
		u6 = new User("Tim", "timmy", "tim@email.com", "DefaultPassword", Role.ROLE_ADMIN, null);
		u7 = new User("Vela", "vela", "vela@email.com", "DefaultPassword", Role.ROLE_HR_INTERNAL, u4);
		u8 = new User("Florence", "florence", "florence@email.com", "DefaultPassword", Role.ROLE_HR_RECRUITER, u5);
		u9 = new User("Nico", "nico", "nico@email.com", "DefaultPassword", Role.ROLE_HR_INTERNAL, u4);
		u10 = new User("Eva", "eva", "eva@email.com", "DefaultPassword", Role.ROLE_HR_RECRUITER, u5);
		u11 = new User("Lina", "lina", "lina@email.com", "DefaultPassword", Role.ROLE_FINANCE_MANAGER, u3);
		u12 = new User("Evangeline", "evangeline", "evangeline@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, u11);
		u13 = new User("Angel", "angel", "angel@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, u11);
		u14 = new User ("Eugene", "eugene", "eugene@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, u11);
		u15 = new User ("Eleanor", "eleanor", "eleanor@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, u11);

		entityManager.persist(u1);
		entityManager.persist(u2);
		entityManager.persist(u3);
		entityManager.persist(u4);
		entityManager.persist(u5);
		entityManager.persist(u6);
		entityManager.persist(u7);
		entityManager.persist(u8);
		entityManager.persist(u9);
		entityManager.persist(u10);
		entityManager.persist(u11);
		entityManager.persist(u12);
		entityManager.persist(u13);
		entityManager.persist(u14);
		entityManager.persist(u15);

		entityManager.flush();

		u1Id = u1.getUserId();
		u2Id = u2.getUserId();
		u3Id = u3.getUserId();
		u4Id = u4.getUserId();
		u5Id = u5.getUserId();
		u6Id = u6.getUserId();
		u7Id = u7.getUserId();
		u8Id = u8.getUserId();
		u9Id = u9.getUserId();
		u10Id = u10.getUserId();
		u11Id = u11.getUserId();
		u12Id = u12.getUserId();
		u13Id = u13.getUserId();
		u14Id = u14.getUserId();
		u15Id = u15.getUserId();

		entityManager.clear();
	}

	@Nested 
	@DisplayName("Create, Update, Delete User Tests")
	class Crud {
		@Test
		@DisplayName("Create User Successfully")
		void testCreateUser() {
			User newUser = new User("Bryce", "bryce", "bryce@email.com", "DefaultPassword", Role.ROLE_FINANCE_EMPLOYEE, u11);

			User savedUser = userRepository.save(newUser);

			assertThat(savedUser.getUserId()).isNotNull();
			assertThat(savedUser.getUsername()).isEqualTo("bryce");
		}

		@Test
		@DisplayName("Update User Successfully")
		void testUpdateUser() {
			User updateUser = userRepository.findById(u6Id).orElse(null);

			updateUser.setManager(u1);
			updateUser.setPassword("UpdatedPassword");

			User updatedUser = userRepository.save(updateUser);

			assertThat(updatedUser.getManager()).isNotNull();
			assertThat(updatedUser.getManager()).isEqualTo(u1);
			assertThat(updatedUser.getPassword()).isEqualTo("UpdatedPassword");
		}

		@Test
		@DisplayName("Delete User Successfully")
		void testDeleteUser() {
			userRepository.deleteById(u5Id);
			Optional<User> deletedUser = userRepository.findById(u5Id);
			assertThat(deletedUser).isEmpty();
		}
	}
	
	@Nested 
	@DisplayName("Custom Query Tests")
	class CustomQuery {
		@Test
		@DisplayName("Successfully find a user by their username when the username exist")
		void testFindByUsername() {
			Optional<User> findUser1 = userRepository.findByUsername("sera");
			Optional<User> findUser2 = userRepository.findByUsername("mandy");

			assertThat(findUser1).isPresent();
			assertThat(findUser1.get().getName()).isEqualTo("Sarah");
			assertThat(findUser2).isNotPresent();
		}


		@Test
		@DisplayName("Successfully checks if a username exists")
		void testExistsByUsername() {
			assertThat(userRepository.existsByUsername("chrissy")).isTrue();
			assertThat(userRepository.existsByUsername("mandy")).isFalse();
		}

		@Test
		@DisplayName("Successfully checks if a username is already used by another user")
		void testExistsByUsernameAndUserIdNot() {
			// Is 'Nat' already claimed by others when User ID 5's profile is being edited by the Administrator? Yes, because it belongs to User ID 4.
			boolean usernameTakenByOthers = userRepository.existsByUsernameAndUserIdNot("nat", u5Id);
			assertThat(usernameTakenByOthers).isTrue();

			// Is 'Nat' already claimed by others when User ID 4's profile is being edited by the Administrator? No, because it belongs to User ID 4.
			usernameTakenByOthers = userRepository.existsByUsernameAndUserIdNot("nat", u4Id);
			assertThat(usernameTakenByOthers).isFalse();
		}

		@Test 
		@DisplayName("Successfully returns a list of managers who have at least one or more subordinates")
		void testFindAllManagers() {
			List<User> managers = userRepository.findAllManagers();

			assertThat(managers).hasSize(6);
			assertThat(managers).extracting(User::getName).containsExactlyInAnyOrder("Christina", "John", "Sarah", "Natty", "Natalie", "Lina");	
		}

		@Test 
		@DisplayName("Successfully returns a list of users together with their subordinates if any")
		void testFindAllWithSubordinates() {
			List<User> allUsers = userRepository.findAllWithSubordinates();
			assertThat(allUsers).hasSize(15);

			// Check for individual users, with their subordinate count from allUsers
			User sarah = null;
			for (User u : allUsers) {
				if (u.getUserId().equals(u2Id)) {
					sarah = u;
					break;
				}
				continue;
			}
				
			assertThat(sarah).isNotNull();
			assertThat(sarah.getSubordinates()).hasSize(2);

			User eleanor = null;
			for (User u : allUsers) {
				if (u.getUserId().equals(u15Id)) {
					eleanor = u;
					break;
				}
				continue;
			}
				
			assertThat(eleanor).isNotNull();
			assertThat(eleanor.getSubordinates()).hasSize(0);
		}

		@Test
		@DisplayName("Successfully removes the manager of affected subordinate when a manager is about to be deleted")
		void testUnlinkSubordinates() {
			// This is a Modifying Query, need to include entity manager, so that updates can be displayed instantly within the time frame of the test

			// Unlink Natalie and Natty, who are subordinates of Sarah
			userRepository.unlinkSubordinates(u2Id);
			entityManager.flush();
			entityManager.clear();

			User natalie = userRepository.findById(u4Id).orElse(null);
			User natty = userRepository.findById(u5Id).orElse(null);

			assertThat(natalie.getManager()).isNull();
			assertThat(natty.getManager()).isNull();
		}

		@Test 
		@DisplayName("Successfully remove the reporting manager of the user who is about to be deleted")
		void testUnlinkManager() {
			// This is a Modifying Query, need to include entity manager, so that updates can be displayed instantly within the time frame of the test

			// Unlink Lina, who is the reporting manager of Eugene
			userRepository.unlinkManager(u14Id);
			entityManager.flush();
			entityManager.clear();

			User lina = userRepository.findById(u11Id).orElse(null);

			assertThat(lina.getSubordinates()).extracting(User::getName).containsExactlyInAnyOrder("Evangeline", "Eleanor", "Angel");
		}

		// 2 more tests for 2 pagination methods in user repository
		@Test 
		@DisplayName("Successfully get user with subordinates and manager simultaneously if any and apply pagination correctly")
		void testFindAllWithSubordinatesAndManager() {
			// Initialise a manager
			User manager = new User();
			manager.setName("Manager1");
			manager.setUsername("manager1");
			manager.setEmail("mngr1@email.com");
			manager.setPassword("DefaultPassword");
			manager.setRole(Role.ROLE_HR_MANAGER_I);
			userRepository.save(manager);

			// Formulate 50 dummy data quickly
			int totalUsers = 100;
			List<User> userList = new ArrayList<>();

			for (int i = 1; i <= totalUsers; i++) {
				User staff = new User();
				staff.setName("Staff" + i);
				staff.setUsername("staff" + i);
				staff.setEmail("s" + i + "@email.com");
				staff.setPassword("DefaultPassword");
				staff.setRole(Role.ROLE_HR_RECRUITER);
				staff.setManager(manager);

				userList.add(staff);
			}

			userRepository.saveAll(userList);

			entityManager.flush();
			entityManager.clear();

			// Apply pagination
			Pageable pageable = PageRequest.of(0,10);

			Page<User> userPage = userRepository.findAllWithSubordinatesAndManager(pageable);

			assertThat(userPage.getContent()).isNotEmpty();
			assertThat(userPage.getTotalElements()).isEqualTo(116); // 100 (For-Loop) + 1 (Manager) + 15 (BeforeEach)
			assertThat(userPage.getTotalPages()).isEqualTo(12);

			// Check whether it gets subordinates and manager in a single query, avoiding 1+N / 1+N+N query performance (executing 1 initial query, followed by N additional ones for the subordinates and manager)

			for (User u : userPage.getContent()) {
				// Check whether Hibernate successfully obtained manager data during the initial database query and populated the manager into User objects, without the need of additional query
				assertThat(Hibernate.isInitialized(u.getManager())).as("Manager loaded in a single query").isTrue();

				// Check whether Hibernate successfully obtained subordinates data during the initial database query and populated the subordinates list into User objects, without the need of additional query
				assertThat(Hibernate.isInitialized(u.getSubordinates())).as("Subordinates should be loaded in a single query").isTrue();
			}
		}

		@Test 
		@DisplayName("Successfully filters users by username containing ignoring case and applying the filter throughout the different pages")
		void testFindByUsernameContainingIgnoreCase() {

			Pageable pageable1 = PageRequest.of(0,5);
			Pageable pageable2 = PageRequest.of(1,5);

			// Scenario 1, 2, 3: No blank spaces and capitalisation. Expecting 3 rows, 1 page
			String filter1 = "V";
			String filter2 = "v";

			Page<User> filterPage1 = userRepository.findByUsernameContainingIgnoreCase(filter1, pageable1);
			Page<User> filterPage2 = userRepository.findByUsernameContainingIgnoreCase(filter2, pageable1);

			assertThat(filterPage1.getContent()).isNotEmpty();
			assertThat(filterPage1.getTotalElements()).isEqualTo(3);
			assertThat(filterPage1.getTotalPages()).isEqualTo(1);
			assertThat(filterPage1).extracting(User::getUsername).containsExactlyInAnyOrder("evangeline", "vela", "eva");

			assertThat(filterPage2.getContent()).isNotEmpty();
			assertThat(filterPage2.getTotalElements()).isEqualTo(3);
			assertThat(filterPage2.getTotalPages()).isEqualTo(1);
			assertThat(filterPage2).extracting(User::getUsername).containsExactlyInAnyOrder("evangeline", "vela", "eva");

			// Scenario 3: Expecting no results, 0 rows, 1 page
			String filter3 = "B";
			Page<User> filterPage3 = userRepository.findByUsernameContainingIgnoreCase(filter3, pageable1);

			assertThat(filterPage3.getContent()).isEmpty();
			assertThat(filterPage3.getTotalElements()).isEqualTo(0);
			assertThat(filterPage3.getTotalPages()).isEqualTo(0);

			// Scenario 5: Expecting more results till 2 pages
			String filter4 = "E";
			Page<User> filterPage4 = userRepository.findByUsernameContainingIgnoreCase(filter4, pageable1);
			Page<User> filterPage5 = userRepository.findByUsernameContainingIgnoreCase(filter4, pageable2);

			// Expected List
			List<String> usernamesList = List.of("evangeline", "vela", "eva", "eleanor", "angel", "florence", "eugene", "sera");

			// First Page
			assertThat(filterPage4.getContent()).isNotEmpty();
			assertThat(filterPage4.getContent()).hasSize(5);
			assertThat(filterPage4.getTotalElements()).isEqualTo(8);
			assertThat(filterPage4.getTotalPages()).isEqualTo(2);
			assertThat(filterPage4).extracting(User::getUsername).isSubsetOf(usernamesList);

			// Second Page
			assertThat(filterPage5.getContent()).isNotEmpty();
			assertThat(filterPage5.getContent()).hasSize(3);
			assertThat(filterPage5.getTotalElements()).isEqualTo(8);
			assertThat(filterPage5.getTotalPages()).isEqualTo(2);
			assertThat(filterPage5).extracting(User::getUsername).isSubsetOf(usernamesList);

			// Ensure no overlap between the 2 pages
			assertThat(filterPage5).extracting(User::getUsername).doesNotContainAnyElementsOf(filterPage4.map(User::getUsername));
		}

		// 2 more tests for 2 my team methods in user repository
		@Test
		@DisplayName("Successfully finds and returns other users reporting to the manager, excluding the user that is currently logged in")
		void testFindByManagerUserIdAndUserIdNot() {

			// Assuming logged in user is Florence,
			// Checks her reporting manager, Natty and displays other users under her - Natty.
			List<User> otherUsers = userRepository.findByManagerUserIdAndUserIdNot(u5Id, u8Id);

			assertThat(otherUsers).hasSize(1);
			assertThat(otherUsers).extracting(User::getUsername).containsExactly("eva");

			for (User u : otherUsers) {
				assertThat(Hibernate.isInitialized(u.getManager())).isTrue();
			}
		}

		@Test 
		@DisplayName("Successfully find the logged in user's subordinates if any")
		void testFindWithSubordinatesByUserId() {
			
			// Assuming logged in user is John,
			// Checks his subordinates and display if any
			Optional<User> managerOptional = userRepository.findWithSubordinatesByUserId(u1Id);

			assertThat(managerOptional.get().getSubordinates()).hasSize(2);
			assertThat(managerOptional.get().getSubordinates()).extracting(User::getUsername).containsExactlyInAnyOrder("chrissy", "sera");

			assertThat(Hibernate.isInitialized(managerOptional.get().getSubordinates())).isTrue();
		}
	}

	@AfterEach 
	void tearDownData() {
		u1 = u2 = u3 = u4 = u5 = u6 = u7 = u8 = u9 = u10 = u11 = u12 = u13 = u14 = u15 = null;
	}
}
