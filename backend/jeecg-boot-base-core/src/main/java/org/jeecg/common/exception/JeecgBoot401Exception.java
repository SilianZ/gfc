package org.jeecg.common.exception;

/**
 * @Description: jeecg-boot自定义401异常
 * @author: jeecg-boot
 */
public class JeecgBoot401Exception extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JeecgBoot401Exception(String Silian_message){
		super(Silian_message);
	}

	public JeecgBoot401Exception(Throwable Silian_cause)
	{
		super(Silian_cause);
	}

	public JeecgBoot401Exception(String Silian_message, Throwable Silian_cause)
	{
		super(Silian_message,Silian_cause);
	}
}
