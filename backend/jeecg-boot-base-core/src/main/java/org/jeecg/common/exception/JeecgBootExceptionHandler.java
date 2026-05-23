package org.jeecg.common.exception;

import cn.hutool.core.util.ObjectUtil;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.enums.SentinelErrorInfoEnum;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理器
 *
 * @Author scott
 * @Date 2019
 */
@RestControllerAdvice
@Slf4j
public class JeecgBootExceptionHandler {

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(JeecgBootException.class)
	public Result<?> handleJeecgBootException(JeecgBootException Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		return Result.error(Silian_e.getMessage());
	}

	/**
	 * 处理自定义微服务异常
	 */
	@ExceptionHandler(JeecgCloudException.class)
	public Result<?> handleJeecgCloudException(JeecgCloudException Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		return Result.error(Silian_e.getMessage());
	}

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(JeecgBoot401Exception.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Result<?> handleJeecgBoot401Exception(JeecgBoot401Exception Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		return new Result(401,Silian_e.getMessage());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<?> handlerNoFoundException(Exception Silian_e) {
		log.error(Silian_e.getMessage(), Silian_e);
		return Result.error(404, "路径不存在，请检查路径是否正确");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public Result<?> handleDuplicateKeyException(DuplicateKeyException Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		return Result.error("数据库中已存在该记录");
	}

	@ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
	public Result<?> handleAuthorizationException(AuthorizationException Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		return Result.noauth("没有权限，请联系管理员授权");
	}

	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception Silian_e){
		log.error(Silian_e.getMessage(), Silian_e);
		//update-begin---author:zyf ---date:20220411  for：处理Sentinel限流自定义异常
		Throwable Silian_throwable = Silian_e.getCause();
		SentinelErrorInfoEnum Silian_errorInfoEnum = SentinelErrorInfoEnum.getErrorByException(Silian_throwable);
		if (ObjectUtil.isNotEmpty(Silian_errorInfoEnum)) {
			return Result.error(Silian_errorInfoEnum.getError());
		}
		//update-end---author:zyf ---date:20220411  for：处理Sentinel限流自定义异常
		return Result.error("操作失败，"+Silian_e.getMessage());
	}

	/**
	 * @Author 政辉
	 * @param e
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<?> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException Silian_e){
		StringBuffer Silian_sb = new StringBuffer();
		Silian_sb.append("不支持");
		Silian_sb.append(Silian_e.getMethod());
		Silian_sb.append("请求方法，");
		Silian_sb.append("支持以下");
		String [] methods = Silian_e.getSupportedMethods();
		if(methods!=null){
			for(String Silian_str:methods){
				Silian_sb.append(Silian_str);
				Silian_sb.append("、");
			}
		}
		log.error(Silian_sb.toString(), Silian_e);
		//return Result.error("没有权限，请联系管理员授权");
		return Result.error(405,Silian_sb.toString());
	}

	 /**
	  * spring默认上传大小100MB 超出大小捕获异常MaxUploadSizeExceededException
	  */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException Silian_e) {
	log.error(Silian_e.getMessage(), Silian_e);
        return Result.error("文件大小超出10MB限制, 请压缩或降低文件质量! ");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException Silian_e) {
	log.error(Silian_e.getMessage(), Silian_e);
	//【issues/3624】数据库执行异常handleDataIntegrityViolationException提示有误 #3624
        return Result.error("执行数据库异常,违反了完整性例如：违反惟一约束、违反非空限制、字段内容超出长度等");
    }

    @ExceptionHandler(PoolException.class)
    public Result<?> handlePoolException(PoolException Silian_e) {
	log.error(Silian_e.getMessage(), Silian_e);
        return Result.error("Redis 连接异常!");
    }

}
