package io.metersphere.tuhu.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ExecutedCaseInfoResult;
import io.metersphere.api.dto.datacount.response.ApiDataCountDTO;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.tuhu.dto.ApiScenarioUrlAndIdDTO;
import io.metersphere.tuhu.dto.TuhuAppIdApiMappingDTO;
import io.metersphere.tuhu.mapper.GraphMapper;
import io.metersphere.tuhu.mapper.TuhuAllAppIdMapper;
import io.metersphere.tuhu.mapper.TuhuAppIdApiMappingMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class GraphService {

    @Value("${halley.service.url}")
    private String halleyServiceUrl;

    @Value("${forseti.service.url}")
    private String forsetiServiceUrl;

    @Resource
    private TuhuService tuhuService;

    @Resource
    private GraphMapper graphMapper;

    @Resource
    private TuhuAllAppIdMapper tuhuAllAppIdMapper;

    @Resource
    private TuhuAppIdApiMappingMapper tuhuAppIdApiMappingMapper;

    private JSONArray fillData(long totalNum, long inNum, String tip) {
        long outNum = totalNum - inNum;
        float inPercent = 0;
        float outPercent = 0;

        if (totalNum > 0) {
            inPercent = (float) inNum / totalNum * 100;
            outPercent = (float) outNum / totalNum * 100;
        }

        JSONArray ja = new JSONArray();
        JSONObject si = new JSONObject();
        si.put("name", String.format("已%s数量: %d (%.2f%%)", tip, inNum, inPercent));
        si.put("value", inNum);
        ja.add(si);

        JSONObject so = new JSONObject();
        so.put("name", String.format("未%s数量: %d (%.2f%%)", tip, outNum, outPercent));
        so.put("value", outNum);
        ja.add(so);

        return ja;
    }

    private int countServiceNum(List<String> allAppIdList) {
        List<String> items = graphMapper.queryApiAppIdList();
        // 求两个Array的交集
        items.retainAll(allAppIdList);
        return items.size();
    }

    public JSONArray getServiceInData() {
        long totalNum = 0;
        long inNum = 0;

        List<String> allAppIdList = graphMapper.queryAppIdList();

        if (allAppIdList != null && allAppIdList.size() > 0) {
            totalNum = allAppIdList.size();
            inNum = this.countServiceNum(allAppIdList);
        }

        return fillData(totalNum, inNum, "接入服务");
    }

    // 暂时无法获取到准确的团队情况
    public JSONArray getTeamInData() {
        return fillData(0, 0, "接入团队");
    }

    public JSONArray getApiDoneData() {
        long totalNum = graphMapper.countApiTotal();
        long inNum = graphMapper.countApiDone();
        return fillData(totalNum, inNum, "完成接口");
    }

    public JSONArray getApiPassRateData() {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        List<ApiDataCountResult> allExecuteResult = graphMapper.countExecuteResult();
        apiCountResult.countScheduleExecute(allExecuteResult);

        long totalNum = apiCountResult.getExecutedCount();
        long inNum = apiCountResult.getSuccessCount();

        return fillData(totalNum, inNum, "执行成功定时任务");
    }

    public JSONArray getApiCovRateData() {
        ApiDataCountDTO apiCountResult = new ApiDataCountDTO();

        List<ApiDataCountResult> countResultByApiCoverageList = graphMapper.countApiCoverage();
        apiCountResult.countApiCoverage(countResultByApiCoverageList);

        long totalNum = apiCountResult.getCoverageCount() + apiCountResult.getUncoverageCount();
        long inNum = apiCountResult.getCoverageCount();

        return fillData(totalNum, inNum, "有用例覆盖的接口");
    }

    public JSONArray getScenCovRateData() {
        return fillData(0, 0, "有场景覆盖的接口");
//        // 场景中复制的接口
//        ApiScenarioUrlAndIdDTO allScenarioInfoList = selectIdAndScenarioByStep();
//        // 场景中的自定义路径与接口定义中的匹配
//        List<ApiDefinition> allEffectiveApiList = graphMapper.selectEffectiveId();
//        // 场景中引用/复制的案例
//        List<ApiTestCase> allEffectiveApiCaseList = graphMapper.selectEffectiveTestCase();
//
//        if (allEffectiveApiList == null || allEffectiveApiList.isEmpty()) {
//            return fillData(0, 0, "有场景覆盖的接口");
//        }
//
//        JSONObject jo = countInterfaceCoverage(allScenarioInfoList, allEffectiveApiList, allEffectiveApiCaseList);
//
//        return fillData(jo.getLong("total"), jo.getLong("inNum"), "有场景覆盖的接口");
    }

    public JSONObject getCaseFailData() {
        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (Exception e) {
        }

        if (startTime == null) {
            return new JSONObject();
        }

        JSONObject jo = new JSONObject();
        int limitNumber = 20;
        JSONArray caseNameList = new JSONArray();
        JSONArray failureTimes = new JSONArray();

        List<ExecutedCaseInfoResult> list = graphMapper.findFailureCaseInfoByExecuteTimeAndLimitNumber(startTime.getTime());
        if (list.size() == 0) {
            return new JSONObject();
        }

        if (list.size() < limitNumber) {
            limitNumber = list.size();
        }

        List<ExecutedCaseInfoResult> subList = list.subList(0, limitNumber);
        for (ExecutedCaseInfoResult executedCaseInfoResult : subList) {
            caseNameList.add(executedCaseInfoResult.getCaseName());
            failureTimes.add(executedCaseInfoResult.getFailureTimes());
        }

        jo.put("x", failureTimes);
        jo.put("y", caseNameList);

        return jo;
    }

    public JSONObject countInterfaceCoverage(ApiScenarioUrlAndIdDTO allScenarioInfoList, List<ApiDefinition> allEffectiveApiList, List<ApiTestCase> allEffectiveApiCaseList) {
        /**
         * 前置工作：
         *  1。将接口集合转化数据结构: map<url,List<id>> urlMap 用来做3的筛选
         *  2。将案例集合转化数据结构：map<testCase.id,List<testCase.apiId>> caseIdMap 用来做2的筛选
         *  3。将接口集合转化数据结构: List<id> allApiIdList 用来做1的筛选
         *  4。自定义List<api.id> coveragedIdList 已覆盖的id集合。 最终计算公式是 coveragedIdList/allApiIdList在
         *
         * 解析allScenarioList的scenarioDefinition字段。
         * 1。提取每个步骤的url。 在 urlMap筛选
         * 2。提取每个步骤的id.   在caseIdMap 和 allApiIdList。
         */
        Map<String, List<String>> urlMap = new HashMap<>();
        List<String> allApiIdList = new ArrayList<>();
        Map<String, List<String>> caseIdMap = new HashMap<>();
        for (ApiDefinition model : allEffectiveApiList) {
            String url = model.getPath();
            String id = model.getId();
            allApiIdList.add(id);
            if (urlMap.containsKey(url)) {
                urlMap.get(url).add(id);
            } else {
                List<String> list = new ArrayList<>();
                list.add(id);
                urlMap.put(url, list);
            }
        }
        for (ApiTestCase model : allEffectiveApiCaseList) {
            String caseId = model.getId();
            String apiId = model.getApiDefinitionId();
            if (urlMap.containsKey(caseId)) {
                urlMap.get(caseId).add(apiId);
            } else {
                List<String> list = new ArrayList<>();
                list.add(apiId);
                urlMap.put(caseId, list);
            }
        }

        List<String> urlList = allScenarioInfoList.getIdList();
        List<String> idList = allScenarioInfoList.getIdList();

        List<String> containsApiIdList = new ArrayList<>();

        for (String url : urlList) {
            List<String> apiIdList = urlMap.get(url);
            if (apiIdList != null) {
                for (String api : apiIdList) {
                    if (!containsApiIdList.contains(api)) {
                        containsApiIdList.add(api);
                    }
                }
            }
        }

        for (String id : idList) {
            List<String> apiIdList = caseIdMap.get(id);
            if (apiIdList != null) {
                for (String api : apiIdList) {
                    if (!containsApiIdList.contains(api)) {
                        containsApiIdList.add(api);
                    }
                }
            }

            if (allApiIdList.contains(id)) {
                if (!containsApiIdList.contains(id)) {
                    containsApiIdList.add(id);
                }
            }
        }

        JSONObject jo = new JSONObject();
        jo.put("total", allApiIdList.size());
        jo.put("inNum", containsApiIdList.size());

        return jo;
    }

    private ApiScenarioUrlAndIdDTO selectIdAndScenarioByStep () {
        List<String> urlList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        long start = 0;
        long step = 100;

        while (true) {
            List<ApiScenarioWithBLOBs> allScenarioInfoList = graphMapper.selectIdAndScenario(start, step);
            if (allScenarioInfoList == null || allScenarioInfoList.size() == 0){
                break;
            }
            for (ApiScenarioWithBLOBs model : allScenarioInfoList) {
                String scenarioDefiniton = model.getScenarioDefinition();
                this.addUrlAndIdToList(scenarioDefiniton, urlList, idList);
            }
            start += step;
        }

        ApiScenarioUrlAndIdDTO apiScenarioUrlAndIdDTO = new ApiScenarioUrlAndIdDTO();
        apiScenarioUrlAndIdDTO.setUrlList(urlList);
        apiScenarioUrlAndIdDTO.setIdList(idList);

        return apiScenarioUrlAndIdDTO;
    }

    private void addUrlAndIdToList(String scenarioDefiniton, List<String> urlList, List<String> idList) {
        try {
            JSONObject scenarioObj = JSONObject.parseObject(scenarioDefiniton);
            if (scenarioObj.containsKey("hashTree")) {
                JSONArray hashArr = scenarioObj.getJSONArray("hashTree");
                for (int i = 0; i < hashArr.size(); i++) {
                    JSONObject elementObj = hashArr.getJSONObject(i);
                    if (elementObj.containsKey("id")) {
                        String id = elementObj.getString("id");
                        idList.add(id);
                    }
                    if (elementObj.containsKey("url")) {
                        String url = elementObj.getString("url");
                        urlList.add(url);
                    }
                    if (elementObj.containsKey("path")) {
                        String path = elementObj.getString("path");
                        urlList.add(path);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void syncAllAppIdFromHalley() {
        Map<String, String> headers = new HashMap<>();

        tuhuAllAppIdMapper.queryOrgAndWsInfoByName("orgName", "wsName");
        headers.put("third-party-token", "Metersphere_JKQO314qDoi8XYlG");
        String rep = TuhuService.restApiGet(halleyServiceUrl, "", headers);
        if (rep != null) {
            JSONObject jo = (JSONObject)JSONObject.parse(rep);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");
                SyncService syncService = new SyncService();
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    syncService.convertItem(item);
                }
                syncService.updateAppId(tuhuAllAppIdMapper);
            }
        }
    }

    public void syncAppIdMappingFromForseti() {
        String rep = TuhuService.restApiGet(forsetiServiceUrl, "", new HashMap<>());
        if (rep != null) {
            JSONObject jo = JSONObject.parseObject(rep);
            if (jo.getBoolean("success")) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject rels = data.getJSONObject("rels");
                Iterator iter = rels.keySet().iterator();
                while(iter.hasNext()){
                    String appId = (String) iter.next();
                    JSONArray apis = rels.getJSONArray(appId);
                    if(apis.size()>0){
                        List<TuhuAppIdApiMappingDTO> list = new ArrayList<>();
                        for(int i = 0; i < apis.size(); i++){
                            TuhuAppIdApiMappingDTO temp = new TuhuAppIdApiMappingDTO();
                            temp.setId(UUID.randomUUID().toString());
                            temp.setAppId(appId);
                            temp.setUrl(apis.getString(i));
                            list.add(temp);
                        }
                        tuhuAppIdApiMappingMapper.deleteByAppId(appId);
                        tuhuAppIdApiMappingMapper.addByAppId(list);
                    }
                }
            }
        }
    }
}
