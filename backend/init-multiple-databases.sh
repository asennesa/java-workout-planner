#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create workout_planner database if not exists
    SELECT 'CREATE DATABASE workout_planner'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'workout_planner')\gexec
    GRANT ALL PRIVILEGES ON DATABASE workout_planner TO $POSTGRES_USER;

    -- Create sonarqube database if not exists
    SELECT 'CREATE DATABASE sonarqube'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sonarqube')\gexec
    GRANT ALL PRIVILEGES ON DATABASE sonarqube TO $POSTGRES_USER;
EOSQL
