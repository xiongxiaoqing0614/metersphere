package io.metersphere.tuhu.dto;

import io.metersphere.base.domain.TestCase;
import lombok.Data;

@Data
public class TestCaseExtDTO extends TestCase {
    private String apiName;
    private String apiDesc;
}
