package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExtractRequestBody {
    public List<String> data; // 省略JasonProperty 因为变量名与key相同

    @JsonProperty("max_keywords")
    public int maxKeywords; // java命名规则

    public ExtractRequestBody(List<String> data, int maxKeywords) {
        this.data = data;
        this.maxKeywords = maxKeywords;
    }
}
