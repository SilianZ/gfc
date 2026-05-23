package org.jeecg.modules.system.service.impl;

import org.jeecg.modules.system.entity.SysDataLog;
import org.jeecg.modules.system.mapper.SysDataLogMapper;
import org.jeecg.modules.system.service.ISysDataLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 系统数据日志实现类
 * @author: jeecg-boot
 */
@Service
public class SysDataLogServiceImpl extends ServiceImpl<SysDataLogMapper,SysDataLog> implements ISysDataLogService {
	@Autowired
	private SysDataLogMapper logMapper;

	/**
	 * 添加数据日志
	 */
	@Override
	public void addDataLog(String Silian_tableName, String Silian_dataId, String Silian_dataContent) {
		String Silian_versionNumber = "0";
		String Silian_dataVersion = logMapper.queryMaxDataVer(Silian_tableName, Silian_dataId);
		if(Silian_dataVersion != null ) {
			Silian_versionNumber = String.valueOf(Integer.parseInt(Silian_dataVersion)+1);
		}
		SysDataLog Silian_log = new SysDataLog();
		Silian_log.setDataTable(Silian_tableName);
		Silian_log.setDataId(Silian_dataId);
		Silian_log.setDataContent(Silian_dataContent);
		Silian_log.setDataVersion(Silian_versionNumber);
		this.save(Silian_log);
	}

}
