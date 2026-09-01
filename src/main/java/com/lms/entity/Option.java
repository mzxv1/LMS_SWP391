package com.lms.entity;

/** Persistence entity mapped to the "options" table. */
public class Option {

    private int id;
    private int questionId;
    private String optionText;
    private boolean correct;

    public Option() {
    }

    public Option(String optionText, boolean correct) {
        this.optionText = optionText;
        this.correct = correct;
    }

    public Option(int id, int questionId, String optionText, boolean correct) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
        this.correct = correct;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
