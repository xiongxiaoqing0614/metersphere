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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class JmeterTuhuParser extends ApiImportAbstractParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        try {
            Object scriptWrapper = SaveService.loadElement(source);
            HashTree testPlan = this.getHashTree(scriptWrapper);

            List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
            this.getApiDefinitionWithBLOBs(testPlan, results, this.getSelectModulePath(request), request, this.getHeaders(testPlan));

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

    private void getApiDefinitionWithBLOBs(HashTree tree, List<ApiDefinitionWithBLOBs> results, String selectModulePath, ApiTestImportRequest importRequest, Map<String, String> headers){
        for (Object key : tree.keySet()) {
            if (key instanceof HTTPSamplerProxy){
                HashTree httpSamplerProxy = tree.get(key);
                String samplerPath = ((HTTPSamplerProxy) key).getPath();
                String samplerName = ((HTTPSamplerProxy) key).getName();
                String samplerMethod = ((HTTPSamplerProxy) key).getMethod();

                MsHTTPSamplerProxy samplerProxy = new MsHTTPSamplerProxy();
                BeanUtils.copyBean(samplerProxy, ((HTTPSamplerProxy) key));

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
                    ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(),samplerName,samplerPath,samplerPath,importRequest);
                    JSONObject headerObj = new JSONObject();
                    for (Object objKey : httpSamplerProxy.keySet()) {
                        if (objKey instanceof HeaderManager){
                            if(((HeaderManager)objKey).getHeaders() != null){
                                for (int j = 0; j<((HeaderManager)objKey).getHeaders().size(); j++){
                                    headerObj.put(((HeaderManager)objKey).getHeader(j).getName(), ((HeaderManager)objKey).getHeader(j).getValue());
                                }
                            }
                        }
                    }
                    for (String headerManagerKey: headers.keySet()){
                        boolean hasHeader = false;
                        for(String headerKey: headerObj.keySet()){
                            if (headerKey.equals(headerManagerKey)){
                                hasHeader = true;
                            }
                        }
                        if(!hasHeader){
                            headerObj.put(headerManagerKey,headers.get(headerManagerKey));
                        }
                    }
                    JSONObject requestObj = new JSONObject();
                    requestObj.put("name", samplerName);
                    requestObj.put("path", samplerPath);
                    requestObj.put("method", samplerMethod);
                    requestObj.put("headers", headerObj.toJSONString());
                    JSONObject bodyObj = new JSONObject();
                    bodyObj.put(Body.RAW, JSON.toJSONString(samplerProxy.getBody()));
                    if (((HTTPSamplerProxy) key).getPostBodyRaw()){
                        requestObj.put("body",bodyObj);
                    }
                    apiDefinition.setRequest(requestObj.toJSONString());
                    apiDefinition.setModulePath(selectModulePath);
                    results.add(apiDefinition);
                }

            }
            // 递归子项
            HashTree node = tree.get(key);
            if (node != null) {
                getApiDefinitionWithBLOBs(node, results, selectModulePath, importRequest, headers);
            }
        }

    }

    private Map<String, String> getHeaders(HashTree tree){
        Map<String, String> headers = new HashMap<>();
        for (Object key : tree.keySet()) {
            if (key instanceof TestPlan){
                HashTree node = tree.get(key);
                for (Object objKey : node.keySet()){
                    if (objKey instanceof HeaderManager){
                        if(((HeaderManager)objKey).getHeaders() != null){
                            for (int j = 0; j<((HeaderManager)objKey).getHeaders().size(); j++){
                                headers.put(((HeaderManager)objKey).getHeader(j).getName(), ((HeaderManager)objKey).getHeader(j).getValue());
                            }
                        }
                        break;
                    }
                }
            }
            break;
        }
        return headers;
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

//    private void convertHttpSampler(MsHTTPSamplerProxy samplerProxy, Object key) {
//        try {
//            HTTPSamplerProxy source = (HTTPSamplerProxy) key;
//            BeanUtils.copyBean(samplerProxy, source);
//            samplerProxy.setRest(new ArrayList<KeyValue>() {{
//                this.add(new KeyValue());
//            }});
//            samplerProxy.setArguments(new ArrayList<KeyValue>() {{
//                this.add(new KeyValue());
//            }});
//            if (source != null && source.getHTTPFiles().length > 0) {
//                samplerProxy.getBody().initBinary();
//                samplerProxy.getBody().setType(Body.FORM_DATA);
//                List<KeyValue> keyValues = new LinkedList<>();
//                for (HTTPFileArg arg : source.getHTTPFiles()) {
//                    List<BodyFile> files = new LinkedList<>();
//                    BodyFile file = new BodyFile();
//                    file.setId(arg.getParamName());
//                    file.setName(arg.getPath());
//                    files.add(file);
//
//                    KeyValue keyValue = new KeyValue(arg.getParamName(), arg.getParamName());
//                    keyValue.setContentType(arg.getProperty("HTTPArgument.content_type").toString());
//                    keyValue.setType("file");
//                    keyValue.setFiles(files);
//                    keyValues.add(keyValue);
//                }
//                samplerProxy.getBody().setKvs(keyValues);
//            }
//            samplerProxy.setProtocol(RequestType.HTTP);
//            samplerProxy.setConnectTimeout(source.getConnectTimeout() + "");
//            samplerProxy.setResponseTimeout(source.getResponseTimeout() + "");
//            samplerProxy.setPort(source.getPropertyAsString("HTTPSampler.port"));
//            samplerProxy.setDomain(source.getDomain());
//            if (source.getArguments() != null) {
//                if (source.getPostBodyRaw()) {
//                    samplerProxy.getBody().setType(Body.RAW);
//                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
//                        samplerProxy.getBody().setRaw(v);
//                    });
//                    samplerProxy.getBody().initKvs();
//                } else {
//                    List<KeyValue> keyValues = new LinkedList<>();
//                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
//                        KeyValue keyValue = new KeyValue(k, v);
//                        keyValues.add(keyValue);
//                    });
//                    if (CollectionUtils.isNotEmpty(keyValues)) {
//                        samplerProxy.setArguments(keyValues);
//                    }
//                }
//                samplerProxy.getBody().initBinary();
//            }
//            // samplerProxy.setPath(source.getPath());
//            samplerProxy.setMethod(source.getMethod());
//            if (this.getUrl(source) != null) {
//                samplerProxy.setUrl(this.getUrl(source));
//                samplerProxy.setPath(null);
//            }
//            samplerProxy.setId(UUID.randomUUID().toString());
//            samplerProxy.setType("HTTPSamplerProxy");
//            // 处理HTTP协议的请求头
//            if (headerMap.containsKey(key.hashCode())) {
//                List<KeyValue> keyValues = new LinkedList<>();
//                headerMap.get(key.hashCode()).forEach(item -> {
//                    HeaderManager headerManager = (HeaderManager) item;
//                    if (headerManager.getHeaders() != null) {
//                        for (int i = 0; i < headerManager.getHeaders().size(); i++) {
//                            keyValues.add(new KeyValue(headerManager.getHeader(i).getName(), headerManager.getHeader(i).getValue()));
//                        }
//                    }
//                });
//                samplerProxy.setHeaders(keyValues);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

