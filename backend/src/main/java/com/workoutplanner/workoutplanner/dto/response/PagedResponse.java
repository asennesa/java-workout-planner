package com.workoutplanner.workoutplanner.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 *
 * @param <T> The type of content in the page
 */
@Data
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public PagedResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = pageNumber == 0;
        this.last = pageNumber == totalPages - 1;
        this.empty = content.isEmpty();
    }
}

