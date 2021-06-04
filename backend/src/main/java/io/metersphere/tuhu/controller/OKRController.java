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

    @GetMapping("/getCurrentOKR")
    public List<TuhuOKRDisplayDTO> getOKR() {
        return okrService.getCurrentOKR();
    }

    @GetMapping("/getOKR/{okrName}")
    public List<TuhuOKRDisplayDTO> getOKR(@PathVariable String okrName) {
        return okrService.getOKRByName(okrName);
    }

    @GetMapping("/getOKRNames")
    public List<String> getOKRNames() {
        return okrService.getOKRNames();
    }


    @PostMapping(value = "/updateOKR")
    public String update(@RequestBody OKRRequest request) {
        return okrService.updateOKR(request);
    }

}
