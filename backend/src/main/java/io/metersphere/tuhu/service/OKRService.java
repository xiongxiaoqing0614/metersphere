package io.metersphere.tuhu.service;

import io.metersphere.tuhu.dto.TestCaseAllInfoDTO;
import io.metersphere.tuhu.dto.TuhuOKRDTO;
import io.metersphere.tuhu.dto.TuhuOKRDisplayDTO;
import io.metersphere.tuhu.mapper.OKRMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class OKRService {

    @Resource
    private KanbanService kanbanService;

    @Resource
    private OKRMapper okrMapper;

    public List<TuhuOKRDTO> getOKR() {
        return okrMapper.getOKR();
    }

    public void addOKR(TuhuOKRDTO okrDto) {
        okrDto.setId(UUID.randomUUID().toString());
        okrDto.setCreateTime(System.currentTimeMillis());
        okrDto.setUpdateTime(System.currentTimeMillis());
        okrMapper.insert(okrDto);
    }

    public List<TuhuOKRDisplayDTO> getAllOKR(){
        List<TuhuOKRDisplayDTO> returnData = new ArrayList<TuhuOKRDisplayDTO>();
        List<TestCaseAllInfoDTO> caseAllInfo = getSummaryByTeam();
        List<TuhuOKRDTO> tuhuOKR = getOKR();
        for(TestCaseAllInfoDTO caseInfo : caseAllInfo) {
            TuhuOKRDisplayDTO displayOKR = new TuhuOKRDisplayDTO();
            BeanUtils.copyProperties(caseInfo, displayOKR);
            TuhuOKRDTO okrData = getTeamOKR(tuhuOKR, caseInfo.getWsId());
            if(okrData == null){
                displayOKR.setOkrApiP0(0);
                displayOKR.setOkrApiTotal(0);
                displayOKR.setOkrScenarioTestTotal(0);
                displayOKR.setOkrApiTestP0(0);
                displayOKR.setOkrApiTestTotal(0);
            }else{
                BeanUtils.copyProperties(okrData, displayOKR);
            }
            returnData.add(displayOKR);
        }
        return returnData;
    }

    public List<TestCaseAllInfoDTO> getSummaryByTeam() {
        List<TestCaseAllInfoDTO> returnData = new ArrayList<TestCaseAllInfoDTO>();
        List<TestCaseAllInfoDTO> summary = kanbanService.getSummary();
        for(TestCaseAllInfoDTO record : summary){
            String wsId = record.getWsId();
            TestCaseAllInfoDTO existTeam = getTeam(returnData, wsId);
            if(existTeam == null){
                existTeam = new TestCaseAllInfoDTO();
                BeanUtils.copyProperties(record, existTeam);
                existTeam.setProjectId("-1");
                existTeam.setProject("工作空间项目");
                returnData.add(existTeam);
            }else{
                existTeam.setApiCount(existTeam.getCompletedAPICount() + record.getCompletedAPICount());
                existTeam.setP0APICount(existTeam.getP0APICount() + record.getP0APICount());
                existTeam.setNonP0APICount(existTeam.getNonP0APICount() + record.getNonP0APICount());

                existTeam.setApiCountThisWeek(existTeam.getApiCountThisWeek() + record.getApiCountThisWeek());
                existTeam.setSingleCountThisWeek(existTeam.getSingleCountThisWeek() + record.getSingleCountThisWeek());
                existTeam.setScenarioCountThisWeek(existTeam.getScenarioCountThisWeek() + record.getScenarioCountThisWeek());

                existTeam.setCompletedAPICount(existTeam.getCompletedAPICount() + record.getCompletedAPICount());
                existTeam.setCompletedSingleCount(existTeam.getCompletedSingleCount() + record.getCompletedSingleCount());
                existTeam.setCompletedScenarioCount(existTeam.getCompletedScenarioCount() + record.getCompletedScenarioCount());

                existTeam.setSingleCount(existTeam.getSingleCount() + record.getSingleCount());
                existTeam.setScenarioCount(existTeam.getScenarioCount() + record.getScenarioCount());
            }
        }
        return returnData;
    }

    private TestCaseAllInfoDTO getTeam(List<TestCaseAllInfoDTO> infoData, String wsId){
        for(TestCaseAllInfoDTO data : infoData){
            if(data.getWsId().equals(wsId))
                return data;
        }
        return null;
    }

    private TuhuOKRDTO getTeamOKR(List<TuhuOKRDTO> okrData, String wsId){
        for(TuhuOKRDTO data : okrData){
            if(data.getWsId().equals(wsId))
                return data;
        }
        return null;
    }
}
