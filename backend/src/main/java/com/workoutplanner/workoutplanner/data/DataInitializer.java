package com.workoutplanner.workoutplanner.data;

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

        // =====================================================
        // STRENGTH EXERCISES
        // =====================================================

        // --- CHEST ---
        exercises.add(createExercise("Barbell Bench Press", "Classic flat bench press with barbell for overall chest development",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dumbbell Bench Press", "Flat bench press with dumbbells for greater range of motion",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Incline Barbell Press", "Upper chest focused press on incline bench with barbell",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Incline Dumbbell Press", "Upper chest focused press on incline bench with dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Decline Bench Press", "Lower chest emphasis pressing movement",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Push-ups", "Bodyweight exercise targeting chest, shoulders, and triceps",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Diamond Push-ups", "Close-grip push-up variation emphasizing triceps and inner chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Wide Push-ups", "Wide-grip push-up for outer chest emphasis",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Decline Push-ups", "Feet elevated push-up targeting upper chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Cable Flyes", "Isolation exercise for chest using cable machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Dumbbell Flyes", "Isolation exercise for chest stretch and contraction",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Incline Dumbbell Flyes", "Upper chest isolation with dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Machine Chest Press", "Machine-based pressing for chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Pec Deck Machine", "Isolation machine for chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Chest Dips", "Dip variation with forward lean for chest emphasis",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Landmine Press", "Angled pressing movement for upper chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));

        // --- BACK ---
        exercises.add(createExercise("Conventional Deadlift", "Classic deadlift for overall posterior chain",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Sumo Deadlift", "Wide stance deadlift variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Trap Bar Deadlift", "Deadlift using hex/trap bar for neutral grip",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Pull-ups", "Overhand grip bodyweight pull for lats",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Chin-ups", "Underhand grip pull-up emphasizing biceps and lower lats",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Neutral Grip Pull-ups", "Parallel grip pull-up for balanced lat development",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Wide Grip Pull-ups", "Wide overhand grip for lat width",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Assisted Pull-ups", "Machine-assisted pull-ups for beginners",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Weighted Pull-ups", "Pull-ups with added weight for advanced training",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Barbell Row", "Bent over row with barbell for back thickness",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Pendlay Row", "Strict barbell row from floor each rep",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dumbbell Row", "Single arm rowing for lat development",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Kroc Row", "Heavy high-rep single arm dumbbell row",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("T-Bar Row", "Rowing movement using T-bar or landmine setup",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Seated Cable Row", "Cable rowing for mid-back thickness",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lat Pulldown", "Cable pulldown for lat width",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Close Grip Lat Pulldown", "Narrow grip pulldown for lower lat emphasis",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Straight Arm Pulldown", "Isolation movement for lats",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Machine Row", "Chest-supported rowing machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Inverted Row", "Bodyweight row using bar or TRX",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Meadows Row", "Single arm landmine row variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Rack Pulls", "Partial deadlift from rack for upper back",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Good Mornings", "Hip hinge with barbell on back",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Back Extension", "Hyperextension for lower back",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Reverse Hyperextension", "Glute and lower back exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));

        // --- SHOULDERS ---
        exercises.add(createExercise("Barbell Overhead Press", "Standing barbell press for shoulder strength",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Seated Dumbbell Press", "Seated pressing with dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Arnold Press", "Rotating dumbbell press named after Arnold Schwarzenegger",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Push Press", "Explosive overhead press with leg drive",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Military Press", "Strict standing barbell overhead press",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Machine Shoulder Press", "Machine-based overhead pressing",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lateral Raises", "Dumbbell raises for lateral deltoids",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cable Lateral Raises", "Constant tension lateral raise with cable",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Front Raises", "Anterior deltoid isolation exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Rear Delt Flyes", "Bent over flyes for posterior deltoids",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Face Pulls", "Cable exercise for rear delts and rotator cuff",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Upright Row", "Pulling movement for traps and lateral delts",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Shrugs", "Trap isolation with barbell or dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Dumbbell Shrugs", "Trap shrugs with dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lu Raises", "Front raise to lateral raise combo movement",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Handstand Push-ups", "Bodyweight vertical pressing against wall",
                ExerciseType.STRENGTH, TargetMuscleGroup.SHOULDERS, DifficultyLevel.ADVANCED));

        // --- BICEPS ---
        exercises.add(createExercise("Barbell Curl", "Classic bicep curl with barbell",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("EZ Bar Curl", "Bicep curl with cambered bar for wrist comfort",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Dumbbell Curl", "Alternating or simultaneous dumbbell curls",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hammer Curls", "Neutral grip curls for biceps and brachialis",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Incline Dumbbell Curl", "Curls on incline bench for stretch",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Preacher Curl", "Curls on preacher bench for isolation",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Concentration Curl", "Seated single arm curl for peak contraction",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cable Curl", "Bicep curl using cable machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Spider Curl", "Chest-supported curl for constant tension",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Reverse Curl", "Overhand grip curl for brachioradialis",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Zottman Curl", "Curl up, rotate, lower with pronated grip",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("21s Bicep Curl", "7 bottom half, 7 top half, 7 full reps",
                ExerciseType.STRENGTH, TargetMuscleGroup.BICEPS, DifficultyLevel.INTERMEDIATE));

        // --- TRICEPS ---
        exercises.add(createExercise("Tricep Pushdown", "Cable pushdown with straight or rope attachment",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Rope Pushdown", "Tricep pushdown with rope for peak contraction",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Skull Crushers", "Lying tricep extension with barbell or EZ bar",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dumbbell Skull Crushers", "Lying tricep extension with dumbbells",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Overhead Tricep Extension", "Cable or dumbbell extension overhead",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Dumbbell Kickback", "Bent over tricep extension",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Close Grip Bench Press", "Narrow grip bench for tricep emphasis",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dips", "Bodyweight dip for triceps and chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Bench Dips", "Dips using bench for support",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Weighted Dips", "Dips with added weight",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("JM Press", "Hybrid skull crusher and close grip press",
                ExerciseType.STRENGTH, TargetMuscleGroup.TRICEPS, DifficultyLevel.ADVANCED));

        // --- LEGS (QUADRICEPS) ---
        exercises.add(createExercise("Barbell Back Squat", "Classic back squat for overall leg development",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Front Squat", "Barbell squat with front rack position",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Goblet Squat", "Squat holding dumbbell or kettlebell at chest",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hack Squat", "Machine squat variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Leg Press", "Machine pressing for legs",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Leg Extension", "Quad isolation on machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Walking Lunges", "Forward stepping lunges",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Reverse Lunges", "Backward stepping lunges",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Bulgarian Split Squat", "Rear foot elevated single leg squat",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Step-ups", "Stepping up onto box or bench",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Sissy Squat", "Bodyweight quad isolation movement",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Pistol Squat", "Single leg bodyweight squat",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Box Squat", "Squat to box for power development",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Pause Squat", "Squat with pause at bottom",
                ExerciseType.STRENGTH, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.ADVANCED));

        // --- LEGS (HAMSTRINGS) ---
        exercises.add(createExercise("Romanian Deadlift", "Hip hinge for hamstrings and glutes",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Stiff Leg Deadlift", "Straight leg deadlift variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Single Leg Romanian Deadlift", "Unilateral RDL for balance and strength",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Lying Leg Curl", "Prone hamstring curl machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Seated Leg Curl", "Seated hamstring curl machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Nordic Curl", "Bodyweight eccentric hamstring exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Glute Ham Raise", "GHD exercise for posterior chain",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Cable Pull Through", "Hip hinge with cable",
                ExerciseType.STRENGTH, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));

        // --- GLUTES ---
        exercises.add(createExercise("Barbell Hip Thrust", "Primary glute builder with barbell",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Single Leg Hip Thrust", "Unilateral hip thrust",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Glute Bridge", "Bodyweight bridge for glute activation",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cable Kickback", "Standing glute kickback with cable",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Frog Pumps", "Feet together glute bridge variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Sumo Squat", "Wide stance squat for glutes and adductors",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Donkey Kicks", "Quadruped glute exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Fire Hydrants", "Quadruped hip abduction",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Clamshells", "Side lying hip external rotation",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Banded Walks", "Lateral walks with resistance band",
                ExerciseType.STRENGTH, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));

        // --- CALVES ---
        exercises.add(createExercise("Standing Calf Raise", "Calf raise on machine or with barbell",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Seated Calf Raise", "Seated calf raise for soleus",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Donkey Calf Raise", "Bent over calf raise variation",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Single Leg Calf Raise", "Unilateral calf work",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Leg Press Calf Raise", "Calf raises on leg press machine",
                ExerciseType.STRENGTH, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));

        // --- CORE ---
        exercises.add(createExercise("Plank", "Isometric core stability hold",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Side Plank", "Lateral core stability exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Crunches", "Basic abdominal flexion exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Bicycle Crunches", "Alternating elbow to knee crunch",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Reverse Crunches", "Lower ab focused crunch",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hanging Leg Raises", "Hanging ab exercise for lower abs",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Hanging Knee Raises", "Easier variation of hanging leg raise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Captain's Chair Leg Raise", "Leg raises using dip station",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Russian Twists", "Rotational core exercise with or without weight",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Cable Woodchop", "Rotational movement with cable",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Ab Wheel Rollout", "Anti-extension core exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Dead Bug", "Anti-extension core stability",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Bird Dog", "Contralateral limb extension for stability",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Pallof Press", "Anti-rotation core exercise with cable or band",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Toes to Bar", "Hanging exercise bringing toes to bar",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("L-Sit", "Isometric hold with legs extended",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Dragon Flag", "Advanced lying core exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Suitcase Carry", "Unilateral loaded carry for obliques",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Farmer's Walk", "Loaded carry for grip and core",
                ExerciseType.STRENGTH, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));

        // --- FOREARMS/GRIP ---
        exercises.add(createExercise("Wrist Curl", "Forearm flexor exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.FOREARMS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Reverse Wrist Curl", "Forearm extensor exercise",
                ExerciseType.STRENGTH, TargetMuscleGroup.FOREARMS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Plate Pinch", "Grip strength exercise with plates",
                ExerciseType.STRENGTH, TargetMuscleGroup.FOREARMS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Dead Hang", "Passive hang for grip endurance",
                ExerciseType.STRENGTH, TargetMuscleGroup.FOREARMS, DifficultyLevel.BEGINNER));

        // =====================================================
        // CARDIO EXERCISES
        // =====================================================

        exercises.add(createExercise("Treadmill Running", "Indoor running on treadmill",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Outdoor Running", "Road or trail running outdoors",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Sprints", "High intensity short distance running",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Interval Running", "Alternating fast and slow running",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Hill Sprints", "Sprint intervals on incline",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Stationary Bike", "Indoor cycling on stationary bike",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Outdoor Cycling", "Road or mountain biking",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Spin Class", "High intensity group cycling class",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Rowing Machine", "Full body cardio on ergometer",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Assault Bike", "Air resistance bike for high intensity",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Elliptical", "Low impact cardio machine",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Stair Climber", "Stair stepping machine",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Swimming", "Pool swimming for cardio",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Jump Rope", "Skipping rope for cardio",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Double Unders", "Two rope rotations per jump",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Box Jumps", "Explosive jump onto elevated surface",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Burpees", "Full body high intensity exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Mountain Climbers", "Running plank position exercise",
                ExerciseType.CARDIO, TargetMuscleGroup.CORE, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Jumping Jacks", "Classic cardio warm-up movement",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("High Knees", "Running in place with high knee lift",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Butt Kicks", "Running in place kicking heels to glutes",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Squat Jumps", "Explosive jump from squat position",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Lunge Jumps", "Alternating jumping lunges",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Ski Erg", "Upper body focused cardio machine",
                ExerciseType.CARDIO, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Battle Ropes", "Rope wave patterns for conditioning",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Kettlebell Swings", "Hip hinge swing for power endurance",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Sled Push", "Pushing weighted sled",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Sled Pull", "Pulling weighted sled",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Tire Flips", "Flipping large tire for conditioning",
                ExerciseType.CARDIO, TargetMuscleGroup.FULL_BODY, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Walking", "Low intensity steady state walking",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Incline Walking", "Walking on incline treadmill",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hiking", "Outdoor trail walking with elevation",
                ExerciseType.CARDIO, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));

        // =====================================================
        // FLEXIBILITY/MOBILITY EXERCISES
        // =====================================================

        // Stretches
        exercises.add(createExercise("Standing Hamstring Stretch", "Standing forward fold for hamstrings",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Seated Hamstring Stretch", "Seated forward fold stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Standing Quad Stretch", "Standing single leg quad stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lying Quad Stretch", "Side lying quad stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Hip Flexor Stretch", "Kneeling lunge stretch for hip flexors",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Couch Stretch", "Intense hip flexor and quad stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Pigeon Pose", "Deep hip external rotation stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("90/90 Stretch", "Hip internal and external rotation stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Figure Four Stretch", "Supine glute and piriformis stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Butterfly Stretch", "Seated groin stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Frog Stretch", "Deep groin and hip opener",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Cross Body Shoulder Stretch", "Rear delt and shoulder stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Overhead Tricep Stretch", "Tricep and lat stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.TRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Doorway Chest Stretch", "Pec stretch using doorframe",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Wall Chest Stretch", "Single arm chest stretch against wall",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lat Stretch", "Side bend stretch for lats",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cat-Cow Stretch", "Spinal flexion and extension flow",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Child's Pose", "Restorative kneeling stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Thread the Needle", "Thoracic rotation stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Scorpion Stretch", "Prone spinal rotation stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Supine Spinal Twist", "Lying spinal rotation stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Neck Stretches", "Various neck stretches all directions",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.SHOULDERS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Wrist Circles", "Wrist mobility rotations",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.FOREARMS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Ankle Circles", "Ankle mobility rotations",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));

        // Yoga Poses
        exercises.add(createExercise("Downward Dog", "Inverted V yoga pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Upward Dog", "Back extension yoga pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Cobra Pose", "Gentle back extension",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Warrior I", "Lunge position yoga pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Warrior II", "Wide stance warrior pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Triangle Pose", "Lateral stretch yoga pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Tree Pose", "Single leg balance pose",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Bridge Pose", "Supine back bend",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Wheel Pose", "Full back bend",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        exercises.add(createExercise("Happy Baby", "Supine hip opener",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Seated Forward Fold", "Seated hamstring stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Standing Forward Fold", "Standing hamstring stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lizard Pose", "Deep hip flexor stretch",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Half Splits", "Hamstring stretch in lunge position",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.INTERMEDIATE));
        exercises.add(createExercise("Full Splits", "Complete leg split position",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.ADVANCED));

        // Foam Rolling / Self Myofascial Release
        exercises.add(createExercise("Foam Roll Quads", "Quad foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.QUADRICEPS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll IT Band", "IT band foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.LEGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll Hamstrings", "Hamstring foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.HAMSTRINGS, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll Glutes", "Glute foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll Calves", "Calf foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CALVES, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll Lats", "Lat foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Foam Roll Upper Back", "Thoracic spine foam rolling",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.BACK, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lacrosse Ball Pec Release", "Chest myofascial release",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exercises.add(createExercise("Lacrosse Ball Glute Release", "Deep glute tissue release",
                ExerciseType.FLEXIBILITY, TargetMuscleGroup.GLUTES, DifficultyLevel.BEGINNER));

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
