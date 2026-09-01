package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.dao.UserDAO;
import com.lms.dto.CourseDTO;
import com.lms.dto.CourseDetailDTO;
import com.lms.dto.CourseFormDTO;
import com.lms.dto.Page;
import com.lms.entity.Course;
import com.lms.entity.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic layer for Course Management.
 *
 * Responsibilities:
 * - Public course list
 * - Public course detail
 * - Expert course management
 * - Admin course management
 */
public class CourseService {

    private static final List<String> VALID_STATUSES =
            List.of("DRAFT", "PUBLISHED", "ARCHIVED");

    private final CourseDAO courseDAO;
    private final UserDAO userDAO;

    public CourseService() {
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
    }

    public CourseService(CourseDAO courseDAO, UserDAO userDAO) {
        this.courseDAO = courseDAO;
        this.userDAO = userDAO;
    }

    public CourseService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
        this.userDAO = new UserDAO();
    }


    // ============================================================
    // PUBLIC COURSE (Student/Guest-facing)
    // ============================================================

    /**
     * Public course catalog for the Home page.
     * No authentication required.
     */
    public List<CourseDTO> listAllPublishedCourses()
            throws SQLException {

        return toDTOList(
                courseDAO.findAllPublished()
        );
    }

    /**
     * Top N published courses for the Home page, ranked by enrollment count
     * (most enrolled first). Used to show only the most popular courses.
     */
    public List<CourseDTO> listTopPublishedCoursesByEnrollment(int limit)
            throws SQLException {

        return courseDAO.findTopPublishedByEnrollment(limit);
    }


    /**
     * Public course list with:
     *
     * - Keyword search
     * - Category filter
     * - Sorting
     * - Pagination
     *
     * Accessible to anonymous users.
     */
    public Page<CourseDTO> getPublicCourses(
            String keyword,
            String category,
            String sort,
            int page,
            int size) throws SQLException {

        // --------------------------------------------------------
        // Normalize pagination
        // --------------------------------------------------------

        if (page < 1) {
            page = 1;
        }

        if (size <= 0) {
            size = 6;
        }


        // --------------------------------------------------------
        // Count total matching courses
        // --------------------------------------------------------

        int totalElements =
                courseDAO.countPublicCourses(
                        keyword,
                        category
                );


        // --------------------------------------------------------
        // Calculate total pages
        // --------------------------------------------------------

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalElements / size
                );


        // --------------------------------------------------------
        // Prevent page from exceeding total pages
        // --------------------------------------------------------

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }


        // --------------------------------------------------------
        // Calculate SQL offset
        // --------------------------------------------------------

        int offset =
                (page - 1) * size;


        // --------------------------------------------------------
        // Load courses
        // --------------------------------------------------------

        List<CourseDTO> courses =
                courseDAO.findPublicCourses(
                        keyword,
                        category,
                        sort,
                        size,
                        offset
                );


        // --------------------------------------------------------
        // Return Page object
        // --------------------------------------------------------

        return new Page<>(
                courses,
                page,
                size,
                totalElements
        );
    }


    /**
     * Load categories used by the public course filter.
     */
    public List<String> getPublicCategories()
            throws SQLException {

        return courseDAO.findAllCategories();
    }


    /**
     * Get public detail of one published course.
     *
     * Public detail is available only when:
     *
     * - Course status = PUBLISHED
     * - Expert account = ACTIVE
     *
     * For public users who have not enrolled:
     * - Only the first 3 lessons are returned.
     * - Remaining lessons are not exposed to the view.
     *
     * The total lesson count is still loaded separately
     * so the view can show how many lessons are locked.
     */
    public CourseDetailDTO getPublicCourseDetail(
            int courseId)
            throws ServiceException, SQLException {

        CourseDetailDTO course =
                courseDAO.findPublishedCourseDetail(
                        courseId
                );

        if (course == null) {
            throw new ServiceException(
                    "Không tìm thấy khóa học."
            );
        }


        // --------------------------------------------------------
        // Load only the first 3 lessons for public preview
        // --------------------------------------------------------

        course.setLessons(
                courseDAO.findPreviewLessonsByCourseId(
                        courseId
                )
        );


        return course;
    }


    // ============================================================
    // EXPERT OPERATIONS
    // ============================================================

    /**
     * Search courses belonging to one expert.
     */
    public List<CourseDTO> searchCourses(
            int expertId,
            String keyword,
            String status)
            throws SQLException {

        List<Course> courses =
                courseDAO.search(
                        expertId,
                        keyword,
                        status
                );

        return toDTOList(courses);
    }


    /**
     * List all courses belonging to one expert.
     */
    public List<CourseDTO> listByExpert(
            int expertId)
            throws SQLException {

        return toDTOList(
                courseDAO.findByExpertId(
                        expertId
                )
        );
    }


    // ============================================================
    // [DungBD] ADMIN COURSE MANAGEMENT LOGIC
    // ============================================================

    /**
     * [Admin: Overview] Retrieves all courses across the entire system without search filters.
     * [Flow] CourseService.listAllCourses() -> CourseService.searchAllCourses(null, null).
     * [Rules] Administrator global oversight; lists all courses regardless of owning expert.
     * [Output] List<CourseDTO> representing all system courses.
     */
    public List<CourseDTO> listAllCourses()
            throws SQLException {

        return searchAllCourses(
                null,
                null
        );
    }


    /**
     * [Admin: Search] Searches courses system-wide matching optional keyword and status filters.
     * [Flow] CourseService.searchAllCourses() -> CourseDAO.searchAll(keyword, status) -> PostgreSQL JOIN.
     * [Rules] Multi-column search on title, category, and expertName; filters on DRAFT/PUBLISHED/ARCHIVED.
     * [Output] List<CourseDTO> presentation objects.
     */
    public List<CourseDTO> searchAllCourses(
            String keyword,
            String status)
            throws SQLException {

        return courseDAO.searchAll(
                keyword,
                status
        );
    }

    /**
     * [Admin: Course Pagination] Retrieves a paginated Page container of courses for Admin Course List.
     * [Flow] CourseService.getCoursesPage() -> CourseDAO.countSearchAll() & CourseDAO.searchAll() -> Page<CourseDTO>.
     * [Rules] Normalizes page (>=1) and size (>=10); applies whitelist column sorting preventing SQL injection.
     * [Output] Page<CourseDTO> containing paginated course records and pagination calculations.
     */
    public Page<CourseDTO> getCoursesPage(
            String keyword,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 10;

        int totalElements = courseDAO.countSearchAll(keyword, status);
        List<CourseDTO> courses = courseDAO.searchAll(keyword, status, page, pageSize, sortBy, sortOrder);

        return new Page<>(courses, page, pageSize, totalElements);
    }


    /**
     * [Admin: Detail] Retrieves complete metadata of a single course by primary key ID.
     * [Flow] CourseService.getCourseById(id) -> CourseDAO.findById(id) -> toDTO(course).
     * [Rules] Validates course existence; throws ServiceException if no course matches ID.
     * [Output] CourseDTO populated with course information and instructor name.
     */
    public CourseDTO getCourseById(
            int id)
            throws ServiceException, SQLException {

        Course course =
                courseDAO.findById(id);

        if (course == null) {

            throw new ServiceException(
                    "Không tìm thấy khóa học với ID: "
                            + id
            );
        }

        return toDTO(course);
    }


    /**
     * [Admin: Moderation] Updates publication status (DRAFT, PUBLISHED, ARCHIVED) for course oversight.
     * [Flow] CourseService.updateCourseStatusByAdmin() -> CourseDAO.updateStatus(id, status).
     * [Rules] Validates status against VALID_STATUSES whitelist; updates status without expert ownership restriction.
     * [Output] Void; throws ServiceException if status is invalid or course ID is not found.
     */
    public void updateCourseStatusByAdmin(
            int id,
            String status)
            throws ServiceException, SQLException {

        if (status == null
                || !VALID_STATUSES.contains(status)) {

            throw new ServiceException(
                    "Trạng thái khóa học không hợp lệ."
            );
        }


        boolean updated =
                courseDAO.updateStatus(
                        id,
                        status
                );


        if (!updated) {

            throw new ServiceException(
                    "Không tìm thấy khóa học để cập nhật trạng thái."
            );
        }
    }


    // ============================================================
    // EXPERT COURSE MANAGEMENT
    // ============================================================

    /**
     * Get a course belonging to the current Expert.
     */
    public CourseDTO getCourseForExpert(
            int id,
            int expertId)
            throws ServiceException, SQLException {

        Course course =
                courseDAO.findById(id);


        if (course == null
                || course.getExpertId() != expertId) {

            throw new ServiceException(
                    "Không tìm thấy khóa học hoặc bạn không có quyền truy cập."
            );
        }


        return toDTO(course);
    }


    // ============================================================
    // CREATE COURSE
    // ============================================================

    /**
     * Create a new course for an Expert.
     */
    public CourseDTO createCourse(
            int expertId,
            CourseFormDTO dto)
            throws ServiceException, SQLException {

        validate(dto);


        Course course =
                new Course();


        course.setTitle(
                dto.getTitle().trim()
        );


        course.setDescription(
                dto.getDescription() == null
                        ? ""
                        : dto.getDescription().trim()
        );


        course.setCategory(
                dto.getCategory() == null
                        ? ""
                        : dto.getCategory().trim()
        );


        course.setPrice(
                dto.getPrice()
        );


        course.setDurationHours(
                dto.getDurationHours()
        );


        course.setExpertId(
                expertId
        );


        course.setStatus(
                dto.getStatus() == null
                        || dto.getStatus().isEmpty()
                        ? "DRAFT"
                        : dto.getStatus()
        );


        int id =
                courseDAO.insert(course);


        course.setId(id);


        return toDTO(course);
    }


    // ============================================================
    // UPDATE COURSE
    // ============================================================

    /**
     * Update an Expert's own course.
     */
    public CourseDTO updateCourse(
            int expertId,
            CourseFormDTO dto)
            throws ServiceException, SQLException {

        if (dto.getId() == null) {

            throw new ServiceException(
                    "Thiếu ID khóa học."
            );
        }


        validate(dto);


        Course existing =
                courseDAO.findById(
                        dto.getId()
                );


        if (existing == null
                || existing.getExpertId() != expertId) {

            throw new ServiceException(
                    "Không tìm thấy khóa học hoặc bạn không có quyền chỉnh sửa."
            );
        }


        existing.setTitle(
                dto.getTitle().trim()
        );


        existing.setDescription(
                dto.getDescription() == null
                        ? ""
                        : dto.getDescription().trim()
        );


        existing.setCategory(
                dto.getCategory() == null
                        ? ""
                        : dto.getCategory().trim()
        );


        existing.setPrice(
                dto.getPrice()
        );


        existing.setDurationHours(
                dto.getDurationHours()
        );


        existing.setStatus(
                dto.getStatus()
        );


        courseDAO.update(existing);


        return toDTO(
                courseDAO.findById(
                        existing.getId()
                )
        );
    }


    // ============================================================
    // DELETE COURSE
    // ============================================================

    /**
     * Delete an Expert's own course.
     */
    public void deleteCourse(
            int id,
            int expertId)
            throws ServiceException, SQLException {

        boolean deleted =
                courseDAO.deleteById(
                        id,
                        expertId
                );


        if (!deleted) {

            throw new ServiceException(
                    "Không tìm thấy khóa học hoặc bạn không có quyền xóa."
            );
        }
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validate(
            CourseFormDTO dto)
            throws ServiceException {

        if (dto.getTitle() == null
                || dto.getTitle().trim().length() < 3) {

            throw new ServiceException(
                    "Tên khóa học phải có ít nhất 3 ký tự."
            );
        }


        if (dto.getDurationHours() <= 0) {

            throw new ServiceException(
                    "Thời lượng khóa học phải lớn hơn 0."
            );
        }


        if (dto.getPrice() == null || dto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {

            throw new ServiceException(
                    "Giá tiền khóa học không được nhỏ hơn 0."
            );
        }


        if (dto.getStatus() != null
                && !dto.getStatus().isEmpty()
                && !VALID_STATUSES.contains(
                dto.getStatus()
        )) {

            throw new ServiceException(
                    "Trạng thái khóa học không hợp lệ."
            );
        }
    }


    // ============================================================
    // MAPPING
    // ============================================================

    private List<CourseDTO> toDTOList(
            List<Course> courses)
            throws SQLException {

        List<CourseDTO> result =
                new ArrayList<>();


        for (Course c : courses) {

            result.add(
                    toDTO(c)
            );
        }


        return result;
    }


    /**
     * Convert Course entity to CourseDTO.
     */
    private CourseDTO toDTO(
            Course c)
            throws SQLException {

        CourseDTO dto =
                new CourseDTO();


        dto.setId(
                c.getId()
        );


        dto.setTitle(
                c.getTitle()
        );


        dto.setDescription(
                c.getDescription()
        );


        dto.setCategory(
                c.getCategory()
        );


        dto.setPrice(
                c.getPrice()
        );


        dto.setDurationHours(
                c.getDurationHours()
        );


        dto.setExpertId(
                c.getExpertId()
        );


        dto.setStatus(
                c.getStatus()
        );


        dto.setCreatedAt(
                c.getCreatedAt()
        );


        dto.setUpdatedAt(
                c.getUpdatedAt()
        );


        User expert =
                userDAO.findById(
                        c.getExpertId()
                );


        dto.setExpertName(
                expert != null
                        ? expert.getFullName()
                        : "N/A"
        );


        return dto;
    }
}