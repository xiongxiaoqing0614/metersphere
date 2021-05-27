package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ForsetiParser extends ApiImportAbstractParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getDataFromForseti(request.getAppId());
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
//        JSONArray dataObj = testObject.getJSONArray("data");
        this.projectId = request.getProjectId();
        return parseForsetiString(testStr, request);
    }

    private String getDataFromForseti(String appId){
        //todo: make it configurable for prod
        String forsetiUrl = "http://10.100.140.67:9119/apis/app/test/apis/";
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("appNames", appId.split(","));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        String responseJson = "";
        String requestBodyString = jsonObj.toJSONString();
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyString, headers);
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3 * 1000);
        httpRequestFactory.setConnectTimeout(2 * 60 * 1000);
        httpRequestFactory.setReadTimeout(10 * 60 * 1000);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(forsetiUrl, HttpMethod.POST, requestEntity, String.class);
            responseJson = responseEntity.getBody();
        }catch(Exception e){
            LogUtil.error(e.getMessage(), e);
        }
        return responseJson;
    }

    private ApiDefinitionImport  parseForsetiString(String jsonStr, ApiTestImportRequest importRequest){
        ApiDefinitionImport apiDefinitionImport = JSON.parseObject(jsonStr, ApiDefinitionImport.class);
        List<ApiDefinitionWithBLOBs> results = new ArrayList<ApiDefinitionWithBLOBs>();
        apiDefinitionImport.setData(results);
        JSONObject testObject = JSONObject.parseObject(jsonStr);
        JSONArray dataArray = testObject.getJSONArray("data");

        for(int i = 0; i < dataArray.size(); i++){
            JSONObject dataObj = dataArray.getJSONObject(i);
            String tag = dataObj.getString("appName");
            ApiModule parentModule = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
            ApiModule module = ApiDefinitionImportUtil.buildModule(parentModule, tag, importRequest.getProjectId());
            JSONArray apiArray = dataObj.getJSONArray("apis");
            for(int j = 0; j < apiArray.size(); j++){
                JSONObject apiObj = apiArray.getJSONObject(j);
                ApiDefinitionResult apiDefinition = parseForsetiApiObj(apiObj, importRequest);
                String apiTag = apiObj.getString("tags");
                if (apiTag != null) {
                    if (apiTag.contains(",")) {
                        String[] apiTags = apiTag.split(",");
                        for(String innerTag: apiTags){
                            if (innerTag.contains("【")) {
                                apiTag = innerTag;
                                break;
                            }
                        }
                        if(apiTag.contains(","))
                            apiTag = apiTags[0];
                    }
                    ApiModule apiModule = ApiDefinitionImportUtil.buildModule(module, apiTag, importRequest.getProjectId());
                    apiDefinition.setModuleId(apiModule.getId());
                } else {
                    apiDefinition.setModuleId(module.getId());
                }
                results.add(apiDefinition);
            }
        }

        return apiDefinitionImport;
    }

    private ApiDefinitionResult parseForsetiApiObj(JSONObject apiObj, ApiTestImportRequest importRequest){

        String requestName = importRequest.getName();
        if(requestName == null){
            requestName = apiObj.getString("apiName");
        }

        String path = apiObj.getString("api");
        String method = apiObj.getString("method").toUpperCase();
        MsHTTPSamplerProxy request = buildRequest(requestName, path, method);

        ApiDefinitionResult apiDefinition = buildApiDefinition(request.getId(), requestName, path, method,importRequest);
        String reqStr = buildForsetiApiReq(apiObj);
        apiDefinition.setRequest(reqStr);
        String resStr = buildForsetiApiRes(apiObj);
        apiDefinition.setResponse(resStr);
        return apiDefinition;
    }

    protected ApiDefinitionResult buildApiDefinition(String id, String name, String path, String method, ApiTestImportRequest importRequest) {
        ApiDefinitionResult apiDefinition = new ApiDefinitionResult();
        apiDefinition.setName(name);
        apiDefinition.setPath(path);
        apiDefinition.setProtocol(RequestType.HTTP);
        apiDefinition.setMethod(method);
        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        if (StringUtils.equalsIgnoreCase("schedule", importRequest.getType())) {
            apiDefinition.setUserId(importRequest.getUserId());
        } else {
            apiDefinition.setUserId(SessionUtils.getUserId());
        }
        return apiDefinition;
    }

    private String buildForsetiApiReq(JSONObject testNode) {
        JSONObject requestObj = new JSONObject();
        requestObj.put("name", testNode.getString("apiName"));
        requestObj.put("path", testNode.getString("api"));
        requestObj.put("method", testNode.getString("method"));
        requestObj.put("rest", testNode.getJSONArray("rest"));
        requestObj.put("arguments", testNode.getJSONArray("querys"));
        requestObj.put("headers", testNode.getJSONArray("reqHeaders"));
        requestObj.put("body", buildForsetiApiReqBody(testNode));
        return requestObj.toJSONString();
    }

    private JSONObject buildForsetiApiReqBody(JSONObject bodyObj) {
        String bodyStr = bodyObj.getString("body");
        JSONObject resBodyObj = new JSONObject();
        if(bodyStr != null) {
            resBodyObj.put(Body.RAW, bodyStr);
        }

        return resBodyObj;
    }

    private String buildForsetiApiRes(JSONObject testNode) {
        JSONObject respObj = new JSONObject();
        respObj.put("type", RequestType.HTTP);
        respObj.put("headers", testNode.getJSONArray("resHeaders"));
        respObj.put("statusCode", buildForsetiApiResCode(testNode));
        respObj.put("body", buildForsetiApiResBody(testNode));

        return respObj.toJSONString();
    }

    private JSONObject buildForsetiApiResBody(JSONObject testNode) {
        String resStr = testNode.getString("res");
        JSONObject bodyObj = new JSONObject();
        if(resStr != null)
            bodyObj.put(Body.RAW, resStr);

        return bodyObj;
    }

    private JSONArray buildForsetiApiResCode(JSONObject testNode) {
        JSONArray bodyArray = testNode.getJSONArray("bizCodes");
        JSONArray codeArray = new JSONArray();
        for(int i = 0; i < bodyArray.size(); i++){
            String code = bodyArray.getJSONObject(i).getString("code");
            String desc = bodyArray.getJSONObject(i).getString("desc");
            codeArray.add(new KeyValue(code, desc));
        }

        return codeArray;
    }
}
