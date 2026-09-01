package com.lms.dto;

public class QuizChapterDTO {
    private int chapterId;
    private String chapterName;
    private int questionCount;
    private int maxQuestionsAvailable;

    public QuizChapterDTO() {
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getMaxQuestionsAvailable() {
        return maxQuestionsAvailable;
    }

    public void setMaxQuestionsAvailable(int maxQuestionsAvailable) {
        this.maxQuestionsAvailable = maxQuestionsAvailable;
    }
}
