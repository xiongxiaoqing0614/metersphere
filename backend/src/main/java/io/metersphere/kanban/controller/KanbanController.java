package io.metersphere.kanban.controller;

import io.metersphere.kanban.dto.KanbanDTO;
import io.metersphere.kanban.service.KanbanService;
import io.metersphere.service.CheckPermissionService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/kanban")
public class KanbanController {

    @Resource
    private KanbanService kanbanService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("summary")
    public List<KanbanDTO> dashboardSummary() {
        return kanbanService.getSummary();
    }

}
