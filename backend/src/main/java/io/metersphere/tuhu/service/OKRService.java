package io.metersphere.tuhu.service;

import io.metersphere.tuhu.controller.OKRRequest;
import io.metersphere.tuhu.dto.AppIdCoverageDTO;
import io.metersphere.tuhu.dto.TestCaseAllInfoDTO;
import io.metersphere.tuhu.dto.TuhuOKRDTO;
import io.metersphere.tuhu.dto.TuhuOKRDisplayDTO;
import io.metersphere.tuhu.mapper.OKRMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class OKRService {

    @Resource
    private KanbanService kanbanService;

    @Resource
    private OKRMapper okrMapper;

    public void addOKR(TuhuOKRDTO okrDto) {
        okrDto.setId(UUID.randomUUID().toString());
        okrDto.setCreateTime(System.currentTimeMillis());
        okrDto.setUpdateTime(System.currentTimeMillis());
        okrMapper.insert(okrDto);
    }

    private String getNameByCurrentTime() {
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf(cal.get(Calendar.YEAR));
        int month = cal.get(Calendar.MONTH) + 1;
        if (month >= 1 && month <= 3) {
            return year + "Q1";
        } else if (month >= 4 && month <= 6) {
            return year + "Q2";
        } else if (month >= 7 && month <= 9) {
            return year + "Q3";
        } else {
            return year + "Q4";
        }
    }

    private String getNextQName() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        if (month >= 1 && month <= 3) {
            return year + "Q2";
        } else if (month >= 4 && month <= 6) {
            return year + "Q3";
        } else if (month >= 7 && month <= 9) {
            return year + "Q4";
        } else {
            return year+1 + "Q1";
        }
    }

    public String updateOKR(OKRRequest request) {
        TuhuOKRDTO okrDto = new TuhuOKRDTO();
        okrDto.setId(request.getId());
        okrDto.setName(request.getName());
        okrDto.setWsId(request.getWsId());
        okrDto.setOkrApiP0(request.getOkrApiP0());
        okrDto.setOkrApiTotal(request.getOkrApiTotal());
        okrDto.setOkrApiTestP0(request.getOkrApiTestP0());
        okrDto.setOkrApiTestTotal(request.getOkrApiTestTotal());
        okrDto.setOkrScenarioTestTotal(request.getOkrScenarioTestTotal());
        okrDto.setDescription(request.getDescription());
        updateOKR(okrDto);
        return okrDto.getId();
    }

    public void updateOKR(TuhuOKRDTO okrDto) {
        if(okrDto.getName() == null){
            okrDto.setName(getNameByCurrentTime());
        }
        if(okrDto.getId().equals("")) {
            addOKR(okrDto);
        }else{
            okrDto.setUpdateTime(System.currentTimeMillis());
            okrMapper.update(okrDto);
        }
    }

    private List<TuhuOKRDisplayDTO> transferDisplayDTO(List<TestCaseAllInfoDTO> caseAllInfo, List<TuhuOKRDTO> tuhuOKR){
        List<TuhuOKRDisplayDTO> returnData = new ArrayList<TuhuOKRDisplayDTO>();

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
                displayOKR.setId("");
            }else{
                BeanUtils.copyProperties(okrData, displayOKR);
            }
            returnData.add(displayOKR);
        }
        return returnData;
    }

    public List<TuhuOKRDTO> getOKR() {
        return okrMapper.getOKR();
    }

    public List<TuhuOKRDisplayDTO> getOKRByName(String okrName) {
        List<TestCaseAllInfoDTO> caseAllInfo = getSummaryByTeam();
        List<TuhuOKRDTO> tuhuOKR = okrMapper.getOKRByName(okrName);
        return transferDisplayDTO(caseAllInfo, tuhuOKR);
    }

    public List<TuhuOKRDTO> getOKRByNameAndWSId(String okrName, String wsId) {
        return okrMapper.getOKRByNameAndWSId(okrName, wsId);
    }

    public List<TuhuOKRDisplayDTO> getAllOKR(){
        List<TestCaseAllInfoDTO> caseAllInfo = getSummaryByTeam();
        List<TuhuOKRDTO> tuhuOKR = getOKR();

        return transferDisplayDTO(caseAllInfo, tuhuOKR);
    }

    public List<TuhuOKRDisplayDTO> getCurrentOKR(){
        return getOKRByName(getNameByCurrentTime());
    }

    public List<TestCaseAllInfoDTO> getSummaryByTeam() {
        List<TestCaseAllInfoDTO> returnData = new ArrayList<TestCaseAllInfoDTO>();
        List<TestCaseAllInfoDTO> summary = kanbanService.getSummaryV2();
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

        List<AppIdCoverageDTO> appIdCoverageList = okrMapper.getAppIdCoverage();
        Map<String, Float> appIdsCoverageMap = new HashMap<>();
        for(AppIdCoverageDTO appIdCoverage : appIdCoverageList) {
            appIdsCoverageMap.put(appIdCoverage.getTeamId(), appIdCoverage.getPercent());
        }

        returnData.forEach(item -> {
            item.setAppIdCoverage(appIdsCoverageMap.get(item.getWsId()));
        });

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

    public List<String> getOKRNames(){
        List<String> existedOKRNames = okrMapper.getOKRNames();
        if(existedOKRNames.isEmpty()){
            existedOKRNames.add(getNameByCurrentTime());
        }else{
            String nextOKRName = getNextQName();
            if(!existedOKRNames.contains(nextOKRName))
                existedOKRNames.add(nextOKRName);
        }
        return existedOKRNames;
    }
}
