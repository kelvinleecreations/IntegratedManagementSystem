-- Users Demo Data --
INSERT into users (name, username, email, password, role, manager_user_id, actual_annual_leave_days, budgeted_annual_leave_days, actual_hospitalization_leave_days, budgeted_hospitalization_leave_days, marital_status, children_count, actual_childcare_leave_days, budgeted_childcare_leave_days, date_of_join) VALUES -- establish ceo and admin
  ('Kelvin', 'kels', 'ceo@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_CEO', null, null, null, null, null, null, null, null, null, null), -- ceo
  ('Bryan', 'bryy', 'admin@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_ADMIN', null, null, null, null, null, null, null, null, null, null); -- admin user
  
INSERT into users (name, username, email, password, role, manager_user_id, actual_annual_leave_days, budgeted_annual_leave_days, actual_hospitalization_leave_days, budgeted_hospitalization_leave_days, marital_status, children_count, actual_childcare_leave_days, budgeted_childcare_leave_days, date_of_join) VALUES -- establish directors (direct subordinates of ceo)
  ('Kelly', 'kellyy', 'hrdirector@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_DIRECTOR', null, 14.0, 14.0, 60.0, 60.0, 'MARRIED', 3, 12.0, 12.0, '2018-01-01'), -- manages hr managers
  ('Benjamin', 'benn', 'financedirector@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_DIRECTOR', null, 14.0, 14.0, 60.0, 60.0, 'MARRIED', 2, 10.0, 10.0, '2018-05-05'); -- manages finance managers

INSERT into users (name, username, email, password, role, manager_user_id, actual_annual_leave_days, budgeted_annual_leave_days, actual_hospitalization_leave_days, budgeted_hospitalization_leave_days, marital_status, children_count, actual_childcare_leave_days, budgeted_childcare_leave_days, date_of_join) VALUES -- establish managers (direct subordinates of directors)
  ('Elizabeth', 'lizz', 'hrmanager1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_R', null, 12.0, 12.0, 60.0, 60.0, 'MARRIED', 1, 8.0, 8.0, '2022-02-08'),
  ('Cassandra', 'cass', 'hrmanager2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_I', null, 12.0, 12.0, 60.0, 60.0, 'MARRIED', 1, 8.0, 8.0, '2022-03-05'),
  ('Yuna', 'yuna', 'hrmanager3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_R', null, 12.0, 12.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2022-05-06'),
  ('Gloria', 'gloria', 'hrmanager4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_I', null, 12.0, 12.0, 60.0, 60.0, 'MARRIED', 1, 8.0, 8.0, '2022-08-08'),
  ('John', 'john', 'financemanager1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_MANAGER', null, 12.0, 12.0, 60.0, 60.0, 'MARRIED', 2, 10.0, 10.0, '2022-10-11'),
  ('Andy', 'andy', 'financemanager2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_MANAGER', null, 12.0, 12.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2022-09-20');

INSERT into users (name, username, email, password, role, manager_user_id, actual_annual_leave_days, budgeted_annual_leave_days, actual_hospitalization_leave_days, budgeted_hospitalization_leave_days, marital_status, children_count, actual_childcare_leave_days, budgeted_childcare_leave_days, date_of_join) VALUES -- establish employees (direct subordinates of managers)
  ('Samantha', 'sams', 'hr1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 10.0, 10.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2024-02-12'),
  ('Fiona', 'fion', 'hr2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 9.0, 9.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-02-20'),
  ('Evelyn', 'evelyn', 'hr3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 10.0, 10.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2024-05-11'),
  ('Xenia', 'xenia', 'hr4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 9.0, 9.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-06-12'),
  ('Tammy', 'tammy', 'hr5@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 8.0, 8.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2026-02-20'),
  ('Cathy', 'cathy', 'hr6@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 9.0, 9.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2025-08-10'),
  ('Olivia', 'olivia', 'hr7@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 10.0, 10.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2024-09-12'),
  ('Mary', 'mary', 'hr8@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 9.0, 9.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-03-05'),
  ('Tommy', 'tom', 'hr9@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 10.0, 10.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2024-01-20'),
  ('Sandra', 'sandra', 'hr10@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 9.0, 9.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2025-09-26'),
  ('Mandy', 'mandy', 'hr11@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null, 8.0, 8.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-12-29'),
  ('Eunice', 'eunice', 'hr12@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 8.0, 8.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-11-28'),
  ('Daisy', 'daisy', 'hr13@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null, 9.0, 9.0, 60.0, 60.0,'SINGLE', 0, 0.0, 0.0, '2025-06-22'),
  ('Matthew', 'matt', 'finance1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null, 9.0, 9.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2025-03-08'),
  ('Henry', 'henry', 'finance2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null, 9.0, 9.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2025-03-20'),
  ('Ryan', 'ryan', 'finance3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null, 9.0, 9.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2025-02-01'),
  ('Clarice', 'clarice', 'finance4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null, 10.0, 10.0, 60.0, 60.0, 'MARRIED', 0, 0.0, 0.0, '2024-02-15'),
  ('Bob', 'bob', 'finance5@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null, 10.0, 10.0, 60.0, 60.0, 'SINGLE', 0, 0.0, 0.0, '2024-03-08');

-- Hierarchy System --
-- Kelly and Benjamin report to Kelvin
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'kels'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('kellyy', 'benn');

-- Elizabeth, Cassandra, Yuna and Gloria report to Kelly
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'kellyy'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('lizz', 'cass', 'yuna', 'gloria');

-- John and Andy report to Benjamin
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'benn'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('john', 'andy');

-- Samantha, Fiona and Evelyn report to Elizabeth
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'lizz'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('sams', 'fion', 'evelyn');

-- Xenia, Tammy, Cathy, Olivia and Mary report to Cassandra
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'cass'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('xenia', 'tammy', 'cathy', 'olivia', 'mary');

-- Tommy, Sandra, Mandy report to Yuna
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'yuna'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('tom', 'sandra', 'mandy');

-- Eunice and Daisy report to Gloria
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'gloria'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('eunice', 'daisy');

-- Matthew, Henry and Ryan report to John
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'john'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('matt', 'henry', 'ryan');

-- Clarice and Bob report to Andy
UPDATE users AS subordinate 
INNER JOIN users manager ON manager.username = 'andy'
SET subordinate.manager_user_id = manager.user_id
WHERE subordinate.username IN ('clarice', 'bob');














