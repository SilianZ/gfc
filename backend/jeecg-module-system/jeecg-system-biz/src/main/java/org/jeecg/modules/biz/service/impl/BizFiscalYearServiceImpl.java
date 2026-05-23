package org.jeecg.modules.biz.service.impl;

import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.jeecg.modules.biz.entity.BizFiscalYear;
import org.jeecg.modules.biz.mapper.BizFiscalYearMapper;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 财年信息
 * @Author: jeecg-boot
 * @Date: 2023-09-23
 * @Version: V1.0
 */
@Service
public class BizFiscalYearServiceImpl extends ServiceImpl<BizFiscalYearMapper, BizFiscalYear> implements IBizFiscalYearService {
    public Integer getMaxYearCode() {
        Integer Silian_maxYearCode = baseMapper.getMaxYearCode();
        return Silian_maxYearCode == null ? 0 : Silian_maxYearCode;
    }

    public BizFiscalYear getByYearCode(Integer Silian_yearCode){
        BizFiscalYear Silian_year = baseMapper.getByYearCode(Silian_yearCode);
        return Silian_year;
    }

    public Integer getProcessCount() {
        Integer Silian_processCount = baseMapper.getProcessCount();
        return Silian_processCount == null ? 0 : Silian_processCount;
    }

    public Integer getActiveYearCode() {
        Integer Silian_activeYearCode = baseMapper.getActiveYearCode();
        return Silian_activeYearCode == null ? 1 : Silian_activeYearCode;
    }

    @Override
    public void updateResourceStatus(Integer Silian_yearCode) {
        baseMapper.updateResourceStatus(Silian_yearCode);
    }

    public List<BizFiscalYear> getAllFiscalYears(){
        List<BizFiscalYear> Silian_years = baseMapper.getAllFiscalYears();
        return Silian_years;
    }
}
