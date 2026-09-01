-- ============================================================
-- LMS Database Schema (PostgreSQL) - HỆ THỐNG QUẢN LÝ HỌC TẬP CHUẨN (G4 - SWP391)
-- Thư mục: database/schema.sql
--
-- Bao gồm 17 nhóm bảng & dữ liệu mẫu bảo toàn 100%:
--   1. users                  : Người dùng (ADMIN, EXPERT, STUDENT)
--   2. password_reset_tokens  : Token đặt lại mật khẩu người dùng
--   3. settings               : Cấu hình hệ thống & Master Lookup Data (Role, Category, Level...)
--   4. courses                : Khóa học (FK -> settings.id)
--   5. chapters               : Chương học (Tầng trung gian Course - Lesson/Quiz)
--   6. lessons                : Bài học (FK -> chapters.id, content_url, lesson_type)
--   7. enrollments            : Đăng ký học (progress_percent, status, course_price, is_paid)
--   8. lesson_progresses      : Tiến độ chi tiết từng bài học của học viên
--   9. payments               : Giao dịch thanh toán khóa học (VNPay, SePay, Banking)
--  10. reviews                : Đánh giá & Phản hồi khóa học
--  11. questions              : Ngân hàng câu hỏi (FK -> chapters.id)
--  12. answer_options         : Đáp án lựa chọn cho câu hỏi (is_correct)
--  13. quizzes                : Đề thi / Bài kiểm tra (time_limit_min, pass_score)
--  14. quiz_chapters          : Cấu hình random câu hỏi theo chương (question_count)
--  15. quiz_attempts          : Lượt làm bài kiểm tra của học viên
--  16. quiz_attempt_questions : Cố định danh sách câu hỏi random cho lượt làm bài
--  17. quiz_answers           : Chi tiết câu trả lời của học viên (FK -> answer_options.id)
-- ============================================================

-- Xóa các bảng cũ theo thứ tự phụ thuộc khóa ngoại
SET client_encoding = 'UTF8';

DROP TABLE IF EXISTS quiz_answers CASCADE;
DROP TABLE IF EXISTS quiz_attempt_questions CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS quiz_chapters CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS answer_options CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS lesson_progresses CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS settings CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. BẢNG NGƯỜI DÙNG (USERS)
CREATE TABLE users (
                       id            SERIAL PRIMARY KEY,
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       email         VARCHAR(100) NOT NULL UNIQUE,
                       full_name     VARCHAR(150) NOT NULL,
                       phone         VARCHAR(20),
                       role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'EXPERT', 'STUDENT')),
                       active        BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG TOKEN ĐẶT LẠI MẬT KHẨU (PASSWORD RESET TOKENS)
CREATE TABLE password_reset_tokens (
                                       id         SERIAL PRIMARY KEY,
                                       user_id    INTEGER     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token_hash VARCHAR(64) NOT NULL UNIQUE,
                                       expires_at TIMESTAMP   NOT NULL,
                                       used_at    TIMESTAMP,
                                       created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2b. BẢNG TOKEN XÁC NHẬN ĐĂNG KÝ QUA EMAIL (EMAIL VERIFICATION TOKENS)
-- Registration is a two-step flow: the account row is only inserted into
-- "users" once the confirmation link is clicked, so the pending form data
-- (including the already-hashed password) is held here until then.
CREATE TABLE email_verification_tokens (
                                            id            SERIAL PRIMARY KEY,
                                            token_hash    VARCHAR(64)  NOT NULL UNIQUE,
                                            username      VARCHAR(50)  NOT NULL,
                                            password_hash VARCHAR(100) NOT NULL,
                                            email         VARCHAR(100) NOT NULL,
                                            full_name     VARCHAR(150) NOT NULL,
                                            expires_at    TIMESTAMP    NOT NULL,
                                            created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. BẢNG CẤU HÌNH HỆ THỐNG & MASTER LOOKUP (SETTINGS)
CREATE TABLE settings (
                          id          SERIAL PRIMARY KEY,
                          type        VARCHAR(50),
                          name        VARCHAR(20)  NOT NULL,
                          value       VARCHAR(100) NOT NULL,
                          priority    INTEGER      NOT NULL DEFAULT 1 CHECK (priority > 0),
                          status      VARCHAR(20)  NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive')),
                          description VARCHAR(200),
                          created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. BẢNG KHÓA HỌC (COURSES)
CREATE TABLE courses (
                         id             SERIAL PRIMARY KEY,
                         title          VARCHAR(200) NOT NULL,
                         description    TEXT,
                         category_id    INTEGER      REFERENCES settings(id) ON DELETE SET NULL,
                         price          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                         duration_hours INTEGER      NOT NULL DEFAULT 0,
                         expert_id      INTEGER      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
                         created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. BẢNG CHƯƠNG HỌC (CHAPTERS)
CREATE TABLE chapters (
                          id          SERIAL PRIMARY KEY,
                          course_id   INTEGER      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                          title       VARCHAR(200) NOT NULL,
                          order_index INTEGER      DEFAULT 1,
                          created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. BẢNG BÀI HỌC (LESSONS)
CREATE TABLE lessons (
                         id               SERIAL PRIMARY KEY,
                         course_id        INTEGER      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                         chapter_id       INTEGER      NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
                         title            VARCHAR(200) NOT NULL,
                         content_url      VARCHAR(300),
                         lesson_type      VARCHAR(20)  NOT NULL DEFAULT 'VIDEO' CHECK (lesson_type IN ('VIDEO', 'DOC', 'QUIZ')),
                         duration_minutes INTEGER      DEFAULT 0,
                         order_index      INTEGER      DEFAULT 1,
                         created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. BẢNG ĐĂNG KÝ HỌC (ENROLLMENTS)
CREATE TABLE enrollments (
                             id               SERIAL PRIMARY KEY,
                             student_id       INTEGER      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             course_id        INTEGER      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                             course_price     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                             is_paid          BOOLEAN      NOT NULL DEFAULT FALSE,
                             enrolled_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             progress_percent INTEGER      DEFAULT 0 CHECK (progress_percent >= 0 AND progress_percent <= 100),
                             status           VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED', 'PENDING')),
                             CONSTRAINT uk_student_course UNIQUE (student_id, course_id)
);

-- 8. BẢNG TIẾN ĐỘ BÀI HỌC CHI TIẾT (LESSON PROGRESSES)
CREATE TABLE lesson_progresses (
                                   id           SERIAL PRIMARY KEY,
                                   user_id      INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   lesson_id    INTEGER   NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
                                   completed    BOOLEAN   NOT NULL DEFAULT FALSE,
                                   completed_at TIMESTAMP,
                                   created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT uk_user_lesson UNIQUE (user_id, lesson_id)
);

-- 9. BẢNG GIAO DỊCH THANH TOÁN (PAYMENTS)
CREATE TABLE payments (
                          id             SERIAL PRIMARY KEY,
                          user_id        INTEGER       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          enrollment_id  INTEGER       NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
                          payment_method VARCHAR(50),
                          amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                          status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
                          created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. BẢNG ĐÁNH GIÁ (REVIEWS)
CREATE TABLE reviews (
                         id          SERIAL PRIMARY KEY,
                         student_id  INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         course_id   INTEGER   NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                         rating      INTEGER   NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment     TEXT,
                         created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. BẢNG NGÂN HÀNG CÂU HỎI (QUESTIONS)
CREATE TABLE questions (
                           id          SERIAL PRIMARY KEY,
                           chapter_id  INTEGER   NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
                           content     TEXT      NOT NULL,
                           explanation TEXT,
                           created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. BẢNG ĐÁP ÁN LỰA CHỌN (ANSWER OPTIONS)
CREATE TABLE answer_options (
                                id          SERIAL PRIMARY KEY,
                                question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                content     TEXT    NOT NULL,
                                is_correct  BOOLEAN NOT NULL DEFAULT FALSE
);

-- 13. BẢNG BÀI KIỂM TRA (QUIZZES)
CREATE TABLE quizzes (
                         id              SERIAL PRIMARY KEY,
                         course_id       INTEGER      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                         title           VARCHAR(200) NOT NULL,
                         total_questions INTEGER      DEFAULT 10,
                         pass_score      INTEGER      DEFAULT 70,
                         time_limit_min  INTEGER      DEFAULT 30,
                         created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. BẢNG CẤU HÌNH CÂU HỎI THEO CHƯƠNG (QUIZ CHAPTERS)
CREATE TABLE quiz_chapters (
                               id             SERIAL PRIMARY KEY,
                               quiz_id        INTEGER NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
                               chapter_id     INTEGER NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
                               question_count INTEGER NOT NULL DEFAULT 5,
                               CONSTRAINT uk_quiz_chapter UNIQUE (quiz_id, chapter_id)
);

-- 15. BẢNG LƯỢT LÀM BÀI (QUIZ ATTEMPTS)
CREATE TABLE quiz_attempts (
                               id           SERIAL PRIMARY KEY,
                               user_id      INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               quiz_id      INTEGER   NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
                               score        INTEGER,
                               is_passed    BOOLEAN,
                               started_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               submitted_at TIMESTAMP
);

-- 16. BẢNG CỐ ĐỊNH CÂU HỎI CHO LƯỢT LÀM BÀI (QUIZ ATTEMPT QUESTIONS)
CREATE TABLE quiz_attempt_questions (
                                        id          SERIAL PRIMARY KEY,
                                        attempt_id  INTEGER NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
                                        question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                        CONSTRAINT uk_attempt_question UNIQUE (attempt_id, question_id)
);

-- 17. BẢNG CÂU TRẢ LỜI CỦA HỌC VIÊN (QUIZ ANSWERS)
CREATE TABLE quiz_answers (
                              id          SERIAL PRIMARY KEY,
                              attempt_id  INTEGER NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
                              question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                              option_id   INTEGER NOT NULL REFERENCES answer_options(id) ON DELETE CASCADE,
                              CONSTRAINT uk_attempt_q_ans UNIQUE (attempt_id, question_id)
);

-- TẠO CÁC CHỈ MỤC (INDEXES) TỐI ƯU TỐC ĐỘ TRUY VẤN
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_prt_user ON password_reset_tokens(user_id);
CREATE INDEX idx_evt_email ON email_verification_tokens(email);
CREATE INDEX idx_settings_type ON settings(type);
CREATE INDEX idx_settings_status ON settings(status);
CREATE INDEX idx_courses_category ON courses(category_id);
CREATE INDEX idx_courses_expert ON courses(expert_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_chapters_course ON chapters(course_id);
CREATE INDEX idx_lessons_course ON lessons(course_id);
CREATE INDEX idx_lessons_chapter ON lessons(chapter_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_lesson_progresses_user ON lesson_progresses(user_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_enrollment ON payments(enrollment_id);
CREATE INDEX idx_reviews_course ON reviews(course_id);
CREATE INDEX idx_questions_chapter ON questions(chapter_id);
CREATE INDEX idx_answer_options_question ON answer_options(question_id);
CREATE INDEX idx_quizzes_course ON quizzes(course_id);
CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);

-- ============================================================
-- DỮ LIỆU MẪU BẢO TOÀN 100% (SEED DATA)
-- Tất cả mật khẩu người dùng mẫu: Password@123
-- ============================================================

-- 1. Chèn dữ liệu Người dùng (5 Users)
INSERT INTO users (id, username, password_hash, email, full_name, phone, role, active) VALUES
                                                                                           (1, 'admin',    '$2a$12$dxjQNqRyKERH9zztbtZ.Nu4GC7c3vbHyeFd427ESBFFm3JZ9ujMfu', 'admin@lms.vn',   'System Administrator', '0901234567', 'ADMIN',   TRUE),
                                                                                           (2, 'expert1',  '$2a$12$dxjQNqRyKERH9zztbtZ.Nu4GC7c3vbHyeFd427ESBFFm3JZ9ujMfu', 'expert1@lms.vn', 'Nguyen Van Chuyen',    '0902345678', 'EXPERT',  TRUE),
                                                                                           (3, 'expert2',  '$2a$12$dxjQNqRyKERH9zztbtZ.Nu4GC7c3vbHyeFd427ESBFFm3JZ9ujMfu', 'expert2@lms.vn', 'Tran Thi Binh',        '0903456789', 'EXPERT',  TRUE),
                                                                                           (4, 'student1', '$2a$12$dxjQNqRyKERH9zztbtZ.Nu4GC7c3vbHyeFd427ESBFFm3JZ9ujMfu', 'student1@lms.vn','Tran Thi Hoc Vien',    '0904567890', 'STUDENT', TRUE),
                                                                                           (5, 'student2', '$2a$12$dxjQNqRyKERH9zztbtZ.Nu4GC7c3vbHyeFd427ESBFFm3JZ9ujMfu', 'student2@lms.vn','Le Van Hoc',           '0905678901', 'STUDENT', TRUE);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 2. Chèn dữ liệu Master Lookup Settings (User Role, Subject, Semester, Course Category, Course Level)
INSERT INTO settings (id, type, name, value, priority, status, description) VALUES
    -- Root Types
    (1, NULL, 'User Role',       'USER_ROLE',       1, 'Active', 'Nhóm vai trò người dùng hệ thống'),
    (2, NULL, 'Subject',         'SUBJECT',         2, 'Active', 'Nhóm môn học / chuyên đề đào tạo'),
    (3, NULL, 'Semester',        'SEMESTER',        3, 'Active', 'Nhóm học kỳ đào tạo'),
    (4, NULL, 'Course Category', 'COURSE_CATEGORY', 4, 'Active', 'Nhóm danh mục khóa học'),
    (5, NULL, 'Course Level',    'COURSE_LEVEL',    5, 'Active', 'Nhóm cấp độ khóa học'),

    -- Child Settings: User Role
    (6, 'User Role', 'Admin',    'ADMIN',    1, 'Active', 'Quản trị viên toàn quyền hệ thống'),
    (7, 'User Role', 'Expert',   'EXPERT',   2, 'Active', 'Giảng viên / Chuyên gia xây dựng và quản lý khóa học'),
    (8, 'User Role', 'Student',  'STUDENT',  3, 'Active', 'Học viên tham gia học tập trên nền tảng'),
    (9, 'User Role', 'Manager',  'MANAGER',  4, 'Active', 'Người quản lý chuyên môn và phân công'),

    -- Child Settings: Course Category (category_id FK -> settings.id 10->14)
    (10, 'Course Category', 'Programming',     'PROGRAMMING', 1, 'Active', 'Các khóa học lập trình Java, Python, C++, C#'),
    (11, 'Course Category', 'Web Development', 'WEB_DEV',     2, 'Active', 'Phát triển Web Frontend, Backend và Fullstack'),
    (12, 'Course Category', 'Design',          'DESIGN',      3, 'Active', 'Thiết kế đồ họa, UI/UX và đồ họa chuyển động'),
    (13, 'Course Category', 'Database',        'DATABASE',    4, 'Active', 'Quản trị CSDL SQL, NoSQL, PostgreSQL và MySQL'),
    (14, 'Course Category', 'AI & Data',       'AI_DATA',     5, 'Active', 'Trí tuệ nhân tạo, Machine Learning và Khoa học dữ liệu'),

    -- Child Settings: Course Level
    (20, 'Course Level', 'Beginner',     'BEGINNER',     1, 'Active', 'Dành cho người mới bắt đầu tiếp cận'),
    (21, 'Course Level', 'Intermediate', 'INTERMEDIATE', 2, 'Active', 'Trình độ trung cấp đã có nền tảng'),
    (22, 'Course Level', 'Advanced',     'ADVANCED',     3, 'Active', 'Trình độ nâng cao chuyên sâu thực chiến'),

    -- Child Settings: Subject (Iteration 3)
    (30, 'Subject', 'Java Core',        'JAVA_CORE', 1, 'Active', 'Chuyên đề Lập trình hướng đối tượng Java căn bản'),
    (31, 'Subject', 'Web Frontend',     'WEB_FE',    2, 'Active', 'Chuyên đề HTML5, CSS3, JavaScript và ReactJS'),
    (32, 'Subject', 'Database Systems', 'DB_SYS',    3, 'Active', 'Chuyên đề Cơ sở dữ liệu quan hệ PostgreSQL/MySQL'),
    (33, 'Subject', 'Software Testing', 'SW_TEST',   4, 'Active', 'Chuyên đề Kiểm thử phần mềm và Đảm bảo chất lượng'),

    -- Child Settings: Semester (Iteration 3)
    (40, 'Semester', 'Spring',  'SP26', 1, 'Active', 'Học kỳ Mùa Xuân năm 2026'),
    (41, 'Semester', 'Summer',  'SU26', 2, 'Active', 'Học kỳ Mùa Hè năm 2026'),
    (42, 'Semester', 'Fall',    'FA26', 3, 'Active', 'Học kỳ Mùa Thu năm 2026');
SELECT setval('settings_id_seq', (SELECT MAX(id) FROM settings));

-- 3. Chèn dữ liệu Khóa học (17 Courses - category_id FK -> settings.id 10->13)
INSERT INTO courses (id, title, description, category_id, price, duration_hours, expert_id, status) VALUES
                                                                                                        (1,  'Lập trình Java Cơ Bản đến Nâng Cao',      'Khóa học cung cấp kiến thức nền tảng Java, Hướng đối tượng (OOP), Collections framework và JDBC.', 10, 500000, 25, 2, 'PUBLISHED'),
                                                                                                        (2,  'Lập trình Python cho người mới bắt đầu',  'Học Python từ cơ bản đến nâng cao, bao gồm biến, hàm, cấu trúc dữ liệu, OOP và xử lý file.', 10, 450000, 20, 2, 'PUBLISHED'),
                                                                                                        (3,  'Lập trình C++ và tư duy lập trình',        'Nắm vững nền tảng C++, cấu trúc dữ liệu, con trỏ, OOP và tư duy giải quyết bài toán.', 10, 550000, 28, 2, 'PUBLISHED'),
                                                                                                        (4,  'Lập trình Java nâng cao',                 'Tìm hiểu Java nâng cao với Multithreading, Stream API, Lambda Expression, Generics và Design Pattern.', 10, 650000, 30, 2, 'PUBLISHED'),
                                                                                                        (5,  'Cấu trúc dữ liệu và giải thuật',          'Khóa học tập trung vào Array, Linked List, Stack, Queue, Tree, Graph và các thuật toán tìm kiếm, sắp xếp.', 10, 600000, 32, 2, 'PUBLISHED'),
                                                                                                        (6,  'Xây dựng Web App với Spring Boot',        'Học cách thiết kế RESTful API, chuẩn kết nối Spring Data JPA và Security với Spring Boot 3.', 11, 750000, 35, 2, 'PUBLISHED'),
                                                                                                        (7,  'HTML CSS JavaScript từ cơ bản',            'Xây dựng nền tảng Frontend với HTML5, CSS3 và JavaScript thông qua các dự án thực tế.', 11, 400000, 22, 3, 'PUBLISHED'),
                                                                                                        (8,  'Frontend với ReactJS',                    'Xây dựng giao diện Web hiện đại với ReactJS, Components, Hooks, State Management và REST API.', 11, 700000, 30, 3, 'PUBLISHED'),
                                                                                                        (9,  'Node.js và Express.js',                   'Xây dựng Backend API với Node.js, Express.js, Middleware, Authentication và kết nối PostgreSQL.', 11, 650000, 28, 3, 'PUBLISHED'),
                                                                                                        (10, 'Thiết kế Giao diện UI/UX chuẩn',          'Phương pháp thiết kế Figma, Layout Bootstrap 5 và nâng cao trải nghiệm người dùng Web App.', 12, 400000, 15, 3, 'PUBLISHED'),
                                                                                                        (11, 'Figma từ cơ bản đến thực chiến',          'Học cách sử dụng Figma để xây dựng Wireframe, Prototype và thiết kế giao diện sản phẩm số.', 12, 350000, 18, 3, 'PUBLISHED'),
                                                                                                        (12, 'UI Design cho Web Application',           'Thiết kế giao diện Web Application chuyên nghiệp, hệ thống màu sắc, typography và component.', 12, 500000, 20, 3, 'PUBLISHED'),
                                                                                                        (13, 'Quản trị CSDL PostgreSQL',                'Tối ưu hóa câu lệnh SQL, Indexing, Transaction và backup khôi phục cơ sở dữ liệu lớn.', 13, 600000, 20, 3, 'PUBLISHED'),
                                                                                                        (14, 'SQL từ cơ bản đến nâng cao',               'Học SQL với SELECT, JOIN, GROUP BY, Subquery, CTE, Window Function và tối ưu truy vấn.', 13, 450000, 18, 3, 'PUBLISHED'),
                                                                                                        (15, 'Database Design và tối ưu hệ thống',      'Thiết kế Database, chuẩn hóa dữ liệu, Index, Transaction và các kỹ thuật tối ưu cơ sở dữ liệu.', 13, 700000, 25, 3, 'PUBLISHED'),
                                                                                                        (16, 'Khóa học Spring Security nâng cao',        'Tìm hiểu Authentication, Authorization, JWT và Security Architecture với Spring Security.', 11, 800000, 24, 2, 'DRAFT'),
                                                                                                        (17, 'Advanced PostgreSQL Performance',         'Phân tích execution plan, indexing nâng cao và tối ưu PostgreSQL cho hệ thống lớn.', 13, 900000, 26, 3, 'ARCHIVED');
SELECT setval('courses_id_seq', (SELECT MAX(id) FROM courses));

-- 4. Chèn dữ liệu Chương học mặc định (17 Chapters)
INSERT INTO chapters (id, course_id, title, order_index) VALUES
                                                             (1,  1,  'Toàn bộ khóa học Java Cơ Bản', 1),
                                                             (2,  2,  'Toàn bộ khóa học Python', 1),
                                                             (3,  3,  'Toàn bộ khóa học C++', 1),
                                                             (4,  4,  'Toàn bộ khóa học Java Nâng Cao', 1),
                                                             (5,  5,  'Toàn bộ khóa học Cấu trúc Dữ liệu', 1),
                                                             (6,  6,  'Toàn bộ khóa học Spring Boot', 1),
                                                             (7,  7,  'Toàn bộ khóa học HTML CSS JS', 1),
                                                             (8,  8,  'Toàn bộ khóa học ReactJS', 1),
                                                             (9,  9,  'Toàn bộ khóa học Node.js Express', 1),
                                                             (10, 10, 'Toàn bộ khóa học UI/UX', 1),
                                                             (11, 11, 'Toàn bộ khóa học Figma', 1),
                                                             (12, 12, 'Toàn bộ khóa học UI Design Web', 1),
                                                             (13, 13, 'Toàn bộ khóa học Quản trị PostgreSQL', 1),
                                                             (14, 14, 'Toàn bộ khóa học SQL Cơ bản Nâng cao', 1),
                                                             (15, 15, 'Toàn bộ khóa học Database Design', 1),
                                                             (16, 16, 'Toàn bộ khóa học Spring Security', 1),
                                                             (17, 17, 'Toàn bộ khóa học Advanced PostgreSQL', 1);
SELECT setval('chapters_id_seq', (SELECT MAX(id) FROM chapters));

-- 5. Chèn dữ liệu Bài học (80 Lessons)
INSERT INTO lessons (course_id, chapter_id, title, content_url, lesson_type, duration_minutes, order_index) VALUES
                                                                                                                (1, 1, 'Bài 1: Giới thiệu ngôn ngữ Java & JDK', 'videos/java-lesson1.mp4', 'VIDEO', 30, 1),
                                                                                                                (1, 1, 'Bài 2: Biến, Kiểu dữ liệu & Toán tử',  'videos/java-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (1, 1, 'Bài 3: Lập trình Hướng đối tượng OOP',   'videos/java-lesson3.mp4', 'VIDEO', 60, 3),

                                                                                                                (2, 2, 'Bài 1: Giới thiệu Python và môi trường lập trình', 'videos/python-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (2, 2, 'Bài 2: Biến, kiểu dữ liệu và toán tử', 'videos/python-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (2, 2, 'Bài 3: Cấu trúc điều kiện và vòng lặp', 'videos/python-lesson3.mp4', 'VIDEO', 50, 3),
                                                                                                                (2, 2, 'Bài 4: Hàm và xử lý dữ liệu', 'videos/python-lesson4.mp4', 'VIDEO', 55, 4),
                                                                                                                (2, 2, 'Bài 5: Lập trình hướng đối tượng với Python', 'videos/python-lesson5.mp4', 'VIDEO', 60, 5),

                                                                                                                (3, 3, 'Bài 1: Thiết kế Wireframe với Figma', 'videos/uiux-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (3, 3, 'Bài 2: Sử dụng Grid System Bootstrap 5', 'videos/uiux-lesson2.mp4', 'VIDEO', 50, 2),

                                                                                                                (4, 4, 'Bài 1: Tổng quan Java nâng cao', 'videos/java-advanced-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (4, 4, 'Bài 2: Lambda Expression và Functional Interface', 'videos/java-advanced-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (4, 4, 'Bài 3: Stream API', 'videos/java-advanced-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (4, 4, 'Bài 4: Multithreading và Concurrency', 'videos/java-advanced-lesson4.mp4', 'VIDEO', 65, 4),
                                                                                                                (4, 4, 'Bài 5: Design Pattern trong Java', 'videos/java-advanced-lesson5.mp4', 'VIDEO', 70, 5),

                                                                                                                (5, 5, 'Bài 1: Tổng quan về cấu trúc dữ liệu', 'videos/dsa-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (5, 5, 'Bài 2: Array và Linked List', 'videos/dsa-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (5, 5, 'Bài 3: Stack và Queue', 'videos/dsa-lesson3.mp4', 'VIDEO', 50, 3),
                                                                                                                (5, 5, 'Bài 4: Tree và Binary Search Tree', 'videos/dsa-lesson4.mp4', 'VIDEO', 65, 4),
                                                                                                                (5, 5, 'Bài 5: Graph và các thuật toán tìm kiếm', 'videos/dsa-lesson5.mp4', 'VIDEO', 70, 5),

                                                                                                                (6, 6, 'Bài 1: Tổng quan Spring Boot', 'videos/springboot-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (6, 6, 'Bài 2: Spring MVC và RESTful API', 'videos/springboot-lesson2.mp4', 'VIDEO', 55, 2),
                                                                                                                (6, 6, 'Bài 3: Spring Data JPA', 'videos/springboot-lesson3.mp4', 'VIDEO', 60, 3),
                                                                                                                (6, 6, 'Bài 4: Kết nối PostgreSQL', 'videos/springboot-lesson4.mp4', 'VIDEO', 50, 4),
                                                                                                                (6, 6, 'Bài 5: Spring Security', 'videos/springboot-lesson5.mp4', 'VIDEO', 70, 5),

                                                                                                                (7, 7, 'Bài 1: Cấu trúc HTML5', 'videos/htmlcssjs-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (7, 7, 'Bài 2: CSS và Box Model', 'videos/htmlcssjs-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (7, 7, 'Bài 3: Flexbox và Responsive Design', 'videos/htmlcssjs-lesson3.mp4', 'VIDEO', 50, 3),
                                                                                                                (7, 7, 'Bài 4: JavaScript cơ bản', 'videos/htmlcssjs-lesson4.mp4', 'VIDEO', 55, 4),
                                                                                                                (7, 7, 'Bài 5: DOM và xử lý sự kiện', 'videos/htmlcssjs-lesson5.mp4', 'VIDEO', 60, 5),

                                                                                                                (8, 8, 'Bài 1: Giới thiệu ReactJS', 'videos/react-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (8, 8, 'Bài 2: Components và JSX', 'videos/react-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (8, 8, 'Bài 3: Props và State', 'videos/react-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (8, 8, 'Bài 4: React Hooks', 'videos/react-lesson4.mp4', 'VIDEO', 60, 4),
                                                                                                                (8, 8, 'Bài 5: Kết nối REST API', 'videos/react-lesson5.mp4', 'VIDEO', 65, 5),

                                                                                                                (9, 9, 'Bài 1: Giới thiệu Node.js', 'videos/nodejs-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (9, 9, 'Bài 2: Module và npm', 'videos/nodejs-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (9, 9, 'Bài 3: Express.js và Routing', 'videos/nodejs-lesson3.mp4', 'VIDEO', 50, 3),
                                                                                                                (9, 9, 'Bài 4: Middleware và Authentication', 'videos/nodejs-lesson4.mp4', 'VIDEO', 60, 4),
                                                                                                                (9, 9, 'Bài 5: Kết nối PostgreSQL', 'videos/nodejs-lesson5.mp4', 'VIDEO', 55, 5),

                                                                                                                (10, 10, 'Bài 1: Tổng quan về UI/UX', 'videos/uiux-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (10, 10, 'Bài 2: User Flow và Wireframe', 'videos/uiux-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (10, 10, 'Bài 3: Thiết kế giao diện với Figma', 'videos/uiux-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (10, 10, 'Bài 4: Typography và Color System', 'videos/uiux-lesson4.mp4', 'VIDEO', 50, 4),
                                                                                                                (10, 10, 'Bài 5: Responsive UI và Design System', 'videos/uiux-lesson5.mp4', 'VIDEO', 60, 5),

                                                                                                                (11, 11, 'Bài 1: Làm quen với Figma', 'videos/figma-lesson1.mp4', 'VIDEO', 30, 1),
                                                                                                                (11, 11, 'Bài 2: Frame, Layer và Component', 'videos/figma-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (11, 11, 'Bài 3: Auto Layout', 'videos/figma-lesson3.mp4', 'VIDEO', 50, 3),
                                                                                                                (11, 11, 'Bài 4: Prototype và Interaction', 'videos/figma-lesson4.mp4', 'VIDEO', 55, 4),
                                                                                                                (11, 11, 'Bài 5: Thiết kế một giao diện thực tế', 'videos/figma-lesson5.mp4', 'VIDEO', 70, 5),

                                                                                                                (12, 12, 'Bài 1: Nguyên tắc UI Design', 'videos/ui-design-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (12, 12, 'Bài 2: Grid System và Layout', 'videos/ui-design-lesson2.mp4', 'VIDEO', 45, 2),
                                                                                                                (12, 12, 'Bài 3: Typography và Spacing', 'videos/ui-design-lesson3.mp4', 'VIDEO', 45, 3),
                                                                                                                (12, 12, 'Bài 4: Component và Design System', 'videos/ui-design-lesson4.mp4', 'VIDEO', 55, 4),
                                                                                                                (12, 12, 'Bài 5: Thiết kế Dashboard thực tế', 'videos/ui-design-lesson5.mp4', 'VIDEO', 65, 5),

                                                                                                                (13, 13, 'Bài 1: Tổng quan PostgreSQL', 'videos/postgresql-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (13, 13, 'Bài 2: SQL và Query cơ bản', 'videos/postgresql-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (13, 13, 'Bài 3: Index và tối ưu truy vấn', 'videos/postgresql-lesson3.mp4', 'VIDEO', 60, 3),
                                                                                                                (13, 13, 'Bài 4: Transaction và Locking', 'videos/postgresql-lesson4.mp4', 'VIDEO', 55, 4),
                                                                                                                (13, 13, 'Bài 5: Backup và Recovery', 'videos/postgresql-lesson5.mp4', 'VIDEO', 60, 5),

                                                                                                                (14, 14, 'Bài 1: SELECT và WHERE', 'videos/sql-lesson1.mp4', 'VIDEO', 35, 1),
                                                                                                                (14, 14, 'Bài 2: JOIN và GROUP BY', 'videos/sql-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (14, 14, 'Bài 3: Subquery và CTE', 'videos/sql-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (14, 14, 'Bài 4: Window Function', 'videos/sql-lesson4.mp4', 'VIDEO', 60, 4),
                                                                                                                (14, 14, 'Bài 5: Tối ưu hóa truy vấn SQL', 'videos/sql-lesson5.mp4', 'VIDEO', 65, 5),

                                                                                                                (15, 15, 'Bài 1: Phân tích yêu cầu Database', 'videos/db-design-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (15, 15, 'Bài 2: ERD và thiết kế bảng', 'videos/db-design-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (15, 15, 'Bài 3: Chuẩn hóa dữ liệu', 'videos/db-design-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (15, 15, 'Bài 4: Index và tối ưu Database', 'videos/db-design-lesson4.mp4', 'VIDEO', 60, 4),
                                                                                                                (15, 15, 'Bài 5: Transaction và Performance', 'videos/db-design-lesson5.mp4', 'VIDEO', 65, 5),

                                                                                                                (16, 16, 'Bài 1: Tổng quan Spring Security', 'videos/spring-security-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (16, 16, 'Bài 2: Authentication', 'videos/spring-security-lesson2.mp4', 'VIDEO', 50, 2),
                                                                                                                (16, 16, 'Bài 3: Authorization và Role', 'videos/spring-security-lesson3.mp4', 'VIDEO', 55, 3),
                                                                                                                (16, 16, 'Bài 4: JWT Authentication', 'videos/spring-security-lesson4.mp4', 'VIDEO', 65, 4),
                                                                                                                (16, 16, 'Bài 5: Security Architecture', 'videos/spring-security-lesson5.mp4', 'VIDEO', 60, 5),

                                                                                                                (17, 17, 'Bài 1: PostgreSQL Query Planner', 'videos/postgresql-performance-lesson1.mp4', 'VIDEO', 40, 1),
                                                                                                                (17, 17, 'Bài 2: EXPLAIN và EXPLAIN ANALYZE', 'videos/postgresql-performance-lesson2.mp4', 'VIDEO', 55, 2),
                                                                                                                (17, 17, 'Bài 3: Indexing nâng cao', 'videos/postgresql-performance-lesson3.mp4', 'VIDEO', 60, 3),
                                                                                                                (17, 17, 'Bài 4: Query Optimization', 'videos/postgresql-performance-lesson4.mp4', 'VIDEO', 65, 4),
                                                                                                                (17, 17, 'Bài 5: Performance Tuning cho hệ thống lớn', 'videos/postgresql-performance-lesson5.mp4', 'VIDEO', 70, 5);

-- 6. Chèn dữ liệu Đăng ký học (3 Enrollments)
INSERT INTO enrollments (student_id, course_id, course_price, is_paid, progress_percent, status) VALUES
                                                                                                     (4, 1, 500000.00, TRUE, 65,  'ACTIVE'),
                                                                                                     (4, 3, 550000.00, TRUE, 100, 'COMPLETED'),
                                                                                                     (5, 1, 500000.00, TRUE, 20,  'ACTIVE');

-- 6B. Bổ sung đăng ký học cho student1 (user_id = 4)
INSERT INTO enrollments (student_id, course_id, course_price, is_paid, progress_percent, status) VALUES
                                                                                                     (4, 2, 450000.00, TRUE, 35, 'ACTIVE'),
                                                                                                     (4, 6, 750000.00, TRUE, 10, 'ACTIVE'),
                                                                                                     (4, 10, 400000.00, TRUE, 0, 'ACTIVE'),
                                                                                                     (4, 4, 650000.00, TRUE, 20, 'ACTIVE'),
                                                                                                     (4, 5, 600000.00, TRUE, 40, 'ACTIVE'),
                                                                                                     (4, 7, 400000.00, TRUE, 60, 'ACTIVE'),
                                                                                                     (4, 8, 700000.00, TRUE, 80, 'ACTIVE'),
                                                                                                     (4, 9, 650000.00, TRUE, 20, 'ACTIVE'),
                                                                                                     (4, 11, 350000.00, TRUE, 40, 'ACTIVE'),
                                                                                                     (4, 12, 500000.00, TRUE, 60, 'ACTIVE'),
                                                                                                     (4, 13, 600000.00, TRUE, 80, 'ACTIVE'),
                                                                                                     (4, 14, 450000.00, TRUE, 20, 'ACTIVE'),
                                                                                                     (4, 15, 700000.00, TRUE, 100, 'COMPLETED');

-- 7. Chèn dữ liệu Tiến độ bài học chi tiết (Lesson Progresses)
INSERT INTO lesson_progresses (user_id, lesson_id, completed, completed_at) VALUES
                                                                                (4, 1, TRUE, CURRENT_TIMESTAMP - INTERVAL '2 DAYS'),
                                                                                (4, 2, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 DAY'),
                                                                                (5, 1, TRUE, CURRENT_TIMESTAMP - INTERVAL '3 DAYS');

-- 7B. Bổ sung tiến độ bài học cho các khóa học mới của student1
INSERT INTO lesson_progresses (user_id, lesson_id, completed, completed_at) VALUES
                                                                                (4, 4, TRUE, CURRENT_TIMESTAMP - INTERVAL '2 DAYS'),
                                                                                (4, 5, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 DAY'),
                                                                                (4, 21, TRUE, CURRENT_TIMESTAMP - INTERVAL '12 HOURS'),
                                                                                (4, 11, TRUE, CURRENT_TIMESTAMP - INTERVAL '10 HOURS'),
                                                                                (4, 16, TRUE, CURRENT_TIMESTAMP - INTERVAL '9 HOURS'),
                                                                                (4, 26, TRUE, CURRENT_TIMESTAMP - INTERVAL '8 HOURS'),
                                                                                (4, 31, TRUE, CURRENT_TIMESTAMP - INTERVAL '7 HOURS'),
                                                                                (4, 36, TRUE, CURRENT_TIMESTAMP - INTERVAL '6 HOURS'),
                                                                                (4, 46, TRUE, CURRENT_TIMESTAMP - INTERVAL '5 HOURS'),
                                                                                (4, 51, TRUE, CURRENT_TIMESTAMP - INTERVAL '4 HOURS'),
                                                                                (4, 56, TRUE, CURRENT_TIMESTAMP - INTERVAL '3 HOURS'),
                                                                                (4, 61, TRUE, CURRENT_TIMESTAMP - INTERVAL '2 HOURS'),
                                                                                (4, 66, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 HOUR');

-- 8. Chèn dữ liệu Giao dịch thanh toán (3 Payments)
INSERT INTO payments (id, user_id, enrollment_id, payment_method, amount, status) VALUES
                                                                                      (1, 4, 1, 'VNPAY', 500000.00, 'SUCCESS'),
                                                                                      (2, 4, 2, 'SEPAY', 550000.00, 'SUCCESS'),
                                                                                      (3, 5, 3, 'BANK_TRANSFER', 500000.00, 'PENDING');
SELECT setval('payments_id_seq', (SELECT MAX(id) FROM payments));

-- 8B. Bổ sung giao dịch thanh toán cho các enrollment mới
INSERT INTO payments (id, user_id, enrollment_id, payment_method, amount, status) VALUES
                                                                                      (4, 4, 4, 'VNPAY', 450000.00, 'SUCCESS'),
                                                                                      (5, 4, 5, 'BANK_TRANSFER', 750000.00, 'SUCCESS'),
                                                                                      (6, 4, 6, 'SEPAY', 400000.00, 'SUCCESS'),
                                                                                      (7, 4, 7, 'VNPAY', 650000.00, 'SUCCESS'),
                                                                                      (8, 4, 8, 'BANK_TRANSFER', 600000.00, 'SUCCESS'),
                                                                                      (9, 4, 9, 'SEPAY', 400000.00, 'SUCCESS'),
                                                                                      (10, 4, 10, 'VNPAY', 700000.00, 'SUCCESS'),
                                                                                      (11, 4, 11, 'BANK_TRANSFER', 650000.00, 'SUCCESS'),
                                                                                      (12, 4, 12, 'SEPAY', 350000.00, 'SUCCESS'),
                                                                                      (13, 4, 13, 'VNPAY', 500000.00, 'SUCCESS'),
                                                                                      (14, 4, 14, 'BANK_TRANSFER', 600000.00, 'SUCCESS'),
                                                                                      (15, 4, 15, 'SEPAY', 450000.00, 'SUCCESS'),
                                                                                      (16, 4, 16, 'VNPAY', 700000.00, 'SUCCESS');
SELECT setval('payments_id_seq', (SELECT MAX(id) FROM payments));

-- 9. Chèn dữ liệu Đánh giá (3 Reviews)
INSERT INTO reviews (student_id, course_id, rating, comment) VALUES
                                                                 (4, 1, 5, 'Khóa học cực kỳ chi tiết và dễ hiểu, giảng viên hỗ trợ nhiệt tình!'),
                                                                 (4, 3, 5, 'Giao diện mẫu và ví dụ C++ áp dụng rất tốt vào bài tập thực tế.'),
                                                                 (5, 1, 4, 'Nội dung rất hay, phần OOP giải thích rõ ràng.');

-- 9B. Bổ sung đánh giá của student1
INSERT INTO reviews (student_id, course_id, rating, comment) VALUES
                                                                 (4, 2, 5, 'Nội dung Python dễ tiếp cận, ví dụ thực tế và bài giảng rõ ràng.'),
                                                                 (4, 6, 4, 'Spring Boot khá đầy đủ, phần REST API rất hữu ích.');

-- 10. Chèn dữ liệu Ngân hàng Câu hỏi (25 Questions)
INSERT INTO questions (id, chapter_id, content, explanation) VALUES
                                                                 (1,  1, 'Trong Java, từ khóa nào được dùng để khai báo một hằng số?', 'Từ khóa final dùng để khai báo hằng số trong Java.'),
                                                                 (2,  1, 'Phương thức nào là điểm bắt đầu (entry point) của một chương trình Java chuẩn?', 'main(String[] args) là hàm khởi chạy chính.'),
                                                                 (3,  1, 'Kiểu dữ liệu nào sau đây là kiểu dữ liệu nguyên thủy (primitive) trong Java?', 'int là kiểu dữ liệu nguyên thủy, Integer là Wrapper class.'),
                                                                 (4,  1, 'Trong OOP Java, tính chất nào cho phép một lớp con kế thừa thuộc tính và phương thức của lớp cha?', 'Tính kế thừa (Inheritance) dùng từ khóa extends.'),
                                                                 (5,  1, 'Từ khóa nào được dùng để tạo một đối tượng (instance) mới trong Java?', 'Từ khóa new dùng để cấp phát bộ nhớ cho đối tượng.'),
                                                                 (6,  1, 'Interface trong Java có thể chứa phương thức có body không (từ Java 8)?', 'Từ Java 8, Interface có thể chứa default method và static method có body.'),
                                                                 (7,  1, 'Ngoại lệ (Exception) nào xảy ra khi truy cập vào một tham chiếu đối tượng null?', 'NullPointerException xảy ra khi gọi phương thức trên đối tượng null.'),
                                                                 (8,  1, 'Gói (package) nào được tự động import vào mọi lớp Java mà không cần khai báo?', 'java.lang được tự động import.'),
                                                                 (9,  1, 'Trong Java Collections Framework, Collection nào KHÔNG cho phép phần tử trùng lặp?', 'Set không cho phép chứa phần tử trùng lặp.'),
                                                                 (10, 1, 'Từ khóa super trong Java được dùng để làm gì?', 'super dùng để truy cập phương thức/constructor của lớp cha.'),
                                                                 (11, 1, 'Vòng lặp nào trong Java đảm bảo khối lệnh được thực thi ít nhất 1 lần?', 'do-while kiểm tra điều kiện sau khi thực thi.'),
                                                                 (12, 1, 'Lớp nào trong Java được dùng để xử lý chuỗi có thể thay đổi (mutable)?', 'StringBuilder hoặc StringBuffer dùng để thao tác chuỗi mutable.'),
                                                                 (13, 1, 'Trong Java, từ khóa try-catch được dùng để làm gì?', 'Bắt và xử lý ngoại lệ (Exception Handling).'),
                                                                 (14, 1, 'Phương thức equals() trong class Object so sánh điều gì mặc định?', 'Mặc định so sánh địa chỉ ô nhớ (toán tử ==).'),
                                                                 (15, 1, 'Khái niệm Polymorphism trong Java có nghĩa là gì?', 'Tính đa hình: một hành động có thể thực hiện theo nhiều cách khác nhau.'),

                                                                 (16, 3, 'Con trỏ (Pointer) trong C++ dùng để lưu trữ cái gì?', 'Con trỏ lưu trữ địa chỉ ô nhớ của biến khác.'),
                                                                 (17, 3, 'Toán tử nào được dùng để giải tham chiếu (dereference) một con trỏ trong C++?', 'Toán tử * dùng để lấy giá trị tại địa chỉ con trỏ trỏ tới.'),
                                                                 (18, 3, 'Từ khóa nào dùng để cấp phát bộ nhớ động trong C++?', 'Toán tử new cấp phát bộ nhớ động trên Heap.'),
                                                                 (19, 3, 'Hàm hủy (Destructor) trong một lớp C++ có tên bắt đầu bằng ký tự nào?', 'Dấu tilde ~ đứng trước tên class để khai báo destructor.'),
                                                                 (20, 3, 'Thư viện nào chuẩn trong C++ cung cấp các hàm nhập xuất như cin, cout?', 'iosteam chứa cin và cout.'),
                                                                 (21, 3, 'Nạp chồng hàm (Function Overloading) trong C++ dựa vào điều gì để phân biệt?', 'Số lượng và kiểu dữ liệu của các tham số truyền vào.'),
                                                                 (22, 3, 'Từ khóa virtual trong C++ được sử dụng để làm gì?', 'Khai báo hàm ảo để hỗ trợ tính đa hình động (Dynamic Binding).'),
                                                                 (23, 3, 'Trong C++, tham chiếu (Reference) khác con trỏ ở điểm nào chính?', 'Tham chiếu không thể null và không thể trỏ lại sau khi khởi tạo.'),
                                                                 (24, 3, 'Cấu trúc Vector trong C++ thuộc thư viện nào?', 'Standard Template Library (STL).'),
                                                                 (25, 3, 'Lệnh delete[] trong C++ được dùng để làm gì?', 'Giải phóng mảng bộ nhớ động đã cấp phát bằng new[].');
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));

-- 11. Chèn dữ liệu Đáp án lựa chọn (100 Answer Options)
INSERT INTO answer_options (id, question_id, content, is_correct) VALUES
                                                                      (1,  1, 'final', TRUE),  (2,  1, 'static', FALSE), (3,  1, 'const', FALSE),  (4,  1, 'immutable', FALSE),
                                                                      (5,  2, 'public static void main(String[] args)', TRUE), (6,  2, 'public void start()', FALSE), (7,  2, 'public static void init()', FALSE), (8,  2, 'void main()', FALSE),
                                                                      (9,  3, 'int', TRUE),    (10, 3, 'Integer', FALSE), (11, 3, 'String', FALSE), (12, 3, 'Array', FALSE),
                                                                      (13, 4, 'Inheritance', TRUE), (14, 4, 'Encapsulation', FALSE), (15, 4, 'Polymorphism', FALSE), (16, 4, 'Abstraction', FALSE),
                                                                      (17, 5, 'new', TRUE),    (18, 5, 'create', FALSE), (19, 5, 'make', FALSE),   (20, 5, 'build', FALSE),
                                                                      (21, 6, 'Có (với default và static method)', TRUE), (22, 6, 'Không bao giờ', FALSE), (23, 6, 'Chỉ với private method', FALSE), (24, 6, 'Chỉ trong abstract class', FALSE),
                                                                      (25, 7, 'NullPointerException', TRUE), (26, 7, 'ClassNotFoundException', FALSE), (27, 7, 'ArrayIndexOutOfBoundsException', FALSE), (28, 7, 'ArithmeticException', FALSE),
                                                                      (29, 8, 'java.lang', TRUE), (30, 8, 'java.util', FALSE), (31, 8, 'java.io', FALSE), (32, 8, 'java.net', FALSE),
                                                                      (33, 9, 'Set', TRUE),    (34, 9, 'List', FALSE),   (35, 9, 'ArrayList', FALSE), (36, 9, 'Vector', FALSE),
                                                                      (37, 10, 'Gọi phương thức/constructor của lớp cha', TRUE), (38, 10, 'Tạo đối tượng mới', FALSE), (39, 10, 'Khai báo hằng số', FALSE), (40, 10, 'Kết thúc hàm', FALSE),
                                                                      (41, 11, 'do-while', TRUE), (42, 11, 'while', FALSE), (43, 11, 'for', FALSE), (44, 11, 'foreach', FALSE),
                                                                      (45, 12, 'StringBuilder', TRUE), (46, 12, 'String', FALSE), (47, 12, 'Char', FALSE), (48, 12, 'Text', FALSE),
                                                                      (49, 13, 'Bắt và xử lý ngoại lệ', TRUE), (50, 13, 'Khai báo hàm', FALSE), (51, 13, 'Tối ưu bộ nhớ', FALSE), (52, 13, 'Đọc ghi file', FALSE),
                                                                      (53, 14, 'Địa chỉ ô nhớ', TRUE), (54, 14, 'Nội dung thuộc tính', FALSE), (55, 14, 'Kích thước đối tượng', FALSE), (56, 14, 'Tên class', FALSE),
                                                                      (57, 15, 'Tính đa hình', TRUE), (58, 15, 'Tính đóng gói', FALSE), (59, 15, 'Tính trừu tượng', FALSE), (60, 15, 'Tính kế thừa', FALSE),

                                                                      (61, 16, 'Địa chỉ ô nhớ', TRUE), (62, 16, 'Giá trị hằng số', FALSE), (63, 16, 'Kích thước mảng', FALSE), (64, 16, 'Tên hàm', FALSE),
                                                                      (65, 17, 'Toán tử *', TRUE), (66, 17, 'Toán tử &', FALSE), (67, 17, 'Toán tử ->', FALSE), (68, 17, 'Toán tử ::', FALSE),
                                                                      (69, 18, 'new', TRUE), (70, 18, 'malloc', FALSE), (71, 18, 'alloc', FALSE), (72, 18, 'create', FALSE),
                                                                      (73, 19, 'Ký tự ~', TRUE), (74, 19, 'Ký tự !', FALSE), (75, 19, 'Ký tự #', FALSE), (76, 19, 'Ký tự $', FALSE),
                                                                      (77, 20, 'iostream', TRUE), (78, 20, 'stdio.h', FALSE), (79, 20, 'conio.h', FALSE), (80, 20, 'stdlib.h', FALSE),
                                                                      (81, 21, 'Danh sách và kiểu dữ liệu tham số', TRUE), (82, 21, 'Kiểu dữ liệu trả về', FALSE), (83, 21, 'Tên hàm', FALSE), (84, 21, 'Phạm vi truy cập', FALSE),
                                                                      (85, 22, 'Đa hình động (Virtual Function)', TRUE), (86, 22, 'Cấp phát bộ nhớ', FALSE), (87, 22, 'Hằng số', FALSE), (88, 22, 'Ghi đè thuộc tính', FALSE),
                                                                      (89, 23, 'Không thể null và không thể trỏ lại', TRUE), (90, 23, 'Có dung lượng nhỏ hơn', FALSE), (91, 23, 'Tốc độ chậm hơn', FALSE), (92, 23, 'Chỉ dùng cho kiểu int', FALSE),
                                                                      (93, 24, 'STL (Standard Template Library)', TRUE), (94, 24, 'Boost', FALSE), (95, 24, 'Qt Framework', FALSE), (96, 24, 'POSIX', FALSE),
                                                                      (97, 25, 'Giải phóng mảng bộ nhớ động', TRUE), (98, 25, 'Xóa một phần tử', FALSE), (99, 25, 'Hủy toàn bộ chương trình', FALSE), (100, 25, 'Xóa ô nhớ con trỏ null', FALSE);
SELECT setval('answer_options_id_seq', (SELECT MAX(id) FROM answer_options));

-- 12. Chèn dữ liệu Bài kiểm tra (2 Quizzes)
INSERT INTO quizzes (id, course_id, title, total_questions, pass_score, time_limit_min) VALUES
                                                                                            (1, 1, 'Bài kiểm tra Kiến thức Java Cơ Bản', 15, 70, 30),
                                                                                            (2, 3, 'Bài đánh giá Kỹ năng C++ và Tư duy Lập trình', 10, 80, 25);
SELECT setval('quizzes_id_seq', (SELECT MAX(id) FROM quizzes));

-- 13. Chèn dữ liệu Cấu hình Random theo Chương (2 Quiz Chapters)
INSERT INTO quiz_chapters (id, quiz_id, chapter_id, question_count) VALUES
                                                                        (1, 1, 1, 15),
                                                                        (2, 2, 3, 10);
SELECT setval('quiz_chapters_id_seq', (SELECT MAX(id) FROM quiz_chapters));

-- 14. Chèn dữ liệu Lượt làm bài mẫu (1 Quiz Attempt cho student1)
INSERT INTO quiz_attempts (id, user_id, quiz_id, score, is_passed, started_at, submitted_at) VALUES
    (1, 4, 1, 87, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 HOUR', CURRENT_TIMESTAMP - INTERVAL '35 MINUTES');
SELECT setval('quiz_attempts_id_seq', (SELECT MAX(id) FROM quiz_attempts));

-- 15. Cố định danh sách 15 câu hỏi đã random cho Lượt làm bài 1
INSERT INTO quiz_attempt_questions (attempt_id, question_id) VALUES
                                                                 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15);

-- 16. Chèn dữ liệu Câu trả lời mẫu của học viên (15 Quiz Answers)
INSERT INTO quiz_answers (attempt_id, question_id, option_id) VALUES
                                                                  (1, 1,  1),
                                                                  (1, 2,  5),
                                                                  (1, 3,  9),
                                                                  (1, 4,  13),
                                                                  (1, 5,  17),
                                                                  (1, 6,  21),
                                                                  (1, 7,  25),
                                                                  (1, 8,  29),
                                                                  (1, 9,  33),
                                                                  (1, 10, 37),
                                                                  (1, 11, 41),
                                                                  (1, 12, 45),
                                                                  (1, 13, 49),
                                                                  (1, 14, 54),
                                                                  (1, 15, 57);