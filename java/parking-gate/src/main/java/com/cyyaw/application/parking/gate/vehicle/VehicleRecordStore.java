package com.cyyaw.application.parking.gate.vehicle;

import com.cyyaw.application.parking.gate.vehicle.model.VehicleRecord;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 车辆通行记录仓库（内存）。
 * <p>车牌识别回调写入本仓库，/api/vehicle/records 从本仓库读取，
 * 使门口端首页「车辆通行记录」面板真正反映实时回调事件。</p>
 * <p>启动时以一组静态记录做种子数据，保留 demo 体验；真实回调置顶插入，
 * 超过上限裁掉最旧记录，防止内存无限增长。</p>
 */
@Slf4j
@Component
public class VehicleRecordStore {

    /** 最多保留的记录条数。 */
    private static final int MAX = 200;

    private final CopyOnWriteArrayList<VehicleRecord> records = new CopyOnWriteArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        for (VehicleRecord r : seedRecords()) {
            records.add(r);
        }
        idCounter.set(records.size());
        log.info("加载车辆通行记录种子 {} 条", records.size());
    }

    /**
     * 追加一条记录：自动分配 id，置顶插入，超限裁尾。
     */
    public synchronized void add(VehicleRecord r) {
        r.setId(idCounter.incrementAndGet());
        records.add(0, r);
        while (records.size() > MAX) {
            records.remove(records.size() - 1);
        }
    }

    /**
     * 返回当前记录快照（最新在前）。
     */
    public List<VehicleRecord> list() {
        return new ArrayList<>(records);
    }

    private List<VehicleRecord> seedRecords() {
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
