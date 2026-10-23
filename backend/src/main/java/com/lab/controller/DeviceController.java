package com.lab.controller;

import com.lab.common.Guard;
import com.lab.common.Result;
import com.lab.entity.Device;
import com.lab.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备管理接口
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public Result<List<Device>> list(@RequestParam(required = false) Integer labId,
                                     @RequestParam(required = false) String keyword) {
        return Result.ok(deviceService.list(labId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Device> detail(@PathVariable Integer id) {
        return Result.ok(deviceService.getById(id));
    }

    @PostMapping
    public Result<Device> create(@RequestBody Device device,
                                 @RequestAttribute("uid") Integer uid,
                                 @RequestAttribute("username") String username,
                                 @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        return Result.ok("新增成功", deviceService.create(device, uid, username));
    }

    @PutMapping("/{id}")
    public Result<Device> update(@PathVariable Integer id, @RequestBody Device device,
                                 @RequestAttribute("uid") Integer uid,
                                 @RequestAttribute("username") String username,
                                 @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        device.setId(id);
        return Result.ok("修改成功", deviceService.update(device, uid, username));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @RequestAttribute("uid") Integer uid,
                               @RequestAttribute("username") String username,
                               @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        deviceService.delete(id, uid, username);
        return Result.ok("删除成功", null);
    }
}
