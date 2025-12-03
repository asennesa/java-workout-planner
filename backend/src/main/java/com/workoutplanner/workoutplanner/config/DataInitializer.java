package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the database with default exercises on application startup.
 * Only loads data if the exercises table is empty.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final ExerciseRepository exerciseRepository;

    public DataInitializer(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (exerciseRepository.count() > 0) {
            logger.info("Exercises already exist in database, skipping initialization");
            return;
        }

        logger.info("Initializing database with default exercises...");
        List<Exercise> exercises = createDefaultExercises();
        exerciseRepository.saveAll(exercises);
        logger.info("Successfully loaded {} default exercises", exercises.size());
    }

    private List<Exercise> createDefaultExercises() {
        List<Exercise> exercises = new ArrayList<>();

        // === STRENGTH EXERCISES ===

        // Chest
        exercises.add(createExercise("Bench Press", "Classic chest exercise using a barbell on a flat bench",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Push-ups", "Bodyweight exercise targeting chest, shoulders, and triceps",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Incline Dumbbell Press", "Upper chest focused press on an incline bench",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Cable Flyes", "Isolation exercise for chest using cable machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));

        // Back
        exercises.add(createExercise("Deadlift", "Compound exercise for posterior chain development",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Pull-ups", "Bodyweight exercise for lats and upper back",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Barbell Row", "Bent over rowing movement for back thickness",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Lat Pulldown", "Cable exercise targeting the latissimus dorsi",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));

        // Legs
        exercises.add(createExercise("Barbell Squat", "King of leg exercises for overall lower body development",
                ExerciseType.STRENGTH, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Leg Press", "Machine-based compound leg exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Romanian Deadlift", "Hip hinge movement targeting hamstrings and glutes",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Leg Extension", "Isolation exercise for quadriceps",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Leg Curl", "Isolation exercise for hamstrings",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Calf Raises", "Isolation exercise for calf muscles",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lunges", "Unilateral leg exercise for balance and strength",
                ExerciseType.STRENGTH, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));

        // Shoulders
        exercises.add(createExercise("Overhead Press", "Compound pressing movement for shoulder development",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Lateral Raises", "Isolation exercise for lateral deltoids",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Face Pulls", "Rear delt and rotator cuff exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));

        // Arms
        exercises.add(createExercise("Barbell Curl", "Classic bicep exercise using a barbell",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hammer Curls", "Bicep and brachialis exercise with neutral grip",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Tricep Pushdown", "Cable exercise for tricep isolation",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Skull Crushers", "Lying tricep extension exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dips", "Compound exercise for triceps and chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));

        // Core
        exercises.add(createExercise("Plank", "Isometric core stability exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Crunches", "Basic abdominal exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hanging Leg Raises", "Advanced core exercise using a pull-up bar",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Russian Twists", "Rotational core exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));

        // Glutes
        exercises.add(createExercise("Hip Thrust", "Primary glute building exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Glute Bridge", "Bodyweight glute activation exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));

        // === CARDIO EXERCISES ===

        exercises.add(createExercise("Running", "Outdoor or treadmill running for cardiovascular fitness",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cycling", "Stationary or outdoor cycling",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Rowing Machine", "Full body cardio workout",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Jump Rope", "High intensity cardio exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Swimming", "Low impact full body cardio",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Stair Climber", "Machine-based cardio targeting legs",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Elliptical", "Low impact cardio machine",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Burpees", "High intensity full body cardio exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Mountain Climbers", "Core and cardio combination exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Jumping Jacks", "Classic cardio warm-up exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));

        // === FLEXIBILITY EXERCISES ===

        exercises.add(createExercise("Hamstring Stretch", "Static stretch for hamstring flexibility",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Quad Stretch", "Standing or lying quadriceps stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hip Flexor Stretch", "Lunge position stretch for hip flexors",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Shoulder Stretch", "Cross-body shoulder stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Chest Stretch", "Doorway or wall chest stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cat-Cow Stretch", "Spinal mobility exercise",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Child's Pose", "Restorative yoga pose for back and hips",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Pigeon Pose", "Deep hip opener stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Downward Dog", "Full body yoga stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Rolling - IT Band", "Self-myofascial release for IT band",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));

        return exercises;
    }

    private Exercise createExercise(String name, String description, ExerciseType type,
                                    TargetMuscleGroup targetMuscleGroup, DifficultyLevel difficultyLevel) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setDescription(description);
        exercise.setType(type);
        exercise.setTargetMuscleGroup(targetMuscleGroup);
        exercise.setDifficultyLevel(difficultyLevel);
        return exercise;
    }
}
