package com.cyyaw.application.parking.gate.vehicle;

import com.cyyaw.application.parking.gate.common.R;
import com.cyyaw.application.parking.gate.vehicle.model.VehicleRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 车辆通行记录接口。
 * <ul>
 *   <li>GET /api/vehicle/records —— 返回车辆通行记录列表（需带登录 token）</li>
 * </ul>
 * <p>当前为静态数据（占位），后续替换为 MQTT 实时推送入库或数据库查询。</p>
 */
@RestController
@RequestMapping("/api/vehicle")
public class VehicleRecordController {

    /**
     * 返回一批静态车辆通行记录。
     */
    @GetMapping("/records")
    public R<List<VehicleRecord>> records() {
        return R.ok(staticRecords());
    }

    private List<VehicleRecord> staticRecords() {
        List<VehicleRecord> list = new ArrayList<>();
        list.add(new VehicleRecord(1, "粤B·8K321", "#G01 主入口", "in", "入场", "14:32:05", "正常", "normal"));
        list.add(new VehicleRecord(2, "京A·F88P2", "#G02 主出口", "out", "出场", "14:28:41", "正常", "normal"));
        list.add(new VehicleRecord(3, "沪C·V12M4", "#G03 地下车库", "in", "入场", "14:25:19", "正常", "normal"));
        list.add(new VehicleRecord(4, "粤B·T233Q", "#G05 南入口", "in", "入场", "14:22:57", "无牌识别", "warning"));
        list.add(new VehicleRecord(5, "苏A·D90K8", "#G02 主出口", "out", "出场", "14:18:33", "超时未缴费", "warning"));
        list.add(new VehicleRecord(6, "浙C·L56X1", "#G06 北出口", "out", "出场", "14:15:08", "正常", "normal"));
        list.add(new VehicleRecord(7, "川A·Z34W7", "#G01 主入口", "in", "入场", "14:11:44", "人工放行", "warning"));
        list.add(new VehicleRecord(8, "闽D·N77H9", "#G11 VIP通道", "in", "入场", "14:08:21", "正常", "normal"));
        list.add(new VehicleRecord(9, "鄂A·P19K3", "#G02 主出口", "out", "出场", "14:05:02", "黑名单车辆", "danger"));
        list.add(new VehicleRecord(10, "粤B·5M460", "#G04 应急通道", "in", "入场", "14:01:36", "拦截失败", "danger"));
        return list;
    }
}
