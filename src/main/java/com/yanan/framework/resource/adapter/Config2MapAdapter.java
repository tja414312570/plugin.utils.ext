package com.yanan.framework.resource.adapter;

import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import com.typesafe.config.impl.SimpleConfigObject;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;

@Adapter(input = {Config.class,SimpleConfigObject.class},target = Map.class)
@Register
public class Config2MapAdapter implements ResourceAdapter<SimpleConfigObject, Map<Object,Object>> {

	@Override
	public Map<Object,Object> parse(SimpleConfigObject input) {
		Map<Object,Object> properties = new HashMap<>();
			input.entrySet().forEach(item->{
				parse(item.getKey(),properties,item.getValue());
			});
		return properties;
	}

	private void parse(String key, Map<Object,Object> properties, ConfigValue value) {
		Object storeValue = value.unwrapped();
		if(value.valueType() == ConfigValueType.OBJECT) {
			Map<Object,Object> childHashMap = new HashMap<>();
			((SimpleConfigObject)value).entrySet().forEach(item->{
				parse(item.getKey(),childHashMap,item.getValue());
			});
			storeValue = childHashMap;
		}
		properties.put(key, storeValue);
	}

}
