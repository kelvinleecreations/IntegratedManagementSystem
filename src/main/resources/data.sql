-- Users Demo Data --
INSERT into users (name, username, email, password, role, manager_user_id) VALUES -- establish ceo and admin
  ('Kelvin', 'kels', 'ceo@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_CEO', null), -- ceo
  ('Bryan', 'bryy', 'admin@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_ADMIN', null); -- admin user
  
INSERT into users (name, username, email, password, role, manager_user_id) VALUES -- establish directors (direct subordinates of ceo)
  ('Kelly', 'kellyy', 'hrdirector@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_DIRECTOR', null), -- manages all hr employees
  ('Benjamin', 'benn', 'financedirector@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_DIRECTOR', null); -- managers all finance employees

INSERT into users (name, username, email, password, role, manager_user_id) VALUES -- establish managers (direct subordinates of directors)
  ('Elizabeth', 'lizz', 'hrmanager1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_R', null),
  ('Cassandra', 'cass', 'hrmanager2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_I', null),
  ('Yuna', 'yuna', 'hrmanager3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_R', null),
  ('Gloria', 'gloria', 'hrmanager4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_MANAGER_I', null),
  ('John', 'john', 'financemanager1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_MANAGER', null),
  ('Andy', 'andy', 'financemanager2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_MANAGER', null);

INSERT into users (name, username, email, password, role, manager_user_id) VALUES -- establish employees (direct subordinates of managers)
  ('Samantha', 'sams', 'hr1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Fiona', 'fion', 'hr2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Evelyn', 'evelyn', 'hr3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Xenia', 'xenia', 'hr4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Tammy', 'tammy', 'hr5@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Cathy', 'cathy', 'hr6@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Olivia', 'olivia', 'hr7@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Mary', 'mary', 'hr8@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Tommy', 'tom', 'hr9@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Sandra', 'sandra', 'hr10@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Mandy', 'mandy', 'hr11@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_RECRUITER', null),
  ('Eunice', 'eunice', 'hr12@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Daisy', 'daisy', 'hr13@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_HR_INTERNAL', null),
  ('Matthew', 'matt', 'finance1@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null),
  ('Henry', 'henry', 'finance2@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null),
  ('Ryan', 'ryan', 'finance3@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null),
  ('Clarice', 'clarice', 'finance4@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null),
  ('Bob', 'bob', 'finance5@email.com', '$2a$10$XX.htgpTqjfQG5DH0kCA5OtqbYh7t1pxWI3hkXChs8Qu9/LCUGDjy', 'ROLE_FINANCE_EMPLOYEE', null);

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














