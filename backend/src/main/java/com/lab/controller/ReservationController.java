package com.lab.controller;

import com.lab.common.Guard;
import com.lab.common.Result;
import com.lab.entity.Reservation;
import com.lab.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预约接口：提交、我的预约、审核列表、审核、取消
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Result<Reservation> submit(@RequestBody Reservation reservation,
                                      @RequestAttribute("uid") Integer uid,
                                      @RequestAttribute("username") String username) {
        return Result.ok("预约提交成功，等待审核",
                reservationService.submit(reservation, uid, username));
    }

    @GetMapping("/my")
    public Result<List<Reservation>> my(@RequestAttribute("uid") Integer uid) {
        return Result.ok(reservationService.listByUser(uid));
    }

    @GetMapping
    public Result<List<Reservation>> list(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) Integer labId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        return Result.ok(reservationService.list(status, labId, keyword));
    }

    @PutMapping("/{id}/audit")
    public Result<Reservation> audit(@PathVariable Integer id,
                                     @RequestBody Map<String, String> body,
                                     @RequestAttribute("uid") Integer uid,
                                     @RequestAttribute("username") String username,
                                     @RequestAttribute("role") String role) {
        Guard.requireManager(role);
        return Result.ok("审核完成",
                reservationService.audit(id, body.get("status"), uid, username));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Integer id,
                               @RequestAttribute("uid") Integer uid,
                               @RequestAttribute("role") String role) {
        reservationService.cancel(id, uid, role);
        return Result.ok("已取消预约", null);
    }
}
