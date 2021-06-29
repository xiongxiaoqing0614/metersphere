package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.OrgAndWsInfoDTO;

import java.util.List;

public interface TuhuAllAppIdMapper {
    OrgAndWsInfoDTO queryOrgAndWsInfoByName(String orgName, String wsName);

    int deleteByOrgIdAndWsId(OrgAndWsInfoDTO orgAndWsInfoDTO);

    int addByOrgIdAndWsId(List<OrgAndWsInfoDTO> orgAndWsInfoDTOList);
}
