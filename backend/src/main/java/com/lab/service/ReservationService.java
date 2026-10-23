package com.lab.service;

import com.lab.common.BizException;
import com.lab.dao.LabDao;
import com.lab.dao.ReservationDao;
import com.lab.entity.Lab;
import com.lab.entity.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务：提交预约、冲突/超员检测、审核
 */
@Service
public class ReservationService {

    /** 审核通过 / 拒绝 */
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";
    private static final String STATUS_PENDING = "pending";

    private final ReservationDao reservationDao;
    private final LabDao labDao;
    private final LogService logService;

    public ReservationService(ReservationDao reservationDao, LabDao labDao, LogService logService) {
        this.reservationDao = reservationDao;
        this.labDao = labDao;
        this.logService = logService;
    }

    /**
     * 提交预约
     * 校验：实验室可用、时间合法、人数不超过容量、同时间段累计人数不超容量
     */
    @Transactional(rollbackFor = Exception.class)
    public Reservation submit(Reservation form, Integer userId, String username) {
        if (form.getLabId() == null) {
            throw new BizException("请选择实验室");
        }
        Lab lab = labDao.findById(form.getLabId());
        if (lab == null) {
            throw new BizException("实验室不存在");
        }
        if (!"available".equals(lab.getStatus())) {
            throw new BizException("该实验室正在维护，暂不可预约");
        }
        LocalDateTime start = form.getStartTime();
        LocalDateTime end = form.getEndTime();
        if (start == null || end == null) {
            throw new BizException("预约开始时间和结束时间不能为空");
        }
        if (!end.isAfter(start)) {
            throw new BizException("结束时间必须晚于开始时间");
        }
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new BizException("不能预约过去的时间段");
        }
        int people = form.getPeople() == null ? 1 : form.getPeople();
        if (people <= 0) {
            throw new BizException("使用人数必须大于 0");
        }
        if (people > lab.getCapacity()) {
            throw new BizException("使用人数超过实验室容量（" + lab.getCapacity() + " 人）");
        }

        int used = reservationDao.sumOverlappingPeople(lab.getId(), start, end, null);
        if (used + people > lab.getCapacity()) {
            throw new BizException("该时间段实验室剩余容量不足（已预约 " + used
                    + " 人，容量 " + lab.getCapacity() + " 人）");
        }

        form.setUserId(userId);
        form.setPeople(people);
        form.setStatus(STATUS_PENDING);
        int id = reservationDao.insert(form);
        if (id <= 0) {
            throw new BizException("预约提交失败");
        }
        logService.record(userId, username, "提交预约",
                "实验室：" + lab.getName() + "，时间：" + start + " ~ " + end + "，人数：" + people);
        return reservationDao.findById(id);
    }

    public List<Reservation> listByUser(Integer userId) {
        return reservationDao.findByUser(userId);
    }

    public List<Reservation> list(String status, Integer labId, String keyword) {
        return reservationDao.findAll(status, labId, keyword);
    }

    /**
     * 审核预约：仅待审核状态可操作；通过时再次校验容量，防止审核期间超员
     */
    @Transactional(rollbackFor = Exception.class)
    public Reservation audit(Integer id, String status, Integer operatorId, String operatorName) {
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            throw new BizException("审核状态非法，仅支持 approved / rejected");
        }
        Reservation reservation = reservationDao.findById(id);
        if (reservation == null) {
            throw new BizException(404, "预约记录不存在");
        }
        if (!STATUS_PENDING.equals(reservation.getStatus())) {
            throw new BizException("该预约已审核，不能重复操作");
        }
        if (STATUS_APPROVED.equals(status)) {
            Lab lab = labDao.findById(reservation.getLabId());
            int used = reservationDao.sumOverlappingPeople(reservation.getLabId(),
                    reservation.getStartTime(), reservation.getEndTime(), reservation.getId());
            int people = reservation.getPeople() == null ? 1 : reservation.getPeople();
            if (lab != null && used + people > lab.getCapacity()) {
                throw new BizException("审核失败：该时段实验室容量不足（已占用 " + used
                        + " 人，容量 " + lab.getCapacity() + " 人）");
            }
        }
        reservationDao.updateStatus(id, status);
        logService.record(operatorId, operatorName,
                STATUS_APPROVED.equals(status) ? "审核通过预约" : "拒绝预约",
                "预约ID：" + id + "，实验室：" + reservation.getLabName());
        return reservationDao.findById(id);
    }

    /**
     * 取消预约：学生/教师只能取消自己的待审核预约，管理员可取消任意记录
     */
    public void cancel(Integer id, Integer userId, String role) {
        Reservation reservation = reservationDao.findById(id);
        if (reservation == null) {
            throw new BizException(404, "预约记录不存在");
        }
        boolean isAdmin = "admin".equals(role);
        if (!isAdmin && !reservation.getUserId().equals(userId)) {
            throw new BizException(403, "无权取消他人的预约");
        }
        if (!STATUS_PENDING.equals(reservation.getStatus()) && !isAdmin) {
            throw new BizException("仅待审核的预约可以取消");
        }
        reservationDao.deleteById(id);
        logService.record(userId, reservation.getUsername(), "取消预约", "预约ID：" + id);
    }
}
