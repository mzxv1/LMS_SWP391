package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.dto.CourseDTO;
import com.lms.dto.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseDAO courseDAO;

    @InjectMocks
    private CourseService courseService;

    @Test
    @DisplayName("Course Pagination: Valid parameters return wrapped Page<CourseDTO>")
    void testGetCoursesPage_ValidParameters() throws SQLException {
        CourseDTO c1 = new CourseDTO();
        c1.setId(1);
        c1.setTitle("Java Core");
        c1.setPrice(new BigDecimal("500000"));
        c1.setStatus("PUBLISHED");

        CourseDTO c2 = new CourseDTO();
        c2.setId(2);
        c2.setTitle("Python Pro");
        c2.setPrice(new BigDecimal("450000"));
        c2.setStatus("DRAFT");

        when(courseDAO.countSearchAll(eq("Java"), eq("PUBLISHED"))).thenReturn(25);
        when(courseDAO.searchAll(eq("Java"), eq("PUBLISHED"), eq(1), eq(10), eq("id"), eq("DESC")))
                .thenReturn(Arrays.asList(c1, c2));

        Page<CourseDTO> page = courseService.getCoursesPage("Java", "PUBLISHED", 1, 10, "id", "DESC");

        assertNotNull(page);
        assertEquals(1, page.getPage());
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getSize());
        assertEquals(10, page.getPageSize());
        assertEquals(25, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
        assertEquals(2, page.getContent().size());

        verify(courseDAO, times(1)).countSearchAll("Java", "PUBLISHED");
        verify(courseDAO, times(1)).searchAll("Java", "PUBLISHED", 1, 10, "id", "DESC");
    }

    @Test
    @DisplayName("Course Pagination: Normalizes negative page and non-positive pageSize defensively")
    void testGetCoursesPage_DefensiveBounds() throws SQLException {
        when(courseDAO.countSearchAll(isNull(), isNull())).thenReturn(5);
        when(courseDAO.searchAll(isNull(), isNull(), eq(1), eq(10), eq("title"), eq("ASC")))
                .thenReturn(Collections.emptyList());

        Page<CourseDTO> page = courseService.getCoursesPage(null, null, -5, 0, "title", "ASC");

        assertNotNull(page);
        assertEquals(1, page.getPage());
        assertEquals(10, page.getSize());
        assertEquals(5, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
    }
}
