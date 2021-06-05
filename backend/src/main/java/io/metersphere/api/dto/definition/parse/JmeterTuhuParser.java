package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.parse.har.model.Har;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.threads.ThreadGroup;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class JmeterTuhuParser extends ApiImportAbstractParser {
    boolean threadConfirm = false;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        try {
            Object scriptWrapper = SaveService.loadElement(source);
            HashTree testPlan = this.getHashTree(scriptWrapper);

            this.projectId = request.getProjectId();
            List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
            List<HashTree> threadGroupList = new ArrayList<>();
            this.getAllThreadGroup(testPlan,threadGroupList);
            this.getApiDefinitionWithBLOBs(testPlan, results, request.getModuleId(), this.getSelectModulePath(request), request, threadGroupList);

            ApiDefinitionImport apiImport = new ApiDefinitionImport();
            apiImport.setData(results);
            return apiImport;
        }
        catch (Exception e) {
            e.printStackTrace();
            MSException.throwException("当前JMX版本不兼容");
        }
        return null;
    }

    private String getSelectModulePath(ApiTestImportRequest importRequest) {
        ApiModule selectModule = null;
        String selectModulePath = null;
        if (StringUtils.isNotBlank(importRequest.getModuleId())) {
            selectModule = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
            if (selectModule != null) {
                selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
            }
        }
        return selectModulePath;
    }

    private void getApiDefinitionWithBLOBs(HashTree tree, List<ApiDefinitionWithBLOBs> results, String selectModuleId, String selectModulePath, ApiTestImportRequest importRequest, List<HashTree> threadGroupList){
        for (Object key : tree.keySet()) {
            if (key instanceof HTTPSamplerProxy){
                HashTree httpSamplerProxy = tree.get(key);
                String samplerPath = ((HTTPSamplerProxy) key).getPath();
                String samplerName = ((HTTPSamplerProxy) key).getName();
                String samplerMethod = ((HTTPSamplerProxy) key).getMethod();

                MsHTTPSamplerProxy samplerProxy = new MsHTTPSamplerProxy();
                samplerProxy.setBody(new Body());
                samplerProxy.setArguments(new ArrayList<KeyValue>() {{
                    this.add(new KeyValue());
                }});
                HTTPSamplerProxy source = (HTTPSamplerProxy) key;
                BeanUtils.copyBean(samplerProxy, source);

                boolean hasPath = false;
                for (int i = 0; i<results.size(); i++){
                    String path = results.get(i).getPath();
                    if (samplerPath.equals(path)){
                        hasPath = true;
                        break;
                    }
                }
                if (!hasPath){
                    MsHTTPSamplerProxy request = buildRequest(samplerName, samplerPath, samplerMethod);
                    ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(),samplerName,samplerPath,samplerMethod,importRequest);
                    List<KeyValue> headerObj = new ArrayList<>();
                    for (Object objKey : httpSamplerProxy.keySet()) {
                        if (objKey instanceof HeaderManager){
                            if(((HeaderManager)objKey).getHeaders() != null){
                                for (int j = 0; j<((HeaderManager)objKey).getHeaders().size(); j++){
                                    headerObj.add(new KeyValue(((HeaderManager)objKey).getHeader(j).getName(), ((HeaderManager)objKey).getHeader(j).getValue()));
                                }
                            }
                        }
                    }
                    List<Map<String, String>> headers = this.getHttpSamplerConHeader(threadGroupList,key);
                    if (headers.size()>0){
                        for (String headerManagerKey: headers.get(0).keySet()){
                            boolean hasHeader = false;
                            for(int k = 0; k<headerObj.size(); k++){
                                if (headerObj.get(k).getName().equals(headerManagerKey)){
                                    hasHeader = true;
                                }
                            }
                            if(!hasHeader){
                                headerObj.add(new KeyValue(headerManagerKey,headers.get(0).get(headerManagerKey)));
                            }
                        }
                    }

                    JSONObject requestObj = new JSONObject();
                    requestObj.put("name", samplerName);
                    requestObj.put("path", samplerPath);
                    requestObj.put("method", samplerMethod);
                    requestObj.put("headers", headerObj);
                    if (source.getArguments() != null) {
                        if (source.getPostBodyRaw()) {
                            samplerProxy.getBody().setType(Body.RAW);
                            source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                                samplerProxy.getBody().setRaw(v);
                            });
                            samplerProxy.getBody().initKvs();
                        } else {
                            samplerProxy.getBody().setType(Body.WWW_FROM);
                            List<KeyValue> keyValues = new LinkedList<>();
                            source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                                KeyValue keyValue = new KeyValue(k, v);
                                keyValues.add(keyValue);
                            });
                            if (CollectionUtils.isNotEmpty(keyValues)) {
                                samplerProxy.getBody().setKvs(keyValues);
                            }
                        }
                        samplerProxy.getBody().initBinary();
                    }
                    requestObj.put("body",samplerProxy.getBody());
                    apiDefinition.setRequest(requestObj.toJSONString());
                    apiDefinition.setModuleId(selectModuleId);
                    apiDefinition.setModulePath(selectModulePath);
                    results.add(apiDefinition);
                }

            }
            // 递归子项
            HashTree node = tree.get(key);
            if (node != null) {
                getApiDefinitionWithBLOBs(node, results, selectModuleId, selectModulePath, importRequest, threadGroupList);
            }
        }

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

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void getAllThreadGroup(HashTree tree,List<HashTree>threadGroupList){
        for (Object key: tree.keySet()){
            if (key instanceof ThreadGroup){
                threadGroupList.add(tree.get(key));
            }
            HashTree node = tree.get(key);
            if (node != null){
                getAllThreadGroup(node,threadGroupList);
            }
        }
    }

    private List<Map<String, String>> getHttpSamplerConHeader(List<HashTree>threadGroupList, Object samplerKey){
        List<Map<String, String>> headerList = new ArrayList<>();
        for (int i = 0; i<threadGroupList.size(); i++){
            boolean threadConfirm = this.threadGroupConfirm(threadGroupList.get(i),samplerKey);
            if (threadConfirm){
                this.getThreadHeader(threadGroupList.get(i),headerList);
                break;
            }
        }
        return headerList;
    }

    private boolean threadGroupConfirm(HashTree tree, Object samplerKey){
        threadConfirm = false;
        for (Object key: tree.keySet()){
            if (key.equals(samplerKey)){
                threadConfirm = true;
                break;
            }
            HashTree node = tree.get(key);
            if (node != null && !threadConfirm){
                threadGroupConfirm(node,samplerKey);
            }
        }
        return threadConfirm;
    }

    private void getThreadHeader(HashTree tree, List<Map<String, String>> headerList){
        Map<String, String> headers = new HashMap<>();
        for (Object key : tree.keySet()) {
            if (key instanceof HeaderManager){
                if(((HeaderManager)key).getHeaders() != null){
                    for (int j = 0; j<((HeaderManager)key).getHeaders().size(); j++){
                        headers.put(((HeaderManager)key).getHeader(j).getName(), ((HeaderManager)key).getHeader(j).getValue());
                    }
                }
                headerList.add(headers);
            }
            HashTree node = tree.get(key);
            if (node != null && !(key instanceof HTTPSamplerProxy)) {
                getThreadHeader(node, headerList);
            }
        }
    }
}

