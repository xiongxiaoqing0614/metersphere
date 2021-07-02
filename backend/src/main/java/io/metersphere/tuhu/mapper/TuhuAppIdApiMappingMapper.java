package io.metersphere.tuhu.mapper;

import io.metersphere.tuhu.dto.OrgAndWsInfoDTO;
import io.metersphere.tuhu.dto.TuhuAppIdApiMappingDTO;

import java.util.List;

public interface TuhuAppIdApiMappingMapper {
    int deleteByAppId(String appId);

    int addByAppId(List<TuhuAppIdApiMappingDTO> tuhuAppIdApiMappingDTO);
}
