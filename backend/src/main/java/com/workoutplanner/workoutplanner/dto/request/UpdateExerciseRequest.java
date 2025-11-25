package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for updating exercise information.
 * 
 * This DTO allows partial updates of exercise data. Only provided fields
 * will be updated, allowing for flexible exercise modifications.
 * 
 * All fields are optional - only the fields that need to be changed
 * should be provided in the request.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExerciseRequest {

    @Length(min = 2, max = 100, message = "Exercise name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Exercise name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ExerciseType type;

    private TargetMuscleGroup targetMuscleGroup;

    private DifficultyLevel difficultyLevel;

    @Length(max = 500, message = "Image URL must not exceed 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    /**
     * Check if any field is being updated.
     * 
     * @return true if at least one field is provided, false otherwise
     */
    public boolean hasUpdates() {
        return name != null || 
               description != null || 
               type != null || 
               targetMuscleGroup != null || 
               difficultyLevel != null || 
               imageUrl != null;
    }

    /**
     * Check if name is being updated.
     * 
     * @return true if name is not null and not blank, false otherwise
     */
    public boolean isNameUpdateRequested() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Check if description is being updated.
     * 
     * @return true if description is not null and not blank, false otherwise
     */
    public boolean isDescriptionUpdateRequested() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Check if image URL is being updated.
     * 
     * @return true if imageUrl is not null and not blank, false otherwise
     */
    public boolean isImageUrlUpdateRequested() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "UpdateExerciseRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", targetMuscleGroup=" + targetMuscleGroup +
                ", difficultyLevel=" + difficultyLevel +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
