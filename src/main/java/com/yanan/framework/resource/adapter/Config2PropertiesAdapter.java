package com.yanan.framework.resource.adapter;

import java.util.Properties;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import com.typesafe.config.impl.SimpleConfigObject;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;

@Adapter(input = {Config.class,SimpleConfigObject.class},target = Properties.class)
@Register
public class Config2PropertiesAdapter implements ResourceAdapter<SimpleConfigObject, Properties> {

	@Override
	public Properties parse(SimpleConfigObject input) {
		Properties properties = new Properties();
			input.entrySet().forEach(item->{
				parse(item.getKey(),properties,item.getValue());
			});
		return properties;
	}

	private void parse(String key, Properties properties, ConfigValue value) {
		if(value.valueType() == ConfigValueType.OBJECT) {
			((SimpleConfigObject)value).entrySet().forEach(item->{
				parse(key+"."+item.getKey(),properties,item.getValue());
			});
		}else {
			properties.put(key, value.unwrapped());
		}
	}

}
