package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.FeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * FeeController 消费管理接口层
 * 提供学费缴纳、校园卡消费、月度报表、异常预警接口
 * 
 * 接口列表：
 * - POST /api/fee/recordTuitionFee - 记录学费缴纳（仅管理员）
 * - POST /api/fee/recordCampusCardFee - 记录校园卡消费（管理员或学生本人）
 * - POST /api/fee/generateMonthlyReport - 生成月度报表（管理员或学生本人）
 * - POST /api/fee/detectAbnormalConsumption - 预警异常消费（管理员或学生本人）
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fee")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    /**
     * 记录学费缴纳
     * @param dataRequest 包含 personId, day, money
     * @return 记录 ID
     */
    @PostMapping("/recordTuitionFee")
    public DataResponse recordTuitionFee(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.recordTuitionFee(dataRequest);
    }

    /**
     * 记录校园卡消费
     * @param dataRequest 包含 personId, day, money
     * @return 记录 ID
     */
    @PostMapping("/recordCampusCardFee")
    public DataResponse recordCampusCardFee(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.recordCampusCardFee(dataRequest);
    }

    /**
     * 生成月度报表
     * @param dataRequest 包含 personId, month（格式：2026-04）
     * @return 月度消费统计报表
     */
    @PostMapping("/generateMonthlyReport")
    public DataResponse generateMonthlyReport(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.generateMonthlyReport(dataRequest);
    }

    /**
     * 预警异常消费
     * @param dataRequest 包含 personId, day, threshold（可选，默认 500）
     * @return 异常检测结果
     */
    @PostMapping("/detectAbnormalConsumption")
    public DataResponse detectAbnormalConsumption(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.detectAbnormalConsumption(dataRequest);
    }

    /**
     * 查询消费账单列表
     * @param dataRequest 包含 personId（可选）, type（可选）, month（可选）
     * @return 消费账单列表
     */
    @PostMapping("/list")
    public DataResponse getFeeList(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.getFeeList(dataRequest);
    }

    /**
     * 删除消费记录
     * @param dataRequest 包含 feeId
     * @return 删除结果
     */
    @PostMapping("/delete")
    public DataResponse deleteFee(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.deleteFee(dataRequest);
    }

    /**
     * 修改消费记录
     * @param dataRequest 包含 feeId, day, money
     * @return 修改结果
     */
    @PostMapping("/update")
    public DataResponse updateFee(@Valid @RequestBody DataRequest dataRequest) {
        return feeService.updateFee(dataRequest);
    }
}
