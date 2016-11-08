package com.jewel.dbmanager.support;

/**
 * Created by Jewel on 11/7/2016.
 */

public class Search {
    public static final String EQUAL="=";
    public static final String NOT_EQUAL="!=";
    public static final String GREATER=">";
    public static final String LESS="<";
    public static final String AND=" AND ";
    public static final String OR=" OR ";


    private String field, value, operator;

    public Search(String field, String value, String operator) {
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}