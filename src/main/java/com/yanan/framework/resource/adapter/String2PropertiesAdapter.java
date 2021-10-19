package com.yanan.framework.resource.adapter;

import java.util.Properties;

import com.typesafe.config.Config;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;

@Adapter(input = {},target = Properties.class)
@Register
public class String2PropertiesAdapter implements ResourceAdapter<Config, Properties> {

	@Override
	public Properties parse(Config input) {
		Properties properties = new Properties();
		
		return null;
	}

}
