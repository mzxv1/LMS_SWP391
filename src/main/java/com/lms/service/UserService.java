package com.lms.service;

import com.lms.dao.EmailVerificationTokenDAO;
import com.lms.dao.PasswordResetTokenDAO;
import com.lms.dao.UserDAO;
import com.lms.dto.*;
import com.lms.entity.EmailVerificationToken;
import com.lms.entity.PasswordResetToken;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.util.AppConfig;
import com.lms.util.PasswordUtil;
import com.lms.util.TokenUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Business logic layer for user-related use cases:
 * Register, Login, Profile, and Admin User Management.
 * Controllers must never talk to UserDAO directly.
 */
public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final UserDAO userDAO;
    private final PasswordResetTokenDAO passwordResetTokenDAO;
    private final EmailVerificationTokenDAO emailVerificationTokenDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.passwordResetTokenDAO = new PasswordResetTokenDAO();
        this.emailVerificationTokenDAO = new EmailVerificationTokenDAO();
    }

    public UserService(UserDAO userDAO, PasswordResetTokenDAO passwordResetTokenDAO) {
        this.userDAO = userDAO;
        this.passwordResetTokenDAO = passwordResetTokenDAO;
        this.emailVerificationTokenDAO = new EmailVerificationTokenDAO();
    }

    public UserService(UserDAO userDAO, PasswordResetTokenDAO passwordResetTokenDAO,
                        EmailVerificationTokenDAO emailVerificationTokenDAO) {
        this.userDAO = userDAO;
        this.passwordResetTokenDAO = passwordResetTokenDAO;
        this.emailVerificationTokenDAO = emailVerificationTokenDAO;
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.passwordResetTokenDAO = new PasswordResetTokenDAO();
        this.emailVerificationTokenDAO = new EmailVerificationTokenDAO();
    }

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    /**
     * Step 1 of self-registration: validates the form and, if the username/
     * email are free, stashes the (already hashed) submission under a
     * single-use confirmation token - NOT into "users" yet. The account is
     * only created once the emailed link is clicked, see confirmRegistration().
     * Any earlier still-pending registration for the same email/username is
     * discarded first, so re-submitting the form (e.g. after a lost email)
     * simply reissues a fresh link.
     */
    public EmailVerificationRequestDTO registerPending(UserRegisterDTO dto) throws ServiceException, SQLException {
        validateRegistration(dto);

        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim();

        if (userDAO.findByUsername(username) != null) {
            throw new ServiceException("Tên đăng nhập đã tồn tại.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new ServiceException("Email đã được sử dụng.");
        }

        emailVerificationTokenDAO.deleteByEmailOrUsername(email, username);

        String rawToken = TokenUtil.generateToken();
        int ttlMinutes = AppConfig.getInt("verification.token.ttl.minutes", 60);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setTokenHash(TokenUtil.sha256Hex(rawToken));
        token.setUsername(username);
        token.setPasswordHash(PasswordUtil.hash(dto.getPassword()));
        token.setEmail(email);
        token.setFullName(dto.getFullName().trim());
        token.setExpiresAt(new Timestamp(System.currentTimeMillis() + ttlMinutes * 60_000L));
        emailVerificationTokenDAO.insert(token);

        EmailVerificationRequestDTO result = new EmailVerificationRequestDTO();
        result.setRawToken(rawToken);
        result.setRecipientEmail(email);
        result.setRecipientName(token.getFullName());
        return result;
    }

    /**
     * Step 2 of self-registration: consumes a still-valid confirmation
     * token, creates the actual "users" row (always STUDENT, mirroring the
     * old direct-insert register()), and returns the new account so the
     * caller can log the user straight in.
     */
    public UserDTO confirmRegistration(String rawToken) throws ServiceException, SQLException {
        if (isBlank(rawToken)) {
            throw new ServiceException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }
        EmailVerificationToken token = emailVerificationTokenDAO.findByTokenHash(TokenUtil.sha256Hex(rawToken.trim()));
        if (token == null || token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ServiceException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }

        // Re-check uniqueness: someone else may have taken the username/email
        // while this confirmation link was sitting unopened in an inbox.
        if (userDAO.findByUsername(token.getUsername()) != null
                || userDAO.findByEmail(token.getEmail()) != null) {
            emailVerificationTokenDAO.deleteById(token.getId());
            throw new ServiceException("Tên đăng nhập hoặc email này vừa được sử dụng bởi tài khoản khác.");
        }

        User user = new User();
        user.setUsername(token.getUsername());
        user.setPasswordHash(token.getPasswordHash());
        user.setEmail(token.getEmail());
        user.setFullName(token.getFullName());
        user.setRole(Role.STUDENT); // public self-registration is always STUDENT
        user.setActive(true);

        int id = userDAO.insert(user);
        user.setId(id);

        emailVerificationTokenDAO.deleteById(token.getId());
        return toDTO(user);
    }

    private void validateRegistration(UserRegisterDTO dto) throws ServiceException {
        if (isBlank(dto.getUsername()) || dto.getUsername().trim().length() < 4) {
            throw new ServiceException("Tên đăng nhập phải có ít nhất 4 ký tự.");
        }
        if (isBlank(dto.getPassword()) || dto.getPassword().length() < 6) {
            throw new ServiceException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ServiceException("Mật khẩu xác nhận không khớp.");
        }
        if (isBlank(dto.getEmail()) || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new ServiceException("Email không hợp lệ.");
        }
        if (isBlank(dto.getFullName())) {
            throw new ServiceException("Họ tên không được để trống.");
        }
    }

    // ---------- Login ----------

    public UserDTO login(UserLoginDTO dto) throws ServiceException, SQLException {
        if (isBlank(dto.getUsername()) || isBlank(dto.getPassword())) {
            throw new ServiceException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        User user = userDAO.findByUsername(dto.getUsername().trim());
        if (user == null || !PasswordUtil.verify(dto.getPassword(), user.getPasswordHash())) {
            throw new ServiceException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (!user.isActive()) {
            throw new ServiceException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        return toDTO(user);
    }

    // ---------- Google Sign-In ----------

    /**
     * Logs in with a Google profile, auto-provisioning a STUDENT account on
     * first sign-in (mirrors register(): public self-provisioning is always
     * STUDENT). Accounts are linked purely by verified email - no google_id
     * column - so this is only safe because we require email_verified.
     */
    public UserDTO loginWithGoogle(GoogleUserDTO google) throws ServiceException, SQLException {
        if (google == null || isBlank(google.getEmail())) {
            throw new ServiceException("Không lấy được email từ Google.");
        }
        if (!google.isEmailVerified()) {
            throw new ServiceException("Email Google của bạn chưa được xác thực.");
        }

        String email = google.getEmail().trim();
        User user = userDAO.findByEmail(email);

        if (user == null) {
            user = provisionGoogleUser(google, email);
        }

        if (!user.isActive()) {
            throw new ServiceException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        return toDTO(user);
    }

    private User provisionGoogleUser(GoogleUserDTO google, String email) throws SQLException {
        String fullName = isBlank(google.getName()) ? email : google.getName().trim();

        User user = new User();
        user.setUsername(uniqueUsernameFromEmail(email));
        // A Google-provisioned account has no password the user knows. We still
        // must store a valid bcrypt hash (password_hash is NOT NULL and
        // PasswordUtil.verify/BCrypt.checkpw throws on a non-bcrypt string), so
        // we hash a random UUID nobody will ever type. login() keeps working
        // unchanged for these rows; they simply can never match it.
        user.setPasswordHash(PasswordUtil.hash(UUID.randomUUID().toString()));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(Role.STUDENT);
        user.setActive(true);

        int id = userDAO.insert(user);
        user.setId(id);
        return user;
    }

    /** Derives a unique username from the email local-part, min 4 chars, numeric suffix on collision. */
    private String uniqueUsernameFromEmail(String email) throws SQLException {
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String base = localPart.replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isEmpty()) {
            base = "user";
        }
        while (base.length() < 4) {
            base = base + "0";
        }

        String candidate = base;
        int suffix = 1;
        while (userDAO.findByUsername(candidate) != null) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    // ============================================================
    // USER SELF-SERVICE (Profile)
    // ============================================================

    public UserDTO getProfile(int userId) throws SQLException, ServiceException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ServiceException("Không tìm thấy người dùng.");
        }
        return toDTO(user);
    }

    public UserDTO getUserByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            return null;
        }
        return toDTO(user);
    }

    public UserDTO updateProfile(int userId, UserProfileUpdateDTO dto) throws ServiceException, SQLException {
        if (isBlank(dto.getFullName())) {
            throw new ServiceException("Họ tên không được để trống.");
        }

        User existing = userDAO.findById(userId);
        if (existing == null) {
            throw new ServiceException("Không tìm thấy người dùng.");
        }

        // Email is fixed at registration and is not user-editable from the profile
        // page; keep it exactly as stored regardless of what the form submitted.
        String phone = (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) ? dto.getPhone().trim() : null;
        userDAO.updateProfile(userId, dto.getFullName().trim(), existing.getEmail(), phone);

        // Optional password change (no current-password confirmation required)
        if (!isBlank(dto.getNewPassword())) {
            if (dto.getNewPassword().length() < 6) {
                throw new ServiceException("Mật khẩu mới phải có ít nhất 6 ký tự.");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new ServiceException("Xác nhận mật khẩu mới không khớp.");
            }
            userDAO.updatePassword(userId, PasswordUtil.hash(dto.getNewPassword()));
        }

        return toDTO(userDAO.findById(userId));
    }

    // ============================================================
    // PASSWORD RECOVERY
    // ============================================================

    /**
     * Creates a single-use reset token for the given email, or returns null
     * if no active user matches - callers must show the same neutral message
     * either way so this never becomes a user-enumeration oracle.
     */
    public PasswordResetRequestDTO createPasswordResetToken(String email) throws SQLException {
        if (isBlank(email)) {
            return null;
        }
        User user = userDAO.findByEmail(email.trim());
        if (user == null || !user.isActive()) {
            return null;
        }

        passwordResetTokenDAO.invalidateAllForUser(user.getId());

        String rawToken = TokenUtil.generateToken();
        int ttlMinutes = AppConfig.getInt("reset.token.ttl.minutes", 30);

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(TokenUtil.sha256Hex(rawToken));
        token.setExpiresAt(new Timestamp(System.currentTimeMillis() + ttlMinutes * 60_000L));
        passwordResetTokenDAO.insert(token);

        PasswordResetRequestDTO result = new PasswordResetRequestDTO();
        result.setRawToken(rawToken);
        result.setRecipientEmail(user.getEmail());
        result.setRecipientName(user.getFullName());
        return result;
    }

    /** Returns the owning user for a still-valid token, or throws if missing/used/expired. */
    public UserDTO validateResetToken(String rawToken) throws ServiceException, SQLException {
        PasswordResetToken token = findValidToken(rawToken);
        User user = userDAO.findById(token.getUserId());
        if (user == null) {
            throw new ServiceException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }
        return toDTO(user);
    }

    public UserDTO resetPassword(PasswordResetDTO dto) throws ServiceException, SQLException {
        if (isBlank(dto.getNewPassword()) || dto.getNewPassword().length() < 6) {
            throw new ServiceException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ServiceException("Mật khẩu xác nhận không khớp.");
        }

        PasswordResetToken token = findValidToken(dto.getToken());
        userDAO.updatePassword(token.getUserId(), PasswordUtil.hash(dto.getNewPassword()));
        passwordResetTokenDAO.markUsed(token.getId());

        User user = userDAO.findById(token.getUserId());
        return toDTO(user);
    }

    private PasswordResetToken findValidToken(String rawToken) throws ServiceException, SQLException {
        if (isBlank(rawToken)) {
            throw new ServiceException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }
        PasswordResetToken token = passwordResetTokenDAO.findByTokenHash(TokenUtil.sha256Hex(rawToken.trim()));
        if (token == null || token.getUsedAt() != null
                || token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ServiceException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }
        return token;
    }

    // ============================================================
    // [DungBD] ADMIN USER MANAGEMENT LOGIC
    // ============================================================

    /**
     * [Admin: User Search & Pagination] Retrieves a paginated Page container of users for User List (SRS 3.2.1).
     * [Flow] UserService.getUsersPage() -> UserDAO.countSearch() & UserDAO.search() -> toDTO() -> Page<UserDTO>.
     * [Rules] Keyword ILIKE filter, role/status exact filter, and SQL injection-safe whitelist column sorting.
     * [Output] Page<UserDTO> containing paginated user records and pagination metadata.
     */
    public Page<UserDTO> getUsersPage(
            String keyword,
            String role,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 10;

        int totalElements = userDAO.countSearch(keyword, role, status);
        List<User> users = userDAO.search(keyword, role, status, page, pageSize, sortBy, sortOrder);

        List<UserDTO> dtoList = new ArrayList<>();
        for (User u : users) {
            dtoList.add(toDTO(u));
        }

        return new Page<>(dtoList, page, pageSize, totalElements);
    }

    /**
     * [Admin: Legacy Search] Helper to query user accounts matching a keyword and specific role.
     * [Flow] UserService.searchUsers() -> UserDAO.search(keyword, role) -> toDTO() -> List<UserDTO>.
     * [Rules] Simple multi-field text search; maps User entities into UserDTO presentation models.
     * [Output] List<UserDTO> matching the filter criteria.
     */
    public List<UserDTO> searchUsers(String keyword, String role) throws SQLException {
        List<User> users = userDAO.search(keyword, role);
        List<UserDTO> result = new ArrayList<>();
        for (User u : users) {
            result.add(toDTO(u));
        }
        return result;
    }

    /**
     * [Admin: Detail Lookup] Retrieves a single user's detailed profile by primary key ID (SRS 3.2.2).
     * [Flow] UserService.getUserById(id) -> UserDAO.findById(id) -> toDTO(user).
     * [Rules] Validates user existence; throws ServiceException if no user matches ID.
     * [Output] UserDTO populated with user profile and role details.
     */
    public UserDTO getUserById(int id) throws ServiceException, SQLException {
        User user = userDAO.findById(id);

        if (user == null) {
            throw new ServiceException("Không tìm thấy người dùng.");
        }

        return toDTO(user);
    }

    /**
     * [Admin: User Creation] Validates and creates a new user account with BCrypt password hashing (SRS 3.2.3).
     * [Flow] UserService.createUserByAdmin() -> UserDAO.findByUsername/findByEmail -> PasswordUtil.hash() -> UserDAO.insert().
     * [Rules] Enforces username >=4 chars, password >=6 chars, password confirmation, email format, and unique checks.
     * [Output] UserDTO of the newly created account.
     */
    public UserDTO createUserByAdmin(AdminUserFormDTO dto) throws ServiceException, SQLException {
        if (isBlank(dto.getUsername()) || dto.getUsername().trim().length() < 4) {
            throw new ServiceException("Tên đăng nhập phải có ít nhất 4 ký tự.");
        }
        if (isBlank(dto.getEmail()) || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new ServiceException("Email không hợp lệ.");
        }
        if (isBlank(dto.getFullName())) {
            throw new ServiceException("Họ tên không được để trống.");
        }
        Role role = parseRole(dto.getRole());

        if (userDAO.findByUsername(dto.getUsername().trim()) != null) {
            throw new ServiceException("Tên đăng nhập đã tồn tại.");
        }
        if (userDAO.findByEmail(dto.getEmail().trim()) != null) {
            throw new ServiceException("Email đã được sử dụng.");
        }

        // Admin does not set a password - a random one is generated so the
        // account can be created immediately; it's returned once (via
        // UserDTO#getGeneratedPassword) so the admin can hand it to the user.
        String rawPassword = PasswordUtil.generateRandomPassword();

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPasswordHash(PasswordUtil.hash(rawPassword));
        user.setEmail(dto.getEmail().trim());
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone().trim());
        user.setRole(role);
        user.setActive(dto.isActive());

        int id = userDAO.insert(user);
        user.setId(id);

        UserDTO created = toDTO(user);
        created.setGeneratedPassword(rawPassword);
        return created;
    }

    /**
     * [Admin: User Update] Updates profile, role, active status, and optional password for a user (SRS 3.2.2).
     * [Flow] UserService.updateUserByAdmin() -> UserDAO.update() & UserDAO.updatePassword() -> UserDAO.findById().
     * [Rules] Enforces Self-Protection against self-deactivation/demotion, email uniqueness, and validate-before-mutate.
     * [Output] Updated UserDTO reflecting persisted changes.
     */
    public UserDTO updateUserByAdmin(AdminUserFormDTO dto, Integer currentAdminId) throws ServiceException, SQLException {
        if (dto.getId() == null) {
            throw new ServiceException("Thiếu ID người dùng.");
        }
        User existing = userDAO.findById(dto.getId());
        if (existing == null) {
            throw new ServiceException("Không tìm thấy người dùng.");
        }
        if (isBlank(dto.getEmail()) || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new ServiceException("Email không hợp lệ.");
        }
        if (isBlank(dto.getFullName())) {
            throw new ServiceException("Họ tên không được để trống.");
        }
        Role role = parseRole(dto.getRole());

        if (!isBlank(dto.getPassword())) {
            if (dto.getPassword().length() < 6) {
                throw new ServiceException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
        }

        if (currentAdminId != null && dto.getId().equals(currentAdminId)) {
            if (!dto.isActive()) {
                throw new ServiceException("Bạn không thể vô hiệu hóa tài khoản quản trị đang đăng nhập của chính mình.");
            }
            if (role != Role.ADMIN) {
                throw new ServiceException("Bạn không thể thay đổi vai trò quản trị của chính mình.");
            }
        }

        if (!existing.getEmail().equalsIgnoreCase(dto.getEmail().trim())) {
            User other = userDAO.findByEmail(dto.getEmail().trim());
            if (other != null && other.getId() != dto.getId()) {
                throw new ServiceException("Email đã được sử dụng bởi tài khoản khác.");
            }
        }

        existing.setEmail(dto.getEmail().trim());
        existing.setFullName(dto.getFullName().trim());
        existing.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone().trim());
        existing.setRole(role);
        existing.setActive(dto.isActive());
        userDAO.update(existing);

        if (!isBlank(dto.getPassword())) {
            userDAO.updatePassword(existing.getId(), PasswordUtil.hash(dto.getPassword()));
        }

        return toDTO(userDAO.findById(existing.getId()));
    }

    /**
     * [Admin: Quick Status Toggle] Toggles user active state (Active <-> Inactive) from the list view.
     * [Flow] UserService.toggleUserStatus() -> UserDAO.findById(id) -> UserDAO.updateStatus(id, newStatus).
     * [Rules] Enforces Self-Protection preventing logged-in Admin from deactivating their own active account.
     * [Output] Boolean representing the new active state (true = Active, false = Inactive).
     */
    public boolean toggleUserStatus(int userId, Integer currentAdminId) throws ServiceException, SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ServiceException("Không tìm thấy người dùng.");
        }

        boolean newStatus = !user.isActive();

        if (currentAdminId != null && userId == currentAdminId && !newStatus) {
            throw new ServiceException("Bạn không thể vô hiệu hóa tài khoản quản trị đang đăng nhập của chính mình.");
        }

        userDAO.updateStatus(userId, newStatus);
        return newStatus;
    }

    /**
     * [Admin: Deletion] Permanently deletes a user account by primary key ID.
     * [Flow] UserService.deleteUser(id) -> UserDAO.deleteById(id).
     * [Rules] Deletes user record by ID; propagates SQLException on database constraint error.
     * [Output] Void.
     */
    public void deleteUser(int id) throws SQLException {
        userDAO.deleteById(id);
    }

    // ---------- helpers ----------

    /**
     * [Helper: Enum Parsing] Parses a case-insensitive string into a strongly-typed Role enum constant.
     * [Flow] Role.valueOf(role.trim().toUpperCase()).
     * [Rules] Validates role string against enum values; throws ServiceException if unrecognized.
     * [Output] Role enum constant.
     */
    private Role parseRole(String role) throws ServiceException {
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            throw new ServiceException("Vai trò không hợp lệ.");
        }
    }

    /**
     * [Helper: String Check] Evaluates whether a given string is null, empty, or consists solely of whitespace.
     * [Flow] (s == null || s.trim().isEmpty()).
     * [Rules] Null-safe string validation.
     * [Output] Boolean true if string is blank; false otherwise.
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * [Helper: Model Transformation] Maps a User database domain entity into a UserDTO representation model.
     * [Flow] User entity getters -> UserDTO setters.
     * [Rules] Strips sensitive password hashes and copies public profile attributes.
     * [Output] UserDTO instance.
     */
    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setFullName(u.getFullName());
        dto.setPhone(u.getPhone());
        dto.setRole(u.getRole().name());
        dto.setActive(u.isActive());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}
