package com.workoutplanner.workoutplanner.exception;

/**
 * Exception thrown when an optimistic lock conflict occurs (concurrent modification).
 */
public class OptimisticLockConflictException extends BusinessLogicException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }
}
