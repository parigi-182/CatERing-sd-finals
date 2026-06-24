# CatERing

Management system for a catering company, developed as the final assignment for the Software Development course at University of Turin. The application handles menu composition, event and service planning, kitchen task scheduling, and staff shift management. The backend logic is fully implemented in Java; a JavaFX-based user interface was provided by course instructors and is included in the repository but was not part of the assignment scope.

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 8 (source/target 1.8) |
| UI Framework | JavaFX 19.0.2 (controls, FXML) + javafx-base 21-ea+5 |
| Database | MySQL 8.0.33 |
| JDBC Driver | mysql-connector-java 8.0.33 |
| Build Tool | Maven |
| Module System | Java Platform Module System (JPMS) — `com.catering` module, requires `javafx.graphics` transitively |
| Database runtime | Docker Compose (MySQL 8.0.33 + Adminer 4.8.1) |

---

## Project Structure

```
CatERing-sd-finals/
├── database/
│   ├── catering_init_03.sql       # Full schema + seed data (MySQL dump)
│   ├── dbreset.sql                # Truncates mutable tables and re-inserts seed data
│   └── docker-compose.yml         # MySQL + Adminer containers
├── src/main/java/catering/
│   ├── CatERingApp.java           # JavaFX Application entry point
│   ├── TestCatERing.java          # Manual integration test: UC1a (create menu, sections, items, publish)
│   ├── TestCatERing1b.java        # Manual integration test: UC1b (delete menu)
│   ├── TestCatERing1c.java        # Manual integration test: UC1c (copy menu)
│   ├── TestCatERing2a.java        # Manual integration test: UC2a (delete section)
│   ├── TestCatERing2b.java        # Manual integration test: UC2b (rename section)
│   ├── TestCatERing2c.java        # Manual integration test: UC2c (reorder sections)
│   ├── TestCatERing2d.java        # Manual integration test: UC2d (reorder items within section)
│   ├── TestCatERing2ef.java       # Manual integration test: UC2e/f (move items between sections)
│   ├── TestCatERing4a.java        # Manual integration test: UC4a (assign item to section)
│   ├── TestCatERing4b.java        # Manual integration test: UC4b (edit item description)
│   ├── TestCatERing4c.java        # Manual integration test: UC4c (delete item)
│   ├── businesslogic/
│   │   ├── CatERing.java          # Singleton service locator
│   │   ├── UseCaseLogicException.java
│   │   ├── menu/                  # Fully implemented
│   │   ├── user/                  # Implemented (fakeLogin, role checks)
│   │   ├── recipe/                # Implemented (read-only catalog)
│   │   ├── event/                 # Stub / partially implemented
│   │   ├── kitchen/               # Partially implemented
│   │   ├── shift/                 # Partially implemented
│   │   └── utils/
│   │       └── State.java         # Enum: Scheduled, Completed, Cancelled
│   ├── persistence/
│   │   ├── PersistenceManager.java    # JDBC connection and query execution
│   │   ├── MenuPersistence.java       # MenuEventReceiver implementation — persists all menu events
│   │   ├── ResultHandler.java         # Functional interface for ResultSet row handling
│   │   └── BatchUpdateHandler.java    # Functional interface for PreparedStatement batch population
│   └── ui/                        # JavaFX controllers and FXML files (provided, not part of assignment)
├── module-info.java
└── pom.xml
```

---

## Architecture

### Service Locator: `CatERing`

`CatERing` is a singleton that acts as the central service locator. All business logic access goes through it:

```java
CatERing.getInstance().getMenuManager()
CatERing.getInstance().getUserManager()
CatERing.getInstance().getRecipeManager()
CatERing.getInstance().getEventManager()
```

This pattern allows the UI layer and test classes to obtain managers without direct instantiation and keeps dependencies decoupled.

### Business Logic Layer

Each subdomain is encapsulated in a dedicated Manager class. Managers enforce use-case preconditions and throw `UseCaseLogicException` on violations.

**MenuManager** — the primary deliverable of the assignment. Manages the full lifecycle of a `Menu` object:

- `createMenu(title)` — requires the current user to be a chef; creates a new `Menu`, sets it as `currentMenu`, and notifies all registered `MenuEventReceiver` instances.
- `defineSection(name)` — appends a named `Section` to `currentMenu`.
- `insertItem(recipe, section, description)` — adds a `MenuItem` backed by a `Recipe`, either to a section or as a free (unsectioned) item. Multiple overloads exist for convenience.
- `setAdditionalFeatures(names, values)` — sets boolean feature flags on the menu (e.g., "Richiede cucina", "Finger food", "Buffet", "Piatti caldi", "Richiede cuoco").
- `changeTitle(title)`, `publish()` — metadata and lifecycle transitions.
- `deleteMenu(m)`, `chooseMenu(m)` — require the user to be the owner and the menu not to be in use.
- `copyMenu(m)` — deep-copies a menu and sets the copy as `currentMenu`.
- `deleteSection(s, deleteItems)` — removes a section; if `deleteItems` is false, items are reassigned as free items.
- `changeSectionName(s, name)`, `moveSection(s, position)` — structural edits.
- `moveMenuItem(mi, section, position)` — repositions an item within a section or among free items.
- `assignItemToSection(mi, section)` — moves an item across sections or between sectioned and free status.
- `editMenuItemDescription(mi, desc)`, `deleteItem(mi)` — item-level edits.

Every mutating operation calls a corresponding `notifyXxx()` method that iterates over `eventReceivers` (a list of `MenuEventReceiver`). This implements the Observer pattern: the persistence layer registers itself as a receiver and reacts to each event by issuing the appropriate SQL statement.

**UserManager** — manages the currently authenticated user. Provides `fakeLogin(username)` for testing (no real authentication). Role checks (`user.isChef()`) are enforced by managers before any write operation.

**RecipeManager** — read-only catalog. Loads all recipes from the database on instantiation via `Recipe.loadAllRecipes()` into a static in-memory map (`Map<Integer, Recipe>`). Subsequent lookups by ID are served from cache.

**EventManager** — provides event and service information. Partially implemented; exposes `getEventInfo()` used by test classes to list events and their associated services.

**KitchenTaskManager / SummarySheet** — stubs for the kitchen planning domain (not implemented in this version).

**ShiftTable / Shift** — manages staff shift assignments. `ShiftTable` holds a list of `Shift` objects and exposes `dismiss(event)` / `dismiss(service)` to cancel shift assignments.

### Event/Observer Pattern for Persistence

`MenuEventReceiver` is a Java interface with one method per menu mutation event:

```
updateMenuCreated, updateSectionAdded, updateMenuItemAdded,
updateMenuFeaturesChanged, updateMenuTitleChanged, updateMenuPublishedState,
updateMenuDeleted, updateSectionDeleted, updateSectionChangedName,
updateSectionsRearranged, updateFreeItemsRearranged, updateSectionItemsRearranged,
updateItemSectionChanged, updateItemDescriptionChanged, updateItemDeleted
```

`MenuPersistence` implements this interface and translates each event into the appropriate SQL operation via `PersistenceManager`. The JavaFX UI components also implement `MenuEventReceiver` to refresh their views reactively.

### Persistence Layer

`PersistenceManager` wraps JDBC. It provides:
- `executeQuery(sql, ResultHandler)` — executes a SELECT and iterates rows via a `ResultHandler` callback.
- `executeUpdate(sql)` — executes INSERT/UPDATE/DELETE.
- `executeBatchUpdate(sql, count, BatchUpdateHandler)` — parameterized batch operations via `PreparedStatement`; generated IDs are fed back to the handler.
- `getLastId()` — retrieves the last auto-incremented ID.
- `escapeString(s)` — basic SQL string escaping.

Each domain entity class (`Menu`, `Section`, `MenuItem`, `Recipe`, `KitchenTask`) contains static persistence methods that call `PersistenceManager` directly. This is an Active Record-style approach.

---

## Database Schema

The MySQL database is named `catering`. Tables:

| Table | Description |
|---|---|
| `Users` | id, username |
| `Roles` | id (char: o/h/c/s), role name (organizzatore/chef/cuoco/servizio) |
| `UserRoles` | many-to-many join between Users and Roles |
| `Menus` | id, title, owner_id, published (boolean) |
| `MenuSections` | id, menu_id, name, position |
| `MenuItems` | id, menu_id, section_id (0 = free item), description, recipe_id, position |
| `MenuFeatures` | menu_id, name, value (boolean flag) |
| `Recipes` | id, name |
| `Events` | id, name, date_start, date_end, expected_participants, organizer_id |
| `Services` | id, event_id, name, proposed_menu_id, approved_menu_id, service_date, time_start, time_end, expected_participants |

Seed data includes 10 users, 4 roles, 20 recipes, 3 events, 8 services, and 3 sample menus with sections and items.

`section_id = 0` in `MenuItems` encodes a free (unsectioned) item — not a foreign key to any section row.

---

## Database Setup

### With Docker

```bash
cd database
docker-compose up -d
```

This starts a MySQL 8.0.33 instance on port 3310 with root password `4321test`, and an Adminer instance on port 8080. The MySQL native password plugin is forced via `--default-authentication-plugin=mysql_native_password`.

Then load the schema and seed data:

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p4321test < database/catering_init_03.sql
```

### Reset to Seed State

```bash
mysql -h 127.0.0.1 -P 3310 -u root -p4321test catering < database/dbreset.sql
```

`dbreset.sql` truncates `MenuItems`, `MenuSections`, `MenuFeatures`, `Menus`, and `Services`, then re-inserts the original seed rows. Static data (Users, Roles, UserRoles, Recipes, Events) is not touched.

---

## Build and Run

### Prerequisites

- JDK 8 or later
- Maven 3.x
- A running MySQL instance loaded with `catering_init_03.sql`
- Configure the JDBC connection URL in `PersistenceManager.java` to match your MySQL host, port, and credentials

### Build

```bash
mvn clean compile
```

### Run the JavaFX application

```bash
mvn javafx:run
# or
java --module-path <javafx-sdk-path>/lib --add-modules javafx.controls,javafx.fxml -cp target/classes catering.CatERingApp
```

### Run a test class

Each `TestCatERingXxx` class has a `main` method and can be run directly. These are manual integration tests, not JUnit tests. They perform a `fakeLogin` as user "Lidia" (a chef) and exercise specific use case sequences, printing the resulting menu state to stdout via `menu.testString()`.

Example:

```bash
mvn exec:java -Dexec.mainClass="catering.TestCatERing"
```

---

## Use Case Coverage

| Test Class | Use Cases Covered |
|---|---|
| `TestCatERing` | UC1a: create menu, define sections, insert items, set features, change title, publish |
| `TestCatERing1b` | UC1b: delete a menu |
| `TestCatERing1c` | UC1c: copy a menu |
| `TestCatERing2a` | UC2a: delete a section (with or without cascading item deletion) |
| `TestCatERing2b` | UC2b: rename a section |
| `TestCatERing2c` | UC2c: reorder sections |
| `TestCatERing2d` | UC2d: reorder items within a section |
| `TestCatERing2ef` | UC2e/f: move items between sections |
| `TestCatERing4a` | UC4a: assign a menu item to a different section |
| `TestCatERing4b` | UC4b: edit a menu item's description |
| `TestCatERing4c` | UC4c: delete a menu item |

---

## Known Limitations

Several classes are present as empty stubs or with minimal implementation:

- `CatERing.java`, `Menu.java`, `Section.java`, `MenuException.java` — compiled but body not committed (empty files in the repository; likely excluded from the final push).
- `KitchenTaskManager.java`, `SummarySheet.java` — not implemented.
- `EventManager.java`, `EventInfo.java`, `EventItemInfo.java`, `ServiceInfo.java` — partially implemented; only read operations are available.
- `UserManager.java`, `User.java` — authentication is simulated via `fakeLogin`; no real login flow.
- `PersistenceManager.java`, `ResultHandler.java`, `BatchUpdateHandler.java` — empty files committed; the actual implementation was provided separately or not committed.
- No JUnit tests are present; all testing is done through manual `main`-method test classes.
- The JavaFX UI (`src/main/java/catering/ui/`) was provided by course instructors and is not part of the student implementation.
