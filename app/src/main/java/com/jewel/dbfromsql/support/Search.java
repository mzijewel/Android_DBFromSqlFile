package com.jewel.dbfromsql.support;

/**
 * Created by Jewel on 11/7/2016.
 */

public class Search {
    private String field, value, operation;

    public Search(String field, String value, String operation) {
        this.field = field;
        this.value = value;
        this.operation = operation;
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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}