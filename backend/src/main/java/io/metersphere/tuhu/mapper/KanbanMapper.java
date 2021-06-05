package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.TestCaseExtDTO;
import io.metersphere.tuhu.dto.TestPlanReportExtDTO;
import io.metersphere.tuhu.dto.TestCaseSummaryDTO;
import java.util.List;

public interface KanbanMapper {
    List<TestCaseSummaryDTO> getSummary();

    TestPlanReportExtDTO queryTestPlanInfoByReportId(String testPlanReportId);

    String queryTestPlanReportIdByScenarioReportId(String scenarioReportId);

    TestCaseExtDTO queryTestCaseInfoBySourceId(String sourceId);
}
