package com.lms.service;

import com.lms.dao.UserDAO;
import com.lms.dto.Page;
import com.lms.dto.UserDTO;
import com.lms.entity.Role;
import com.lms.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("User Pagination: Valid parameters return wrapped Page<UserDTO> with password stripped")
    void testGetUsersPage_ValidParameters() throws SQLException {
        User u1 = new User();
        u1.setId(1);
        u1.setUsername("admin1");
        u1.setPasswordHash("$2a$12$secretHashMustNotLeak");
        u1.setFullName("Nguyen Admin");
        u1.setEmail("admin@lms.vn");
        u1.setRole(Role.ADMIN);
        u1.setActive(true);

        User u2 = new User();
        u2.setId(2);
        u2.setUsername("student1");
        u2.setPasswordHash("$2a$12$anotherSecretHash");
        u2.setFullName("Le Hoc Vien");
        u2.setEmail("student@lms.vn");
        u2.setRole(Role.STUDENT);
        u2.setActive(true);

        when(userDAO.countSearch(eq("admin"), eq("ADMIN"), eq("Active"))).thenReturn(15);
        when(userDAO.search(eq("admin"), eq("ADMIN"), eq("Active"), eq(1), eq(10), eq("id"), eq("ASC")))
                .thenReturn(Arrays.asList(u1, u2));

        Page<UserDTO> page = userService.getUsersPage("admin", "ADMIN", "Active", 1, 10, "id", "ASC");

        assertNotNull(page);
        assertEquals(1, page.getPage());
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getSize());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(2, page.getContent().size());

        // Verify password hash is not exposed
        UserDTO dto1 = page.getContent().get(0);
        assertEquals("admin1", dto1.getUsername());
        assertEquals("Nguyen Admin", dto1.getFullName());
        assertEquals("ADMIN", dto1.getRole());

        verify(userDAO, times(1)).countSearch("admin", "ADMIN", "Active");
        verify(userDAO, times(1)).search("admin", "ADMIN", "Active", 1, 10, "id", "ASC");
    }

    @Test
    @DisplayName("User Pagination: Normalizes negative page and pageSize defensively")
    void testGetUsersPage_DefensiveBounds() throws SQLException {
        when(userDAO.countSearch(isNull(), isNull(), isNull())).thenReturn(8);
        when(userDAO.search(isNull(), isNull(), isNull(), eq(1), eq(10), eq("id"), eq("ASC")))
                .thenReturn(Collections.emptyList());

        Page<UserDTO> page = userService.getUsersPage(null, null, null, -2, -10, "id", "ASC");

        assertNotNull(page);
        assertEquals(1, page.getPage());
        assertEquals(10, page.getSize());
        assertEquals(8, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
    }

    @Test
    @DisplayName("Self-Protection: Admin cannot deactivate their own logged-in account")
    void testToggleUserStatus_SelfDeactivation_ThrowsServiceException() throws SQLException {
        User selfAdmin = new User();
        selfAdmin.setId(1);
        selfAdmin.setActive(true);

        when(userDAO.findById(1)).thenReturn(selfAdmin);

        ServiceException ex = assertThrows(ServiceException.class, () -> {
            userService.toggleUserStatus(1, 1);
        });

        assertTrue(ex.getMessage().contains("không thể vô hiệu hóa tài khoản quản trị đang đăng nhập của chính mình"));
        verify(userDAO, never()).updateStatus(anyInt(), anyBoolean());
    }
}
