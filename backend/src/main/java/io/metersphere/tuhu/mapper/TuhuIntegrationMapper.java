package io.metersphere.tuhu.mapper;

import io.metersphere.base.domain.TestPlan;

import java.util.List;

public interface TuhuIntegrationMapper {
    String getOrgIdByName(String orgName);

    String getWSIdByName(String wsName);

    List<String> getProjIdByName(String projName);

    List<TestPlan> getPlanByName(String planName);

    List<String> getPlanIdByName(String planName);

    List<String> getPlanIDs();
}
