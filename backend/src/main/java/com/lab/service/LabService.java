package com.lab.service;

import com.lab.common.BizException;
import com.lab.dao.DeviceDao;
import com.lab.dao.LabDao;
import com.lab.dao.ReservationDao;
import com.lab.entity.Lab;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实验室管理服务
 */
@Service
public class LabService {

    private final LabDao labDao;
    private final DeviceDao deviceDao;
    private final ReservationDao reservationDao;
    private final LogService logService;

    public LabService(LabDao labDao, DeviceDao deviceDao,
                      ReservationDao reservationDao, LogService logService) {
        this.labDao = labDao;
        this.deviceDao = deviceDao;
        this.reservationDao = reservationDao;
        this.logService = logService;
    }

    public List<Lab> list() {
        return labDao.findAll();
    }

    public Lab getById(Integer id) {
        Lab lab = labDao.findById(id);
        if (lab == null) {
            throw new BizException(404, "实验室不存在");
        }
        return lab;
    }

    public Lab create(Lab lab, Integer operatorId, String operatorName) {
        validate(lab);
        lab.setId(labDao.insert(lab));
        logService.record(operatorId, operatorName, "新增实验室", "实验室：" + lab.getName());
        return lab;
    }

    public Lab update(Lab lab, Integer operatorId, String operatorName) {
        if (lab.getId() == null) {
            throw new BizException("实验室 ID 不能为空");
        }
        getById(lab.getId());
        validate(lab);
        labDao.update(lab);
        logService.record(operatorId, operatorName, "修改实验室", "实验室：" + lab.getName());
        return lab;
    }

    public void delete(Integer id, Integer operatorId, String operatorName) {
        Lab lab = getById(id);
        if (deviceDao.countByLab(id) > 0 || reservationDao.countByLab(id) > 0) {
            throw new BizException("该实验室下存在设备或预约记录，无法删除");
        }
        labDao.deleteById(id);
        logService.record(operatorId, operatorName, "删除实验室", "实验室：" + lab.getName());
    }

    private void validate(Lab lab) {
        if (lab.getName() == null || lab.getName().isBlank()) {
            throw new BizException("实验室名称不能为空");
        }
        if (lab.getCapacity() == null || lab.getCapacity() <= 0) {
            throw new BizException("实验室容量必须大于 0");
        }
        if (lab.getStatus() == null || lab.getStatus().isBlank()) {
            lab.setStatus("available");
        }
        if (!"available".equals(lab.getStatus()) && !"maintenance".equals(lab.getStatus())) {
            throw new BizException("实验室状态非法，仅支持 available / maintenance");
        }
    }
}
