package com.picbed.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BatchDeleteRequest {

    @NotEmpty
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
