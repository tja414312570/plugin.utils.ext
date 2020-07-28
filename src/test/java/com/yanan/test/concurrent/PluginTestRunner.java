package com.yanan.test.concurrent;

import com.yanan.utils.resource.ResourceManager;

public class PluginTestRunner{
	public static void main(String[] args) {
		System.out.println(ResourceManager.getResource("classpath*:plugin.yc"));
	}

}
