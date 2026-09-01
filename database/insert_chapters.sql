-- ============================================================
-- Script chèn tự động 5 chương học cho mỗi khóa học hiện có
-- Đường dẫn: database/insert_chapters.sql
-- Hệ quản trị cơ sở dữ liệu: PostgreSQL
-- ============================================================

DO $$
DECLARE
    r RECORD;
    ch_title VARCHAR(200);
BEGIN
    -- Lặp qua từng khóa học hiện có trong bảng courses
    FOR r IN SELECT id, title FROM courses LOOP
        -- Thêm 5 chương học với nội dung thực tế tương ứng với từng khóa học
        FOR i IN 1..5 LOOP
            IF i = 1 THEN
                ch_title := 'Chương 1: Giới thiệu và chuẩn bị môi trường - ' || r.title;
            ELSIF i = 2 THEN
                ch_title := 'Chương 2: Kiến thức nền tảng và cốt lõi - ' || r.title;
            ELSIF i = 3 THEN
                ch_title := 'Chương 3: Thực hành xây dựng ứng dụng thực tế - ' || r.title;
            ELSIF i = 4 THEN
                ch_title := 'Chương 4: Kỹ thuật nâng cao và tối ưu hóa - ' || r.title;
            ELSE
                ch_title := 'Chương 5: Tổng kết và định hướng phát triển - ' || r.title;
            END IF;
            
            -- Chèn vào bảng chapters
            INSERT INTO chapters (course_id, title, order_index)
            VALUES (r.id, LEFT(ch_title, 200), i + 1);
        END LOOP;
    END LOOP;
END $$;
