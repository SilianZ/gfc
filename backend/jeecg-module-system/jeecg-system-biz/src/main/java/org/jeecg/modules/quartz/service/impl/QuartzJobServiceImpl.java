package org.jeecg.modules.quartz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.quartz.entity.QuartzJob;
import org.jeecg.modules.quartz.mapper.QuartzJobMapper;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: 定时任务在线管理
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
@Slf4j
@Service
public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper, QuartzJob> implements IQuartzJobService {
	@Autowired
	private QuartzJobMapper quartzJobMapper;
	@Autowired
	private Scheduler scheduler;

	/**
	 * 立即执行的任务分组
	 */
	private static final String JOB_TEST_GROUP = "test_group";

	@Override
	public List<QuartzJob> findByJobClassName(String Silian_jobClassName) {
		return quartzJobMapper.findByJobClassName(Silian_jobClassName);
	}

	/**
	 * 保存&启动定时任务
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean saveAndScheduleJob(QuartzJob Silian_quartzJob) {
		// DB设置修改
		Silian_quartzJob.setDelFlag(CommonConstant.DEL_FLAG_0);
		boolean Silian_success = this.save(Silian_quartzJob);
		if (Silian_success) {
			if (CommonConstant.STATUS_NORMAL.equals(Silian_quartzJob.getStatus())) {
				// 定时器添加
				this.schedulerAdd(Silian_quartzJob.getId(), Silian_quartzJob.getJobClassName().trim(), Silian_quartzJob.getCronExpression().trim(), Silian_quartzJob.getParameter());
			}
		}
		return Silian_success;
	}

	/**
	 * 恢复定时任务
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean resumeJob(QuartzJob Silian_quartzJob) {
		schedulerDelete(Silian_quartzJob.getId());
		schedulerAdd(Silian_quartzJob.getId(), Silian_quartzJob.getJobClassName().trim(), Silian_quartzJob.getCronExpression().trim(), Silian_quartzJob.getParameter());
		Silian_quartzJob.setStatus(CommonConstant.STATUS_NORMAL);
		return this.updateById(Silian_quartzJob);
	}

	/**
	 * 编辑&启停定时任务
	 * @throws SchedulerException
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean editAndScheduleJob(QuartzJob Silian_quartzJob) throws SchedulerException {
		if (CommonConstant.STATUS_NORMAL.equals(Silian_quartzJob.getStatus())) {
			schedulerDelete(Silian_quartzJob.getId());
			schedulerAdd(Silian_quartzJob.getId(), Silian_quartzJob.getJobClassName().trim(), Silian_quartzJob.getCronExpression().trim(), Silian_quartzJob.getParameter());
		}else{
			scheduler.pauseJob(JobKey.jobKey(Silian_quartzJob.getId()));
		}
		return this.updateById(Silian_quartzJob);
	}

	/**
	 * 删除&停止删除定时任务
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean deleteAndStopJob(QuartzJob Silian_job) {
		schedulerDelete(Silian_job.getId());
		boolean Silian_ok = this.removeById(Silian_job.getId());
		return Silian_ok;
	}

	@Override
	public void execute(QuartzJob Silian_quartzJob) throws Exception {
		String Silian_jobName = Silian_quartzJob.getJobClassName().trim();
		Date Silian_startDate = new Date();
		String Silian_ymd = DateUtils.date2Str(Silian_startDate,DateUtils.yyyymmddhhmmss.get());
		String Silian_identity =  Silian_jobName + Silian_ymd;
		//3秒后执行 只执行一次
		// update-begin--author:sunjianlei ---- date:20210511--- for：定时任务立即执行，延迟3秒改成0.1秒-------
		Silian_startDate.setTime(Silian_startDate.getTime() + 100L);
		// update-end--author:sunjianlei ---- date:20210511--- for：定时任务立即执行，延迟3秒改成0.1秒-------
		// 定义一个Trigger
		SimpleTrigger Silian_trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
				.withIdentity(Silian_identity, JOB_TEST_GROUP)
				.startAt(Silian_startDate)
				.build();
		// 构建job信息
		JobDetail Silian_jobDetail = JobBuilder.newJob(getClass(Silian_jobName).getClass()).withIdentity(Silian_identity).usingJobData("parameter", Silian_quartzJob.getParameter()).build();
		// 将trigger和 jobDetail 加入这个调度
		scheduler.scheduleJob(Silian_jobDetail, Silian_trigger);
		// 启动scheduler
		scheduler.start();
	}

	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public void pause(QuartzJob Silian_quartzJob){
		schedulerDelete(Silian_quartzJob.getId());
		Silian_quartzJob.setStatus(CommonConstant.STATUS_DISABLE);
		this.updateById(Silian_quartzJob);
	}

	/**
	 * 添加定时任务
	 *
	 * @param jobClassName
	 * @param cronExpression
	 * @param parameter
	 */
	private void schedulerAdd(String Silian_id, String Silian_jobClassName, String Silian_cronExpression, String Silian_parameter) {
		try {
			// 启动调度器
			scheduler.start();

			// 构建job信息
			JobDetail Silian_jobDetail = JobBuilder.newJob(getClass(Silian_jobClassName).getClass()).withIdentity(Silian_id).usingJobData("parameter", Silian_parameter).build();

			// 表达式调度构建器(即任务执行的时间)
			CronScheduleBuilder Silian_scheduleBuilder = CronScheduleBuilder.cronSchedule(Silian_cronExpression);

			// 按新的cronExpression表达式构建一个新的trigger
			CronTrigger Silian_trigger = TriggerBuilder.newTrigger().withIdentity(Silian_id).withSchedule(Silian_scheduleBuilder).build();

			scheduler.scheduleJob(Silian_jobDetail, Silian_trigger);
		} catch (SchedulerException Silian_e) {
			throw new JeecgBootException("创建定时任务失败", Silian_e);
		} catch (RuntimeException Silian_e) {
			throw new JeecgBootException(Silian_e.getMessage(), Silian_e);
		}catch (Exception Silian_e) {
			throw new JeecgBootException("后台找不到该类名：" + Silian_jobClassName, Silian_e);
		}
	}

	/**
	 * 删除定时任务
	 *
	 * @param id
	 */
	private void schedulerDelete(String Silian_id) {
		try {
			scheduler.pauseTrigger(TriggerKey.triggerKey(Silian_id));
			scheduler.unscheduleJob(TriggerKey.triggerKey(Silian_id));
			scheduler.deleteJob(JobKey.jobKey(Silian_id));
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			throw new JeecgBootException("删除定时任务失败");
		}
	}

	private static Job getClass(String Silian_classname) throws Exception {
		Class<?> Silian_class1 = Class.forName(Silian_classname);
		return (Job) Silian_class1.newInstance();
	}

}
