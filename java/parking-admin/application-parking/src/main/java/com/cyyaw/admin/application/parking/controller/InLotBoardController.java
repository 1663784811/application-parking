package com.cyyaw.admin.application.parking.controller;

import com.cyyaw.admin.application.parking.service.InLotBoardService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.InLotBoardDTO;
import com.cyyaw.admin.entity.dto.parking.InLotBoardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "在场车辆看板")
@RestController
@RequestMapping("/admin/parking/board")
public class InLotBoardController {

    @Autowired
    private InLotBoardService inLotBoardService;

    @Operation(summary = "在场车辆看板", description = "在场车辆列表（含实时预估费用）+ 在场总数 + 总车位；只读，不生成订单。以 InLotBoardDTO 接收查询参数")
    @GetMapping("/inLot")
    public BaseResult<InLotBoardVO> inLot(InLotBoardDTO query) {
        return BaseResult.ok(inLotBoardService.loadInLotBoard(query.getParkingId(), query.getCarNumber()));
    }
}
