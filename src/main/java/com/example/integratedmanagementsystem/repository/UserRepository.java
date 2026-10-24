package com.example.integratedmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.integratedmanagementsystem.model.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  // for CustomUserDetailsService
  public Optional<User> findByUsername(String username);

  // for UserForm
  // Finds all users who are managers, 
  // Inner Join filter out users who have no subordinates. 
  // Fetch tells Hibernate to load managers and subordinates data simultaneously
  // Order by arrange the rows in alphabetical order of the manager's name
  @Query("SELECT DISTINCT m FROM User m INNER JOIN FETCH m.subordinates ORDER BY m.name ASC")
  public List<User> findAllManagers();

  // for UserForm
  public boolean existsByUsername(String username);

  // for UserForm
  public boolean existsByUsernameAndUserIdNot(String username, Integer userId);

  // Load all users with subordinates simultaneously in a single query
  // For administration.html u.subordinates.size()>0 error
  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.subordinates")
  public List<User> findAllWithSubordinates();

  // Database Update Operations in JPQL
  // Transactional informs Spring to open a database transaction before running the method and commit it immediately after
  // Modifying informs Spring that this query will modify data rather than to read
  // By default, Hibernate keeps an internal memory snapshot of data. If there are any changes in the database, Hibernate's memory will become outdated. Clear Automatic tells Hibernate to wipe the memory after query runs, forcing it to fetch updated data.

  // Unlink all employees who report to the user that is being deleted, preventing Foreign Key Constraint Error
  // Update User entity table, set manager_user_id as null, for employees whose manager has userId matching the parameter variable
  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE User u SET u.manager = null WHERE u.manager.userId = :managerId")
  public void unlinkSubordinates(Integer managerId);

  // Unlink the deleted user's reporting manager
  // Update User entity table, set own manager column to null, for the user with userId matching the parameter variable
  @Transactional
  @Modifying (clearAutomatically = true)
  @Query("UPDATE User u SET u.manager = null WHERE u.userId = :userId")
  public void unlinkManager(Integer userId);

  // For Pagination
  // Get Users, manager and the subordinates simultaneously
  @EntityGraph(attributePaths = {"subordinates", "manager"})
  @Query("SELECT DISTINCT u from User u")
  public Page<User> findAllWithSubordinatesAndManager(Pageable pageable);

  // Filtering Function
  @EntityGraph(attributePaths = {"subordinates", "manager"})
  public Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

  // For My Team Function
  @EntityGraph(attributePaths = {"manager"})
  // Find and return other users reporting to the manager by the manager id, excluding the user that is currently logged in
  public List<User> findByManagerUserIdAndUserIdNot(Integer managerId, Integer userId);

  @EntityGraph(attributePaths = {"subordinates"})
  // Find the logged in user's subordinates if any
  Optional<User> findWithSubordinatesByUserId(Integer userId);


}
