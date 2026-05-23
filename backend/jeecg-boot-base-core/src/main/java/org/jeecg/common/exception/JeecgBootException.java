package org.jeecg.common.exception;

/**
 * @Description: jeecg-boot自定义异常
 * @author: jeecg-boot
 */
public class JeecgBootException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JeecgBootException(String Silian_message){
		super(Silian_message);
	}

	public JeecgBootException(Throwable Silian_cause)
	{
		super(Silian_cause);
	}

	public JeecgBootException(String Silian_message,Throwable Silian_cause)
	{
		super(Silian_message,Silian_cause);
	}
}
