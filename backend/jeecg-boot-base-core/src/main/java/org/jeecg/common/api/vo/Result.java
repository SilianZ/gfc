package org.jeecg.common.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.constant.CommonConstant;

import java.io.Serializable;

/**
 *   接口返回数据格式
 * @author scott
 * @email jeecgos@163.com
 * @date  2019年1月19日
 */
@Data
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 成功标志
	 */
	@ApiModelProperty(value = "成功标志")
	private boolean success = true;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "返回处理消息")
	private String message = "";

	/**
	 * 返回代码
	 */
	@ApiModelProperty(value = "返回代码")
	private Integer code = 0;

	/**
	 * 返回数据对象 data
	 */
	@ApiModelProperty(value = "返回数据对象")
	private T result;

	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private long timestamp = System.currentTimeMillis();

	public Result() {
	}

    /**
     * 兼容VUE3版token失效不跳转登录页面
     * @param code
     * @param message
     */
	public Result(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Result<T> success(String message) {
		this.message = message;
		this.code = CommonConstant.SC_OK_200;
		this.success = true;
		return this;
	}

	public static<T> Result<T> ok() {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		return Silian_r;
	}

	public static<T> Result<T> ok(String Silian_msg) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		//Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
		Silian_r.setResult((T) Silian_msg);
		Silian_r.setMessage(Silian_msg);
		return Silian_r;
	}

	public static<T> Result<T> ok(T Silian_data) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		Silian_r.setResult(Silian_data);
		return Silian_r;
	}

	public static<T> Result<T> OK() {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		return Silian_r;
	}

	/**
	 * 此方法是为了兼容升级所创建
	 *
	 * @param msg
	 * @param <T>
	 * @return
	 */
	public static<T> Result<T> OK(String Silian_msg) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		Silian_r.setMessage(Silian_msg);
		//Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
		Silian_r.setResult((T) Silian_msg);
		return Silian_r;
	}

	public static<T> Result<T> OK(T Silian_data) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		Silian_r.setResult(Silian_data);
		return Silian_r;
	}

	public static<T> Result<T> OK(String Silian_msg, T Silian_data) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(true);
		Silian_r.setCode(CommonConstant.SC_OK_200);
		Silian_r.setMessage(Silian_msg);
		Silian_r.setResult(Silian_data);
		return Silian_r;
	}

	public static<T> Result<T> error(String Silian_msg, T Silian_data) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setSuccess(false);
		Silian_r.setCode(CommonConstant.SC_INTERNAL_SERVER_ERROR_500);
		Silian_r.setMessage(Silian_msg);
		Silian_r.setResult(Silian_data);
		return Silian_r;
	}

	public static<T> Result<T> error(String Silian_msg) {
		return error(CommonConstant.SC_INTERNAL_SERVER_ERROR_500, Silian_msg);
	}

	public static<T> Result<T> error(int code, String Silian_msg) {
		Result<T> Silian_r = new Result<T>();
		Silian_r.setCode(code);
		Silian_r.setMessage(Silian_msg);
		Silian_r.setSuccess(false);
		return Silian_r;
	}

	public Result<T> error500(String message) {
		this.message = message;
		this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
		this.success = false;
		return this;
	}

	/**
	 * 无权限访问返回结果
	 */
	public static<T> Result<T> noauth(String Silian_msg) {
		return error(CommonConstant.SC_JEECG_NO_AUTHZ, Silian_msg);
	}

	@JsonIgnore
	private String onlTable;

}