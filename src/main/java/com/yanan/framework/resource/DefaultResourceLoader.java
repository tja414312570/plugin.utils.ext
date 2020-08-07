package com.yanan.framework.resource;

import java.util.Objects;

import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.utils.resource.Resource;
import com.yanan.utils.resource.ResourceManager;

/**
 * 默认资源加载器，因为可以加载任意资源，需要设置优先级最高
 * @author yanan
 */
@Register(model=ProxyModel.CGLIB,priority = 127)
public class DefaultResourceLoader implements ResourceLoader{
	private static final String EMPTY_PATH_TOKEN = "";
	@Override
	public Resource getResource(String path) {
		Resource resource;
		if(Objects.equals(getClass(), DefaultResourceLoader.class)) {
			String pathToken = EMPTY_PATH_TOKEN;
			int pathTokenIndex = path.indexOf(':');
			if(pathTokenIndex != -1) {
				pathToken = path.substring(0,pathTokenIndex);
			}
			ResourceLoader resourceLoader = getResourceLoader(pathToken);
			resource = resourceLoader.getResource(path);
		}else {
			resource =  ResourceManager.getResource(path);
		}
		return resource;
	}
	public String getPath(String path) {
		int pathTokenIndex = path.indexOf(':');
		if(pathTokenIndex != -1)
			path = path.substring(pathTokenIndex+1);
		return path;
	}
	public String getPathToken(String path) {
		String pathToken = null;
		int pathTokenIndex = path.indexOf(':');
		if(pathTokenIndex != -1)
			pathToken = path.substring(0,pathTokenIndex);
		return pathToken;
	}
	public ResourceLoader getResourceLoader(String pathToken) {
		return PlugsFactory
				.getPluginsInstanceByAttributeStrict(ResourceLoader.class, pathToken);
	}
}
