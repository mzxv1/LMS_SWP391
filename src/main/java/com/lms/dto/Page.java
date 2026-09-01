package com.lms.dto;

import java.util.List;

/**
 * [DTO: Generic Pagination] Generic container encapsulating paginated items and page navigation metadata.
 * [Calculations] Computes totalPages = ceil(totalElements / size), hasPrevious (page > 1), and hasNext.
 * [Usage] Wraps query results from SettingService.getSettingsPage(), UserService.getUsersPage(), and CourseService.getCoursesPage().
 */
public class Page<T> {

    private List<T> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;

    public Page() {
    }

    public Page(
            List<T> content,
            int page,
            int size,
            int totalElements) {

        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;

        this.totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil(
                (double) totalElements / size
        );
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageNumber() {
        return page;
    }

    public void setPageNumber(int pageNumber) {
        this.page = pageNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPageSize() {
        return size;
    }

    public void setPageSize(int pageSize) {
        this.size = pageSize;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}