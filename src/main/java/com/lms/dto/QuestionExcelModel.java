package com.lms.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class QuestionExcelModel {

    @ExcelProperty(index = 1)
    private String content;

    @ExcelProperty(index = 2)
    private String explanation;

    @ExcelProperty(index = 3)
    private String optionA;

    @ExcelProperty(index = 4)
    private String optionB;

    @ExcelProperty(index = 5)
    private String optionC;

    @ExcelProperty(index = 6)
    private String optionD;

    @ExcelProperty(index = 7)
    private String correctOption;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
