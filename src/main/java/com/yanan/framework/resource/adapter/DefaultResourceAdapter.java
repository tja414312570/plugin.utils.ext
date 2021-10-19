package com.yanan.framework.resource.adapter;

import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;
import com.yanan.utils.reflect.TypeToken;

public class DefaultResourceAdapter<I,R> implements ResourceAdapter<I,R>{

	@Override
	public R parse(I input) {
		System.err.println(input);
		System.out.println(new TypeToken<R>() {}.getTypeClass());
		return null;
	}

}
