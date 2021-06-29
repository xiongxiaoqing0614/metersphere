package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.commons.constants.ApiImportPlatform;
import org.apache.commons.lang3.StringUtils;

public class ScenarioImportParserFactory {
    public static ApiImportParser getImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Metersphere.name(), platform)) {
            return new MsScenarioParser();
        } else if (StringUtils.equals(ApiImportPlatform.Postman.name(), platform)) {
            return new PostmanScenarioParser();
        } else if (StringUtils.equals(ApiImportPlatform.Jmeter.name(), platform)) {
            return new MsJmeterParser();
        } else if (StringUtils.equals(ApiImportPlatform.Har.name(), platform)) {
            return new HarScenarioParser();
        } else if (StringUtils.equals(ApiImportPlatform.JmeterTuhu.name(), platform)){
            return new MsJmeterTuhuParser();
        } else if (StringUtils.equals(ApiImportPlatform.JmeterTuhuCase.name(), platform)){
            return new MsJmeterTuhuCaseParser();
        }
        return null;
    }
}
