package com.lab.service;

import com.lab.common.BizException;
import com.lab.dao.DeviceDao;
import com.lab.dao.LabDao;
import com.lab.entity.Device;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备管理服务
 */
@Service
public class DeviceService {

    private final DeviceDao deviceDao;
    private final LabDao labDao;
    private final LogService logService;

    public DeviceService(DeviceDao deviceDao, LabDao labDao, LogService logService) {
        this.deviceDao = deviceDao;
        this.labDao = labDao;
        this.logService = logService;
    }

    public List<Device> list(Integer labId, String keyword) {
        return deviceDao.findAll(labId, keyword);
    }

    public Device getById(Integer id) {
        Device device = deviceDao.findById(id);
        if (device == null) {
            throw new BizException(404, "设备不存在");
        }
        return device;
    }

    public Device create(Device device, Integer operatorId, String operatorName) {
        validate(device);
        device.setId(deviceDao.insert(device));
        logService.record(operatorId, operatorName, "新增设备",
                "设备：" + device.getName() + "，实验室ID：" + device.getLabId());
        return device;
    }

    public Device update(Device device, Integer operatorId, String operatorName) {
        if (device.getId() == null) {
            throw new BizException("设备 ID 不能为空");
        }
        getById(device.getId());
        validate(device);
        deviceDao.update(device);
        logService.record(operatorId, operatorName, "修改设备",
                "设备：" + device.getName() + "，状态：" + device.getStatus());
        return device;
    }

    public void delete(Integer id, Integer operatorId, String operatorName) {
        Device device = getById(id);
        deviceDao.deleteById(id);
        logService.record(operatorId, operatorName, "删除设备", "设备：" + device.getName());
    }

    private void validate(Device device) {
        if (device.getLabId() == null || labDao.findById(device.getLabId()) == null) {
            throw new BizException("所属实验室不存在");
        }
        if (device.getName() == null || device.getName().isBlank()) {
            throw new BizException("设备名称不能为空");
        }
        if (device.getStatus() == null || device.getStatus().isBlank()) {
            device.setStatus("available");
        }
        if (!"available".equals(device.getStatus())
                && !"occupied".equals(device.getStatus())
                && !"maintenance".equals(device.getStatus())) {
            throw new BizException("设备状态非法，仅支持 available / occupied / maintenance");
        }
    }
}
