package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified response DTO for all types of sets (Cardio, Strength, Flexibility).
 * 
 * This DTO consolidates the three separate set response DTOs into a single,
 * flexible response object that includes all possible fields for all set types.
 * 
 * Common Fields (all set types):
 * - setId, workoutExerciseId, setNumber, restTimeInSeconds, notes, completed
 * 
 * Type-Specific Fields:
 * - Cardio: durationInSeconds, distance, distanceUnit
 * - Strength: reps, weight
 * - Flexibility: durationInSeconds, stretchType, intensity
 * 
 * Usage:
 * - All fields are optional in the response
 * - Type-specific fields will be null for other set types
 * - Use helper methods to determine set type and access relevant fields
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetResponse {

    // Common fields for all set types
    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    // Cardio-specific fields
    private Integer durationInSeconds;
    private BigDecimal distance;
    private String distanceUnit;

    // Strength-specific fields
    private Integer reps;
    private BigDecimal weight;

    // Flexibility-specific fields
    private String stretchType;
    private Integer intensity;

    // Helper methods for set type detection
    /**
     * Check if this is a cardio set.
     * Cardio sets are identified by the presence of distance or distanceUnit fields.
     * 
     * @return true if this is a cardio set, false otherwise
     */
    public boolean isCardioSet() {
        return distance != null || distanceUnit != null;
    }

    /**
     * Check if this is a strength set.
     * Strength sets are identified by the presence of both reps and weight fields.
     * 
     * @return true if this is a strength set, false otherwise
     */
    public boolean isStrengthSet() {
        return reps != null && weight != null;
    }

    /**
     * Check if this is a flexibility set.
     * Flexibility sets are identified by the presence of stretchType or intensity fields.
     * 
     * @return true if this is a flexibility set, false otherwise
     */
    public boolean isFlexibilitySet() {
        return stretchType != null || intensity != null;
    }

    /**
     * Get the detected set type based on provided fields.
     * 
     * @return the set type as a string ("CARDIO", "STRENGTH", "FLEXIBILITY", or "UNKNOWN")
     */
    public String getSetType() {
        if (isCardioSet()) return "CARDIO";
        if (isStrengthSet()) return "STRENGTH";
        if (isFlexibilitySet()) return "FLEXIBILITY";
        return "UNKNOWN";
    }

    /**
     * Get a summary of the set for logging/debugging.
     * 
     * @return a string summary of the set
     */
    public String getSetSummary() {
        return String.format("SetResponse{type=%s, setId=%d, exerciseId=%d, setNumber=%d, completed=%s}", 
                           getSetType(), setId, workoutExerciseId, setNumber, completed);
    }

    /**
     * Create a cardio set response.
     * 
     * @param setId the set ID
     * @param workoutExerciseId the workout exercise ID
     * @param setNumber the set number
     * @param durationInSeconds the duration in seconds
     * @param distance the distance
     * @param distanceUnit the distance unit
     * @param restTimeInSeconds the rest time in seconds
     * @param notes the notes
     * @param completed whether the set is completed
     * @return a SetResponse configured for cardio
     */
    public static SetResponse createCardioSet(Long setId, Long workoutExerciseId, Integer setNumber,
                                            Integer durationInSeconds, BigDecimal distance, String distanceUnit,
                                            Integer restTimeInSeconds, String notes, Boolean completed) {
        SetResponse response = new SetResponse();
        response.setSetId(setId);
        response.setWorkoutExerciseId(workoutExerciseId);
        response.setSetNumber(setNumber);
        response.setDurationInSeconds(durationInSeconds);
        response.setDistance(distance);
        response.setDistanceUnit(distanceUnit);
        response.setRestTimeInSeconds(restTimeInSeconds);
        response.setNotes(notes);
        response.setCompleted(completed);
        return response;
    }

    /**
     * Create a strength set response.
     * 
     * @param setId the set ID
     * @param workoutExerciseId the workout exercise ID
     * @param setNumber the set number
     * @param reps the number of reps
     * @param weight the weight
     * @param restTimeInSeconds the rest time in seconds
     * @param notes the notes
     * @param completed whether the set is completed
     * @return a SetResponse configured for strength
     */
    public static SetResponse createStrengthSet(Long setId, Long workoutExerciseId, Integer setNumber,
                                              Integer reps, BigDecimal weight, Integer restTimeInSeconds,
                                              String notes, Boolean completed) {
        SetResponse response = new SetResponse();
        response.setSetId(setId);
        response.setWorkoutExerciseId(workoutExerciseId);
        response.setSetNumber(setNumber);
        response.setReps(reps);
        response.setWeight(weight);
        response.setRestTimeInSeconds(restTimeInSeconds);
        response.setNotes(notes);
        response.setCompleted(completed);
        return response;
    }

    /**
     * Create a flexibility set response.
     * 
     * @param setId the set ID
     * @param workoutExerciseId the workout exercise ID
     * @param setNumber the set number
     * @param durationInSeconds the duration in seconds
     * @param stretchType the stretch type
     * @param intensity the intensity level
     * @param restTimeInSeconds the rest time in seconds
     * @param notes the notes
     * @param completed whether the set is completed
     * @return a SetResponse configured for flexibility
     */
    public static SetResponse createFlexibilitySet(Long setId, Long workoutExerciseId, Integer setNumber,
                                                  Integer durationInSeconds, String stretchType, Integer intensity,
                                                  Integer restTimeInSeconds, String notes, Boolean completed) {
        SetResponse response = new SetResponse();
        response.setSetId(setId);
        response.setWorkoutExerciseId(workoutExerciseId);
        response.setSetNumber(setNumber);
        response.setDurationInSeconds(durationInSeconds);
        response.setStretchType(stretchType);
        response.setIntensity(intensity);
        response.setRestTimeInSeconds(restTimeInSeconds);
        response.setNotes(notes);
        response.setCompleted(completed);
        return response;
    }
}
