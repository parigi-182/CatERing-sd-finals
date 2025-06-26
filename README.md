# CatERing – Catering Company Management Software

**CatERing** is the final assignment for the *Software Development* course.  
It simulates a management system for a catering company, handling menus, staff shifts, users, and more.

The project is structured around several core managers, each responsible for a different domain in the system.

## 🧠 Managers Overview

The application uses a modular architecture where business logic is encapsulated in specialized *Manager* classes:

- **MenuManager**:  
  Handles all use cases related to menu creation and management.  
  You can create a menu, define sections, add dishes, reorder items, and remove components.

- **UserManager**:  
  Manages the login and current user context. Required to perform operations (e.g., only logged-in users can edit menus).

- **EventManager**:  
  Handles event planning. Though not fully implemented, it's designed to associate menus and shifts with events.

- **RecipeManager**:  
  Manages the catalog of available recipes that can be added to menus. Functionality is limited in this version.

Each manager exposes public methods to access the business logic while hiding implementation details.  
They are instantiated and coordinated by the `CatERing` singleton class, which serves as the main service locator.

## 🧪 Assignment Focus

As students, we were asked to implement the **menu management use cases** through Java code and test classes.  
The current project contains several manual test cases that simulate real-world usage.

> The user interface (UI) was provided by the course instructors but has **not been considered** in this implementation,  
> since the assignment focused solely on backend logic and functionality.

The project is still a **work in progress**:  
a future refactor is planned to improve structure and maintainability, and JUnit testing may be introduced later.
