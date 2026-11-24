# IntegratedManagementSystem
## About
A **basic**, (majority) server-side rendering demo web application project built using Java as the main programming language and MySQL as the database.

It contains:
1. [Completed] **User Management System**
2. [Completed] **Leave Management System**
3. [In Progress] **HR Management System**

Roles Involved: **Administrators** and **Staff** (representing the different roles)

## Overview of Technology Stack
- **Frontend**: HTML, CSS, JavaScript, Thymeleaf
- **Backend**: Spring Boot
- **Database**: MySQL
- **AI**: Ollama LLM

## Key Features By System
| System | Feature | Information | Targeted Role |
| :--- | :--- | :--- | :--- |
| User Management System | Employee Management | Add, Modify, Delete users | Administrators |
| User Management System | My Team | View Team Members, Reporting Manager and Direct Subordinates | Staff |
| User Management System | My Profile | Modify credentials | All |
| Leave Management System | Leave Application State Management | Submit application and flow is determined by user's choice | Staff |
| Leave Management System | Entitlement Calculations | Dynamic changes based on year for Annual Leave (AL) and children count for Childcare Leave (CL) | Staff |
| Leave Management System | Calendar | Displays team approved leave applications | Staff |
| Leave Management System | Leave Clash Analysis | Displays team clashing leave application during review | Managers |
| HR Management System | Basic Resume Analysis | Resume review and ranking | HR Recruiters |

## System Design Implementations
| Technique | Use Case(s) |
| :--- | :--- |
| Rate Limiting | Login & Analyse candidate resume |
| Caching | Analyse candidate resume |

## Thank you ##









