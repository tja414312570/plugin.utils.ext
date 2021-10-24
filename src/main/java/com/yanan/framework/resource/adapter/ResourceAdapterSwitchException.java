package com.yanan.framework.resource.adapter;

public class ResourceAdapterSwitchException extends RuntimeException {

	public ResourceAdapterSwitchException(String msg, Exception e) {
		super(msg,e);
	}
	public ResourceAdapterSwitchException( Exception e) {
		super("failed to parse resource!",e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7396643672663022435L;

}
