package io.metersphere.ldap.controller;

import io.metersphere.base.domain.Organization;
import io.metersphere.base.domain.User;
import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.organization.AddOrgMemberRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.service.LdapService;
import io.metersphere.service.OrganizationService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private UserService userService;
    @Resource
    private LdapService ldapService;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private SystemParameterService systemParameterService;

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {

        String isOpen = systemParameterService.getValue(ParamConstants.LDAP.OPEN.getValue());
        if (StringUtils.isBlank(isOpen) || StringUtils.equals(Boolean.FALSE.toString(), isOpen)) {
            MSException.throwException(Translator.get("ldap_authentication_not_enabled"));
        }

        DirContextOperations dirContext = ldapService.authenticate(request);
        String email = ldapService.getMappingAttr("email", dirContext).replaceAll("\\.ad$", ".cn");
        String userId = ldapService.getMappingAttr("username", dirContext);

        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LDAP.name());
        SecurityUtils.getSubject().getSession().setAttribute("email", email);


        if (StringUtils.isBlank(email)) {
            MSException.throwException(Translator.get("login_fail_email_null"));
        }

        // userId 或 email 有一个相同即为存在本地用户
        User u = userService.selectUser(userId, email);
        if (u == null) {

            // 新建用户 获取LDAP映射属性
            String name = ldapService.getMappingAttr("name", dirContext);
            String phone = ldapService.getNotRequiredMappingAttr("phone", dirContext);

            User user = new User();
            user.setId(userId);
            user.setName(name);
            user.setEmail(email);

            if (StringUtils.isNotBlank(phone)) {
                user.setPhone(phone);
            }

            user.setSource(UserSource.LDAP.name());
            userService.addLdapUser(user);
            //TODO: make it configurable
            InetAddress address = null;
            try {
                address = InetAddress.getLocalHost();
                System.out.println(address.getHostName());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String urlSF = "https://yewu-gateway-inner.tuhu.work/ext-spring-yw-user-center/open/sf/employee/getEmployeeInfo";
            String urlSFPro = "http://yewu-gateway.ad.tuhu.cn:9010/ext-spring-yw-user-center/open/sf/employee/getEmployeeInfo";
            String ldapAddress = systemParameterService.getValue(ParamConstants.LDAP.URL.getValue());
            if(ldapAddress.endsWith("3889"))
                urlSF = urlSFPro;
            LogUtil.info(ldapAddress);

            MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            String responseJson = "";
            String requestBodyString = "{\"email\":\"" + email + "\"}";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyString, headers);
            HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            httpRequestFactory.setConnectionRequestTimeout(3 * 1000);
            httpRequestFactory.setConnectTimeout(2 * 60 * 1000);
            httpRequestFactory.setReadTimeout(10 * 60 * 1000);
            RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(urlSF, HttpMethod.POST, requestEntity, String.class);
                responseJson = responseEntity.getBody();
            }catch(Exception e){
                LogUtil.error(e.getMessage(), e);
            }

            String organizationName = ldapService.getFromJson("departmentLevel2Name", responseJson);
            String departmentLevel3Name = ldapService.getFromJson("departmentLevel3Name", responseJson);
            String workspaceName = ldapService.getFromJson("departmentLevel4Name", responseJson);
            if(workspaceName == null) {
                if (departmentLevel3Name != null)
                    workspaceName = departmentLevel3Name;
                else
                    workspaceName = ldapService.getNotRequiredMappingAttr("department", dirContext);
            }
            if(organizationName != null)
            {
                Organization org = organizationService.getOrganizationByName(organizationName);
                Workspace ws = workspaceService.getWorkspaceByName(workspaceName);
                boolean bNewOrg = false;
                if (org == null) {
                    bNewOrg = true;
                    //create a new organization
                    org = new Organization();
                    org.setName(organizationName);
                    organizationService.addOrganization(org);
                }
                if (bNewOrg || ws == null) {
                    //create a new workspace
                    ws = new Workspace();
                    ws.setName(workspaceName);
                    ws.setOrganizationId(org.getId());
                    workspaceService.addWorkspaceByAdmin(ws);
                }

                //set org and ws to this user
                AddOrgMemberRequest addOrgMemberRequest = new AddOrgMemberRequest();
                addOrgMemberRequest.setOrganizationId(org.getId());
                addOrgMemberRequest.setUserIds(Stream.of(userId).collect(toList()));
                if(workspaceName.equalsIgnoreCase("测试组"))
                    addOrgMemberRequest.setRoleIds(Stream.of("org_admin").collect(toList()));
                else
                    addOrgMemberRequest.setRoleIds(Stream.of("org_member").collect(toList()));
                userService.addOrganizationMember(addOrgMemberRequest);
                user.setLastOrganizationId(org.getId());
                AddMemberRequest addMemberRequest = new AddMemberRequest();
                addMemberRequest.setWorkspaceId(ws.getId());
                addMemberRequest.setUserIds(Stream.of(userId).collect(toList()));
                if(workspaceName.equalsIgnoreCase("测试组")) {
                    addMemberRequest.setRoleIds(Stream.of("test_manager").collect(toList()));
                }else if(workspaceName.contains("测试")){
                    addMemberRequest.setRoleIds(Stream.of("test_user").collect(toList()));
                }else {
                    addMemberRequest.setRoleIds(Stream.of("test_viewer").collect(toList()));
                }
                userService.addMember(addMemberRequest);
                user.setLastWorkspaceId(ws.getId());
            }

        }

        // 执行 ShiroDBRealm 中 LDAP 登录逻辑
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(userId);
        return userService.login(loginRequest);
    }

    @PostMapping("/test/connect")
    public void testConnect() {
        ldapService.testConnect();
    }

    @PostMapping("/test/login")
    public void testLogin(@RequestBody LoginRequest request) {
        ldapService.authenticate(request);
    }

    @GetMapping("/open")
    public boolean isOpen() {
        return ldapService.isOpen();
    }

}
