package io.metersphere.tuhu.controller;
import lombok.Data;

@Data
public class NamesRequest {
    private String orgName;
    private String wsName;
    private String projName;
    private String planName;
}
