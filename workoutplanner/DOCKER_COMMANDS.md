# Docker Commands Reference

## Essential Docker Commands for Your Setup:

### **1. Start Your Services:**
```bash
# Start SonarQube and PostgreSQL
docker-compose up -d

# Check if they're running
docker-compose ps
```

### **2. Stop Your Services (Safely):**
```bash
# Stop containers but keep data
docker-compose down

# Restart after stopping
docker-compose up -d
```

### **3. Check Status:**
```bash
# See running containers
docker-compose ps

# Check logs if something's wrong
docker-compose logs sonarqube
docker-compose logs postgres
```

### **4. Restart Services:**
```bash
# Restart without stopping first
docker-compose restart

# Restart just one service
docker-compose restart sonarqube
docker-compose restart postgres
```

## Daily Workflow:

### **Morning (Start your work):**
```bash
cd /Users/asengeorgiev/repos/java_workout_app_repo/java-workout-planner/workoutplanner
docker-compose up -d
```

### **Evening (End your work):**
```bash
docker-compose down
```

### **If something goes wrong:**
```bash
# Check what's running
docker-compose ps

# Check logs
docker-compose logs

# Restart everything
docker-compose restart
```

## Useful Commands to Remember:

```bash
# See all your containers
docker ps

# See all containers (including stopped)
docker ps -a

# Check if SonarQube is accessible
curl http://localhost:9000

# Check if PostgreSQL is accessible
docker-compose exec postgres psql -U sonar -d sonar
```

## Pro Tips:

1. **Always run commands from the `workoutplanner` directory** (where your `docker-compose.yml` is)
2. **Use `-d` flag** to run in background (detached mode)
3. **Your data is safe** with regular `down`/`up` commands
4. **Check `docker-compose ps`** to see if services are running

## Data Persistence:

### **Safe Commands (Data Persists):**
```bash
docker-compose down          # Stops containers, keeps data
docker-compose up -d         # Restarts with existing data
docker-compose restart       # Restarts containers, keeps data
```

### **Dangerous Commands (Data Lost):**
```bash
docker-compose down -v      # Stops containers AND deletes volumes
docker-compose down --volumes  # Same as above
```

**That's it! These are the main commands you'll need for daily Docker management.** 🐳
