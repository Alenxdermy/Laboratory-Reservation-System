package com.lab.controller;

import com.lab.common.Guard;
import com.lab.common.Result;
import com.lab.entity.Lab;
import com.lab.service.LabService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实验室管理接口
 */
@RestController
@RequestMapping("/api/labs")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping
    public Result<List<Lab>> list() {
        return Result.ok(labService.list());
    }

    @GetMapping("/{id}")
    public Result<Lab> detail(@PathVariable Integer id) {
        return Result.ok(labService.getById(id));
    }

    @PostMapping
    public Result<Lab> create(@RequestBody Lab lab,
                              @RequestAttribute("uid") Integer uid,
                              @RequestAttribute("username") String username,
                              @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        return Result.ok("新增成功", labService.create(lab, uid, username));
    }

    @PutMapping("/{id}")
    public Result<Lab> update(@PathVariable Integer id, @RequestBody Lab lab,
                              @RequestAttribute("uid") Integer uid,
                              @RequestAttribute("username") String username,
                              @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        lab.setId(id);
        return Result.ok("修改成功", labService.update(lab, uid, username));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @RequestAttribute("uid") Integer uid,
                               @RequestAttribute("username") String username,
                               @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        labService.delete(id, uid, username);
        return Result.ok("删除成功", null);
    }
}
