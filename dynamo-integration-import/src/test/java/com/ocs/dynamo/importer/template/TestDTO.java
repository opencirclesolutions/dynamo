package com.ocs.dynamo.importer.template;

import com.ocs.dynamo.importer.ImportField;
import com.ocs.dynamo.importer.dto.AbstractDTO;

public class TestDTO extends AbstractDTO {

    @ImportField(index = 0)
    private int first;

    @ImportField(index = 1)
    private String second;

    private static final long serialVersionUID = -7497484411658589226L;

    public TestDTO() {
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}
