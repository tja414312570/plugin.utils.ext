package com.yanan.framework.resource;

/**
 * 资源加载异常
 * @author yanan
 */
public class ResourceLoaderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5150543798830711755L;
	/**
	 * 抛出默认的资源异常
	 */
	public ResourceLoaderException() {
		super();
	}
	/**
	 * 抛出一个资源加载异常
	 * @param msg 消息
	 */
	public ResourceLoaderException(String msg) {
		super(msg);
	}
	/**
	 * 抛出一个资源加载异常
	 * @param msg 消息
	 * @param cause 原因
	 */
	public ResourceLoaderException(String msg,Throwable cause) {
		super(msg,cause);
	}

}
