package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.UserDTO;
import com.lms.entity.Option;
import com.lms.entity.Question;
import com.lms.entity.Chapter;
import com.lms.service.CourseService;
import com.lms.service.QuestionService;
import com.lms.service.ExpertQuizService;
import com.lms.service.ServiceException;
import com.lms.dto.QuizDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import com.alibaba.excel.EasyExcel;
import com.lms.dto.QuestionExcelModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing question pool (Expert role).
 * URL mappings:
 *   GET  /expert/questions        -> Show course & chapter selection and list of questions
 *   GET  /expert/questions/new    -> Show blank form to create question for a chapter
 *   POST /expert/questions/new    -> Create a question
 *   GET  /expert/questions/detail -> Show form prefilled with question details
 *   POST /expert/questions/detail -> Update a question
 *   POST /expert/questions/delete -> Delete a question from the pool
 *   GET  /expert/questions/template -> Download Excel import template
 *   GET  /expert/questions/export   -> Export questions of current chapter to Excel
 *   POST /expert/questions/import   -> Import questions from Excel file
 */
@WebServlet({
    "/expert/questions", 
    "/expert/questions/new", 
    "/expert/questions/detail", 
    "/expert/questions/delete",
    "/expert/questions/template",
    "/expert/questions/import",
    "/expert/questions/export"
})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class ExpertQuestionServlet extends HttpServlet {

    private final QuestionService questionService = new QuestionService();
    private final CourseService courseService = new CourseService();
    private final ExpertQuizService expertQuizService = new ExpertQuizService();
    private static final int PAGE_SIZE = 10; // 10 questions per page

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/expert/questions":
                    list(req, resp);
                    break;
                case "/expert/questions/new":
                    showNewForm(req, resp);
                    break;
                case "/expert/questions/detail":
                    detail(req, resp);
                    break;
                case "/expert/questions/template":
                    downloadTemplate(resp);
                    break;
                case "/expert/questions/export":
                    exportQuestions(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/expert/questions/new":
                    create(req, resp);
                    break;
                case "/expert/questions/detail":
                    update(req, resp);
                    break;
                case "/expert/questions/delete":
                    delete(req, resp);
                    break;
                case "/expert/questions/import":
                    importQuestions(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private int currentExpertId(HttpServletRequest req) {
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        return currentUser.getId();
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, ServiceException {
        int expertId = currentExpertId(req);

        // 1. Get all courses owned by this expert for the selection dropdown
        List<CourseDTO> courses = courseService.listByExpert(expertId);
        req.setAttribute("courses", courses);

        // 2. Determine selected course
        String courseIdParam = req.getParameter("courseId");
        int selectedCourseId = -1;

        if (courseIdParam != null && !courseIdParam.trim().isEmpty()) {
            try {
                selectedCourseId = Integer.parseInt(courseIdParam);
            } catch (NumberFormatException ignored) {}
        } else if (!courses.isEmpty()) {
            selectedCourseId = courses.get(0).getId();
        }
        req.setAttribute("selectedCourseId", selectedCourseId);

        // 3. Load chapters for selected course
        List<Chapter> chapters = new ArrayList<>();
        int selectedChapterId = -1;

        if (selectedCourseId != -1) {
            chapters = questionService.getChaptersForCourse(selectedCourseId, expertId);
            req.setAttribute("chapters", chapters);

            String chapterIdParam = req.getParameter("chapterId");
            if (chapterIdParam != null && !chapterIdParam.trim().isEmpty()) {
                try {
                    selectedChapterId = Integer.parseInt(chapterIdParam);
                } catch (NumberFormatException ignored) {}
            } else if (!chapters.isEmpty()) {
                selectedChapterId = chapters.get(0).getId();
            }
        }
        req.setAttribute("selectedChapterId", selectedChapterId);

        // 4. Load paginated questions if a valid chapter is selected
        if (selectedChapterId != -1) {
            String keyword = req.getParameter("keyword");
            
            // Read page parameter
            int page = 1;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                } catch (NumberFormatException ignored) {}
            }
            
            int totalCount = questionService.getTotalQuestionsCount(selectedChapterId, expertId, keyword);
            int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
            if (totalPages > 0 && page > totalPages) {
                page = totalPages;
            }
            
            List<Question> questions = questionService.getQuestionsPaginated(selectedChapterId, expertId, keyword, page, PAGE_SIZE);
            
            req.setAttribute("keyword", keyword);
            req.setAttribute("questions", questions);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", totalCount);
        }

        // 5. Load Quizzes for selected course (integrated Tab 2)
        if (selectedCourseId != -1) {
            List<QuizDTO> quizzes = expertQuizService.getQuizzesByCourse(selectedCourseId, expertId);
            req.setAttribute("quizzes", quizzes);
        }

        req.getRequestDispatcher("/WEB-INF/views/expert/question-list.jsp").forward(req, resp);
    }

    private void showNewForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, ServiceException {
        int expertId = currentExpertId(req);
        int chapterId = Integer.parseInt(req.getParameter("chapterId"));
        Chapter chapter = questionService.getChapterById(chapterId, expertId);
        CourseDTO course = courseService.getCourseById(chapter.getCourseId());

        req.setAttribute("chapter", chapter);
        req.setAttribute("course", course);
        req.getRequestDispatcher("/WEB-INF/views/expert/question-form.jsp").forward(req, resp);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        int expertId = currentExpertId(req);
        int chapterId = Integer.parseInt(req.getParameter("chapterId"));

        Question q = new Question();
        q.setChapterId(chapterId);
        q.setContent(req.getParameter("content"));
        q.setExplanation(req.getParameter("explanation"));

        // Options collection
        List<Option> opts = new ArrayList<>();
        String[] optionTexts = req.getParameterValues("optionText");
        String correctIndexStr = req.getParameter("correctIndex");
        int correctIndex = (correctIndexStr != null) ? Integer.parseInt(correctIndexStr) : -1;

        if (optionTexts != null) {
            for (int i = 0; i < optionTexts.length; i++) {
                Option opt = new Option();
                opt.setOptionText(optionTexts[i]);
                opt.setCorrect(i == correctIndex);
                opts.add(opt);
            }
        }
        q.setOptions(opts);

        try {
            questionService.addQuestion(q, expertId);
            resp.sendRedirect(req.getContextPath() + "/expert/questions?chapterId=" + chapterId);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formData", q);
            try {
                Chapter chapter = questionService.getChapterById(chapterId, expertId);
                req.setAttribute("chapter", chapter);
                req.setAttribute("course", courseService.getCourseById(chapter.getCourseId()));
            } catch (ServiceException ignored) {}
            req.getRequestDispatcher("/WEB-INF/views/expert/question-form.jsp").forward(req, resp);
        }
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, ServiceException {
        int expertId = currentExpertId(req);
        int id = Integer.parseInt(req.getParameter("id"));
        Question q = questionService.getQuestionById(id, expertId);
        Chapter chapter = questionService.getChapterById(q.getChapterId(), expertId);
        CourseDTO course = courseService.getCourseById(chapter.getCourseId());

        req.setAttribute("question", q);
        req.setAttribute("chapter", chapter);
        req.setAttribute("course", course);
        req.getRequestDispatcher("/WEB-INF/views/expert/question-detail.jsp").forward(req, resp);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, ServiceException {
        int expertId = currentExpertId(req);
        int id = Integer.parseInt(req.getParameter("id"));

        Question q = new Question();
        q.setId(id);
        q.setContent(req.getParameter("content"));
        q.setExplanation(req.getParameter("explanation"));

        // Options collection
        List<Option> opts = new ArrayList<>();
        String[] optionTexts = req.getParameterValues("optionText");
        String correctIndexStr = req.getParameter("correctIndex");
        int correctIndex = (correctIndexStr != null) ? Integer.parseInt(correctIndexStr) : -1;

        if (optionTexts != null) {
            for (int i = 0; i < optionTexts.length; i++) {
                Option opt = new Option();
                opt.setOptionText(optionTexts[i]);
                opt.setCorrect(i == correctIndex);
                opts.add(opt);
            }
        }
        q.setOptions(opts);

        try {
            questionService.updateQuestion(q, expertId);
            Question updated = questionService.getQuestionById(id, expertId);
            resp.sendRedirect(req.getContextPath() + "/expert/questions?chapterId=" + updated.getChapterId());
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("question", q);
            try {
                Question original = questionService.getQuestionById(id, expertId);
                Chapter chapter = questionService.getChapterById(original.getChapterId(), expertId);
                req.setAttribute("chapter", chapter);
                req.setAttribute("course", courseService.getCourseById(chapter.getCourseId()));
            } catch (ServiceException ignored) {}
            req.getRequestDispatcher("/WEB-INF/views/expert/question-detail.jsp").forward(req, resp);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, ServiceException {
        int expertId = currentExpertId(req);
        String idParam = req.getParameter("id");
        String chapterIdParam = req.getParameter("chapterId");

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int questionId = Integer.parseInt(idParam);
                questionService.deleteQuestion(questionId, expertId);
            } catch (NumberFormatException ignored) {}
        }

        // Redirect back to list of the same chapter
        String redirectUrl = req.getContextPath() + "/expert/questions";
        if (chapterIdParam != null && !chapterIdParam.trim().isEmpty()) {
            redirectUrl += "?chapterId=" + chapterIdParam;
        }
        resp.sendRedirect(redirectUrl);
    }

    private void downloadTemplate(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=question_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = resp.getOutputStream()) {
            Sheet sheet = workbook.createSheet("Template Nhập Câu Hỏi");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header Row
            Row header = sheet.createRow(0);
            String[] columns = {"STT", "Nội dung câu hỏi", "Giải thích đáp án", "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D", "Đáp án đúng"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample MCQ Row
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(1);
            row1.createCell(1).setCellValue("Hệ quản trị cơ sở dữ liệu nào dưới đây là cơ sở dữ liệu quan hệ?");
            row1.createCell(2).setCellValue("PostgreSQL là CSDL quan hệ (RDBMS), các hệ khác là NoSQL.");
            row1.createCell(3).setCellValue("PostgreSQL");
            row1.createCell(4).setCellValue("MongoDB");
            row1.createCell(5).setCellValue("Redis");
            row1.createCell(6).setCellValue("Cassandra");
            row1.createCell(7).setCellValue("A"); // Correct option is A

            // Sample True/False Row
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue(2);
            row2.createCell(1).setCellValue("Java là ngôn ngữ lập trình hướng đối tượng. Đúng hay Sai?");
            row2.createCell(2).setCellValue("Java được thiết kế là ngôn ngữ hướng đối tượng hoàn toàn.");
            row2.createCell(3).setCellValue("Đúng");
            row2.createCell(4).setCellValue("Sai");
            row2.createCell(5).setCellValue("");
            row2.createCell(6).setCellValue("");
            row2.createCell(7).setCellValue("A"); // Correct option is A (Đúng)

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private void exportQuestions(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int expertId = currentExpertId(req);
        int chapterId = Integer.parseInt(req.getParameter("chapterId"));

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=questions_chapter_" + chapterId + ".xlsx");

        List<Question> list = questionService.getQuestionsForChapter(chapterId, expertId);

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = resp.getOutputStream()) {
            Sheet sheet = workbook.createSheet("Questions");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header Row
            Row header = sheet.createRow(0);
            String[] columns = {"STT", "Nội dung câu hỏi", "Giải thích đáp án", "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D", "Đáp án đúng"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Question q : list) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowIndex - 1);
                row.createCell(1).setCellValue(q.getContent());
                row.createCell(2).setCellValue(q.getExplanation());

                List<Option> opts = q.getOptions();
                char correctChar = 'A';
                for (int i = 0; i < 4; i++) {
                    if (i < opts.size()) {
                        Option opt = opts.get(i);
                        row.createCell(3 + i).setCellValue(opt.getOptionText());
                        if (opt.isCorrect()) {
                            correctChar = (char) ('A' + i);
                        }
                    } else {
                        row.createCell(3 + i).setCellValue("");
                    }
                }
                row.createCell(7).setCellValue(String.valueOf(correctChar));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private void importQuestions(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int expertId = currentExpertId(req);
        int chapterId = Integer.parseInt(req.getParameter("chapterId"));

        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            req.getSession().setAttribute("errorMsg", "Vui lòng chọn file Excel hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/expert/questions?chapterId=" + chapterId);
            return;
        }

        try (InputStream in = filePart.getInputStream()) {
            List<QuestionExcelModel> excelList = EasyExcel.read(in)
                    .head(QuestionExcelModel.class)
                    .sheet()
                    .doReadSync();

            List<Question> importedList = new ArrayList<>();
            for (QuestionExcelModel data : excelList) {
                if (data.getContent() == null || data.getContent().trim().isEmpty()) {
                    continue;
                }

                Question q = new Question();
                q.setChapterId(chapterId);
                q.setContent(data.getContent().trim());
                q.setExplanation(data.getExplanation() != null ? data.getExplanation().trim() : "");

                List<Option> opts = new ArrayList<>();
                String correctStr = data.getCorrectOption() != null ? data.getCorrectOption().trim().toUpperCase() : "";

                String[] optionTexts = {
                    data.getOptionA(),
                    data.getOptionB(),
                    data.getOptionC(),
                    data.getOptionD()
                };

                for (int j = 0; j < 4; j++) {
                    String optText = optionTexts[j];
                    if (optText != null && !optText.trim().isEmpty()) {
                        Option opt = new Option();
                        opt.setOptionText(optText.trim());

                        boolean isCorrect = false;
                        if (correctStr.equals(String.valueOf((char)('A' + j))) || 
                            correctStr.equals(opt.getOptionText().toUpperCase())) {
                            isCorrect = true;
                        }
                        opt.setCorrect(isCorrect);
                        opts.add(opt);
                    }
                }
                q.setOptions(opts);
                importedList.add(q);
            }

            for (Question q : importedList) {
                questionService.addQuestion(q, expertId);
            }
            req.getSession().setAttribute("successMsg", "Import thành công " + importedList.size() + " câu hỏi!");
        } catch (Exception e) {
            req.getSession().setAttribute("errorMsg", "Lỗi định dạng file hoặc dữ liệu trong file: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/expert/questions?chapterId=" + chapterId);
    }
}
