package io.metersphere.tuhu.mapper;

import io.metersphere.base.domain.TestPlan;

import java.util.List;

public interface TuhuIntegrationMapper {
    String getOrgIdByName(String orgName);

    List<String> getWsIdsByName(String wsName);

    String getWsIdByNameOrgId(String wsName, String orgId);

    List<String> getProjIdsByName(String projName);

    String getProjIdByNameWsId(String projName, String wsId);

    List<TestPlan> getPlansByName(String planName);

    List<String> getPlanIdsByName(String planName);

    String getPlanIdByNameProjId(String planName, String projId);

    String getOrgIdByWsId(String wsId);

    String getUserIdByName(String userName);

}
