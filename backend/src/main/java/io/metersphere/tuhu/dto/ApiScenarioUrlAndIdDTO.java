package io.metersphere.tuhu.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiScenarioUrlAndIdDTO {
    private List<String> urlList;
    private List<String> idList;
}
