package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface KanbanMapper {
    List<TestCaseSummaryDTO> getSummary();

    TestPlanReportExtDTO queryTestPlanInfoByReportId(String testPlanReportId);

    String queryTestPlanReportIdByScenarioReportId(String scenarioReportId);

    TestCaseExtDTO queryTestCaseInfoBySourceId(String sourceId);

    List<ProjectIdAndCount> countByProjectIDAndCreateInThisWeek(long firstDayTimestamp, long lastDayTimestamp);

    List<ProjectIdAndCount> countByProjectIDAndTagAndCreateInThisWeek(String tag,long firstDayTimestamp, long lastDayTimestamp);

    List<ProjectIdAndCountGroup> countByProjectIDAndTagAndCreateInThisWeekP4();

    List<ProjectIdAndCountGroup> countByProjectIDAndTagAndCreateInThisWeekP0();

    List<ProjectIdAndCount> testCaseCountByProjectIDAndCreateInThisWeek(long firstDayTimestamp, long lastDayTimestamp);

    List<ProjectIdAndCount> countByProjectIDAndCreatInThisWeek(long firstDayTimestamp, long lastDayTimestamp);

    List<TestPlanIdAndCount> getTestPlanIdAndCount(String name);

    List<TestPlanIdAndCount> getTestPlanIdAndWeeklyCount(String name);

    List<PlanIdAndStatus> getAllTestPlanTestCase();

    List<PlanIdAndStatus> getAllTestPlanApiCase();

    List<PlanIdAndStatus> getAllTestPlanApiScenario();

    List<PlanIdAndStatus> getAllTestPlanLoadCase();
}
