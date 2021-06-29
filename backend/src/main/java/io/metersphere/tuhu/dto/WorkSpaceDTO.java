package io.metersphere.tuhu.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkSpaceDTO {
    private String id;
    private String name;
    private List<String> appIdList;
}
