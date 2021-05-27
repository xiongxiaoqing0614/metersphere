package io.metersphere.tuhu.controller;


import io.metersphere.tuhu.dto.TuhuOKRDisplayDTO;
import io.metersphere.tuhu.service.KanbanService;
import io.metersphere.tuhu.service.OKRService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/tuhu/okr")
public class OKRController {

    @Resource
    private KanbanService kanbanService;

    @Resource
    private OKRService okrService;

    @GetMapping("/getOKR")
    public List<TuhuOKRDisplayDTO> getOKR() {
        return okrService.getAllOKR();
    }

//    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
//    public void create(@RequestPart("request") OKRRequest request) {
//        TuhuOKRDTO okrReqDTO = new TuhuOKRDTO();
//        BeanUtils.copyBean(okrReqDTO, request);
//        okrService.addOKR(okrReqDTO);
//    }

//    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
//    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
//    public ApiDefinitionWithBLOBs update(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
//        return okrService.update(request, bodyFiles);
//    }

}
