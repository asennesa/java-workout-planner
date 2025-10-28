# Liquibase Migration Guide

## Overview

This project now uses **Liquibase** for database schema version control and migration management. This ensures that database changes are tracked, versioned, and applied consistently across all environments.

## What Changed

### 1. **Hibernate DDL Auto Mode**
```properties
# Before: Hibernate automatically modified the database
spring.jpa.hibernate.ddl-auto=update

# After: Hibernate validates but doesn't modify the database
spring.jpa.hibernate.ddl-auto=validate
```

### 2. **Liquibase Configuration**
All database schema changes are now managed through Liquibase changelog files located in:
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog file
└── changes/
    └── v1.0/
        ├── 001-create-users-table.yaml
        ├── 002-create-exercises-table.yaml
        ├── 003-create-workout-sessions-table.yaml
        ├── 004-create-workout-exercises-table.yaml
        └── 005-create-set-tables.yaml
```

## Directory Structure

```
src/main/resources/
└── db/
    └── changelog/
        ├── db.changelog-master.yaml        # Entry point - includes all migrations
        └── changes/
            ├── v1.0/                        # Version 1.0 migrations
            │   ├── 001-create-users-table.yaml
            │   ├── 002-create-exercises-table.yaml
            │   ├── 003-create-workout-sessions-table.yaml
            │   ├── 004-create-workout-exercises-table.yaml
            │   └── 005-create-set-tables.yaml
            ├── v1.1/                        # Future version migrations
            └── v2.0/                        # Future version migrations
```

## Current Migrations (v1.0)

### Migration 001: Users Table
Creates the `users` table with:
- Authentication fields (username, password_hash, email)
- Personal information (first_name, last_name)
- Role and security status
- Token management
- Audit fields (created_at, updated_at)
- Optimistic locking (version)

### Migration 002: Exercises Table
Creates the `exercises` table with:
- Exercise information (name, description)
- Exercise type (STRENGTH, CARDIO, FLEXIBILITY)
- Target muscle group
- Difficulty level
- Audit fields

### Migration 003: Workout Sessions Table
Creates the `workout_sessions` table with:
- Workout metadata (name, description)
- User relationship (foreign key to users)
- Status tracking (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
- Timing information (started_at, completed_at, duration)
- Session notes
- Audit fields

### Migration 004: Workout Exercises Table
Creates the `workout_exercises` table linking exercises to workout sessions:
- Relationships to both workout_sessions and exercises
- Order tracking (order_in_workout)
- Notes per exercise in the workout
- Audit fields

### Migration 005: Set Tables
Creates three set tables:
- `strength_sets` - for strength exercises (reps, weight)
- `cardio_sets` - for cardio exercises (duration, distance, distance_unit)
- `flexibility_sets` - for flexibility exercises (duration, stretch_type, intensity)

Each with:
- Common fields (set_number, rest_time, notes, completed)
- Type-specific fields
- Audit fields

## How to Use

### Running Migrations

Migrations run automatically when you start the application:

```bash
# Start the application (migrations will apply automatically)
./mvnw spring-boot:run
```

Liquibase will:
1. Check which migrations have been applied
2. Apply any new migrations in order
3. Record the applied migrations in `databasechangelog` table

### Creating New Migrations

When you need to add a new feature that requires database changes:

#### Step 1: Create a new migration file
```bash
# Create new version directory if needed
mkdir -p src/main/resources/db/changelog/changes/v1.1

# Create migration file
touch src/main/resources/db/changelog/changes/v1.1/006-add-feature-name.yaml
```

#### Step 2: Write the migration
```yaml
databaseChangeLog:
  - changeSet:
      id: 006-add-feature-name
      author: your-name
      context: dev,prod
      labels: feature-name
      comment: Description of what this migration does
      changes:
        # Your database changes here
        - addColumn:
            tableName: workout_sessions
            columns:
              - column:
                  name: new_field
                  type: VARCHAR(100)
                  
      rollback:
        - dropColumn:
            tableName: workout_sessions
            columnName: new_field
```

#### Step 3: Add to master changelog
Edit `db.changelog-master.yaml`:
```yaml
databaseChangeLog:
  # ... existing includes ...
  
  # Version 1.1 - New Feature
  - include:
      file: db/changelog/changes/v1.1/006-add-feature-name.yaml
```

#### Step 4: Update your Java entity
Update the corresponding entity class to match the new schema.

#### Step 5: Test
```bash
# Compile and run
./mvnw clean compile
./mvnw spring-boot:run
```

### Common Migration Patterns

#### Adding a Column
```yaml
- addColumn:
    tableName: users
    columns:
      - column:
          name: phone_number
          type: VARCHAR(20)
```

#### Creating an Index
```yaml
- createIndex:
    indexName: idx_users_email
    tableName: users
    columns:
      - column:
          name: email
```

#### Adding a Foreign Key
```yaml
- addForeignKeyConstraint:
    baseTableName: workout_sessions
    baseColumnNames: user_id
    referencedTableName: users
    referencedColumnNames: user_id
    constraintName: fk_workout_sessions_user
    onDelete: CASCADE
```

#### Inserting Data
```yaml
- insert:
    tableName: exercises
    columns:
      - column:
          name: name
          value: "Push-up"
      - column:
          name: type
          value: "STRENGTH"
```

#### Running SQL Directly
```yaml
- sql:
    sql: |
      UPDATE workout_sessions 
      SET status = 'COMPLETED' 
      WHERE completed_at IS NOT NULL;
```

## Configuration

### Application Properties

Current configuration in `application.properties`:

```properties
# Hibernate validates schema without modifying it
spring.jpa.hibernate.ddl-auto=validate

# Liquibase Configuration
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.default-schema=public
spring.liquibase.drop-first=false

# Baseline existing database
spring.liquibase.baseline-on-migrate=true
spring.liquibase.baseline-version=0

# Environment contexts
spring.liquibase.contexts=dev,prod

# Validate checksums
spring.liquibase.validate-on-migrate=true
```

### Environment-Specific Migrations

Use contexts to run migrations only in specific environments:

```yaml
databaseChangeLog:
  - changeSet:
      id: seed-test-data
      author: dev-team
      context: dev  # Only runs in dev environment
      changes:
        - sqlFile:
            path: db/changelog/data/test-data.sql
```

Set context in application.properties:
```properties
# Dev environment
spring.liquibase.contexts=dev

# Production environment
spring.liquibase.contexts=prod
```

## Database Tables Created by Liquibase

Liquibase automatically creates two tables to track migrations:

### `databasechangelog`
Stores all applied migrations:
- `id`: ChangeSet ID
- `author`: Author of the change
- `filename`: Migration file path
- `dateexecuted`: When it was applied
- `orderexecuted`: Order of execution
- `exectype`: Type of execution (EXECUTED, RERAN, etc.)
- `md5sum`: Checksum to detect manual changes

### `databasechangeloglock`
Prevents concurrent migrations:
- `id`: Lock ID
- `locked`: Lock status
- `lockgranted`: When lock was acquired
- `lockedby`: Who has the lock

## Best Practices

### ✅ DO:
- **Always create rollback strategies** for your migrations
- **Test migrations locally** before committing
- **Use descriptive IDs and comments** for changesets
- **Version migrations sequentially** (001, 002, 003...)
- **Group related changes** in a single changeset
- **Use contexts** for environment-specific changes
- **Commit migration files with code** that depends on them

### ❌ DON'T:
- **Never modify applied migrations** - create new ones instead
- **Don't use `drop-first=true` in production** - data loss!
- **Don't skip version numbers** - keep them sequential
- **Don't mix schema and data changes** - separate them
- **Don't use `ddl-auto=update`** with Liquibase - use `validate`

## Troubleshooting

### Migration Fails with "Checksum Validation Failed"
**Problem**: You modified an already-applied migration.

**Solution**: 
1. Revert the changes to the migration file, OR
2. Clear checksums (not recommended):
```sql
UPDATE databasechangelog SET md5sum = NULL WHERE id = 'your-changeset-id';
```

### "Table Already Exists" Error
**Problem**: Database already has tables from Hibernate's `ddl-auto=update`.

**Solution**: Liquibase is configured with `baseline-on-migrate=true` to handle this.

### Locked Database
**Problem**: Previous migration failed and left a lock.

**Solution**:
```sql
UPDATE databasechangeloglock SET locked = FALSE WHERE id = 1;
```

### Need to Rollback a Migration
```bash
# Rollback last migration
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Rollback to specific date
./mvnw liquibase:rollback -Dliquibase.rollbackDate=2025-10-01
```

## Additional Resources

- [Liquibase Official Documentation](https://docs.liquibase.com/)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Liquibase Best Practices](https://www.liquibase.org/get-started/best-practices)

## Migration Workflow Example

Let's say you want to add a "profile picture URL" to users:

### 1. Create Migration File
`src/main/resources/db/changelog/changes/v1.1/006-add-profile-picture.yaml`
```yaml
databaseChangeLog:
  - changeSet:
      id: 006-add-profile-picture-to-users
      author: asen
      context: dev,prod
      comment: Add profile_picture_url field to users table
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: profile_picture_url
                  type: VARCHAR(500)
                  remarks: 'URL to user profile picture'
                  
      rollback:
        - dropColumn:
            tableName: users
            columnName: profile_picture_url
```

### 2. Update Master Changelog
`db.changelog-master.yaml`
```yaml
databaseChangeLog:
  # ... existing includes ...
  
  # Version 1.1 - Profile Pictures
  - include:
      file: db/changelog/changes/v1.1/006-add-profile-picture.yaml
```

### 3. Update Java Entity
`User.java`
```java
@Column(name = "profile_picture_url", length = 500)
private String profilePictureUrl;
```

### 4. Test
```bash
./mvnw spring-boot:run
```

✅ Done! The migration will apply automatically on startup.

---

**Remember**: With Liquibase, your database schema is now version-controlled, auditable, and consistently deployable across all environments! 🚀

