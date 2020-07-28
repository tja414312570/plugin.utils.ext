package com.yanan.test.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.PlugsFactory;

public class PluginTestRunner implements TestInstanceFactory{
	private Logger logger =LoggerFactory.getLogger(PluginTestRunner.class);

	@Override
	public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
			throws TestInstantiationException {
		//添加测试类注册定义
		Class<?> testClass = factoryContext.getTestClass();
		long start = System.currentTimeMillis();
		//初始化Plugin
		PlugsFactory.init();
		logger.info("prepared create test instance for "+testClass.getName());
		if(PlugsFactory.getInstance().getRegisterDefinition(testClass) == null)
			PlugsFactory.getInstance().addRegisterDefinition(testClass);
		Object testInstance = PlugsFactory.getPluginsInstance(testClass);
		long times = System.currentTimeMillis()-start;
		logger.info("instance test class success at ["+times+" ms],instance hash :"+testInstance.hashCode());
		return testInstance;
	}

}
