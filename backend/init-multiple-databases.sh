#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE workout_planner;
    GRANT ALL PRIVILEGES ON DATABASE workout_planner TO $POSTGRES_USER;
EOSQL
