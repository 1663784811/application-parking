package com.cyyaw.admin.application.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.CostUtil;
import com.cyyaw.admin.application.parking.service.InLotBoardService;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.application.parking.service.PkParkingService;
import com.cyyaw.admin.dao.parking.PkCarLogDao;
import com.cyyaw.admin.dao.parking.PkCostRulesDao;
import com.cyyaw.admin.entity.dto.parking.InLotBoardVO;
import com.cyyaw.admin.entity.dto.parking.InLotCarVO;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import com.cyyaw.admin.entity.module.parking.PkCostRules;
import com.cyyaw.admin.entity.module.parking.PkParking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在场车辆看板：只读预估，不建订单、不写库。
 * <p>
 * 与 H5 出场查询共用 {@link CostUtil} 这一套计费引擎，保证看板上的预估价
 * 和车主扫码看到的金额口径一致。
 */
@Service
public class InLotBoardServiceImpl implements InLotBoardService {

    /** 单次最多返回的车辆数；超出部分不画格子，由 inLotTotal / truncated 告知前端 */
    private static final int MAX_BOARD_SIZE = 500;

    /** 停车记录状态：0 场内 */
    private static final int STATUS_IN = 0;

    @Autowired
    private PkCarLogService pkCarLogService;

    @Autowired
    private PkCarLogDao pkCarLogDao;

    @Autowired
    private PkParkingService pkParkingService;

    @Autowired
    private PkCostRulesDao pkCostRulesDao;

    @Override
    public InLotBoardVO loadInLotBoard(Long parkingId, String carNumber) {
        InLotBoardVO vo = new InLotBoardVO();
        if (parkingId == null) {
            // 未选停车场：返回空看板，由前端走空态
            vo.setCapacity(0);
            vo.setInLotTotal(0);
            vo.setMatchedCount(0);
            vo.setTruncated(false);
            vo.setRuleConfigured(false);
            vo.setList(Collections.emptyList());
            return vo;
        }

        PkParking parking = pkParkingService.findById(parkingId);
        vo.setCapacity(parking == null || parking.getCapacity() == null ? 0 : parking.getCapacity());

        // 费率每请求只取一次，随后逐车在内存里算，避免每辆车查一次规则
        List<PkCostRules> rules = pkCostRulesDao.selectByParkingId(parkingId);
        vo.setRuleConfigured(rules != null && !rules.isEmpty());

        QueryWrapper<PkCarLog> wrapper = new QueryWrapper<>();
        wrapper.eq("parking_id", parkingId);
        wrapper.eq("status", STATUS_IN);
        // 看板不显示逻辑删除的记录（列表接口未过滤，这里显式排除，避免"幽灵车"）
        wrapper.eq("del_time", 0);
        if (carNumber != null && !carNumber.isBlank()) {
            wrapper.like("car_number", carNumber.trim());
        }
        // 停得最久的排前面：它最该被关注
        wrapper.orderByAsc("entry_time");

        Page<PkCarLog> pageResult = pkCarLogService.findPage(1, MAX_BOARD_SIZE, wrapper);
        List<PkCarLog> carLogs = pageResult.getRecords() == null
                ? Collections.emptyList()
                : pageResult.getRecords();

        // 在场总数按全场口径单独统计：车牌筛选时若直接取分页 total，
        // 占用率会跟着筛选结果一起掉下来（老页面的口径问题）
        vo.setInLotTotal((int) countInLot(parkingId));
        vo.setMatchedCount((int) pageResult.getTotal());
        vo.setTruncated(pageResult.getTotal() > carLogs.size());

        LocalDateTime now = LocalDateTime.now();
        List<InLotCarVO> list = new ArrayList<>(carLogs.size());
        for (PkCarLog carLog : carLogs) {
            list.add(buildCarVO(carLog, rules, now));
        }
        vo.setList(list);
        return vo;
    }

    /**
     * 在场车辆总数：与列表用同一套过滤条件（status=0 且未逻辑删除），
     * 保证统计卡片上的数字和看板上画出来的车是同一口径。
     */
    private long countInLot(Long parkingId) {
        QueryWrapper<PkCarLog> countWrapper = new QueryWrapper<>();
        countWrapper.eq("parking_id", parkingId);
        countWrapper.eq("status", STATUS_IN);
        countWrapper.eq("del_time", 0);
        long total = pkCarLogDao.selectCount(countWrapper);
        return total;
    }

    /**
     * 单车预估：金额 = 入场时刻 → 当前时刻 的实时费用。
     * 请求时刻只取一次并传入，保证同一屏所有车用的是同一个计费时刻。
     */
    private InLotCarVO buildCarVO(PkCarLog carLog, List<PkCostRules> rules, LocalDateTime now) {
        LocalDateTime entryTime = carLog.getEntryTime() == null ? now : carLog.getEntryTime();
        InLotCarVO car = new InLotCarVO();
        car.setId(carLog.getId());
        car.setCarNumber(carLog.getCarNumber());
        car.setCarType(carLog.getCarType());
        car.setEntryTime(carLog.getEntryTime());
        car.setOutRecognizeTime(carLog.getOutRecognizeTime());
        car.setDuration(CostUtil.formatDuration(entryTime, now));
        car.setDurationMinutes(Duration.between(entryTime, now).toMinutes());
        // 出场摄像头已识别 = 待缴费（status 仍为 0，缴完费放行才写出场时间）
        car.setWaiting(carLog.getOutChannelId() != null);
        // 有无适用规则要区分开："真 0 元（免费时段）"与"压根没有适用费率"
        boolean matched = CostUtil.hasApplicableRules(carLog.getCarType(), rules, now);
        car.setRuleMatched(matched);
        car.setAmount(matched
                ? CostUtil.computeCost(entryTime, now, carLog.getCarType(), rules)
                : BigDecimal.ZERO);
        return car;
    }

}
