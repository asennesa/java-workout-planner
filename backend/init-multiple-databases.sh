#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE workout_planner'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'workout_planner')\gexec
    GRANT ALL PRIVILEGES ON DATABASE workout_planner TO $POSTGRES_USER;
EOSQL
