package com.yanan.framework.debug;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.handler.HandlerSet;
import com.yanan.framework.plugin.handler.InvokeHandler;
import com.yanan.framework.plugin.handler.MethodHandler;

//@Register
public class SnapshotHandler implements InvokeHandler{

	@Override
	public void before(MethodHandler methodHandler) {
		System.err.println(methodHandler.getMethod());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		Output byteBufferOutput = new Output(baos);
		new RuntimeException().printStackTrace();
		Kryo kryo = SerialUtils.getKryo();
		kryo.register(MethodHandler.class);
		HandlerSet handlerSet = methodHandler.getHandlerSet();
		methodHandler.setHandlerSet(null);
		kryo.writeClassAndObject(byteBufferOutput, methodHandler.getPlugsProxy().getProxyObject());
		methodHandler.setHandlerSet(handlerSet);
		byteBufferOutput.flush();
		System.out.println(baos);
		
		Input input = new Input(baos.toByteArray());
		methodHandler = (MethodHandler) kryo.readClassAndObject(input);
		System.out.println(methodHandler);
	}

	@Override
	public void after(MethodHandler methodHandler) {
		
	}

	@Override
	public void error(MethodHandler methodHandler, Throwable exception) {
		
	}
	
}
