package com.yanan.test.junit.extension;

import java.io.File;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.utils.resource.ResourceManager;

/**
 * Plugin 测试环境上下文加载器
 * @author yanan
 *
 */
public class PluginTestContextLoader implements BeforeAllCallback{
	private Logger logger =LoggerFactory.getLogger(PluginTestContextLoader.class);
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		logger.info("plugin test frame snapshot version");
		//设置测试上下文类路径为主类路径
		String classPath = this.getClass().getResource(".").getPath();
		String packagePath = this.getClass().getPackage().getName().replace(".", File.separator);
		classPath = classPath.substring(0,classPath.indexOf(packagePath));
		logger.info("test environment classpath :"+classPath);
		ResourceManager.setClassPath(classPath, 0);
		logger.info("start plugin");
		long start = System.currentTimeMillis();
		//初始化Plugin
		PlugsFactory.init();
		long times = System.currentTimeMillis()-start;
		logger.info("plugin started at ["+times+" ms]");
	}


}
