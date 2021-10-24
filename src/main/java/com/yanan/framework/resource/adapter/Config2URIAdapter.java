package com.yanan.framework.resource.adapter;

import java.net.URI;
import java.net.URISyntaxException;

import com.typesafe.config.impl.ConfigString;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;

@Adapter(input = {ConfigString.class},target = URI.class)
@Register
public class Config2URIAdapter implements ResourceAdapter<ConfigString, URI> {

	@Override
	public URI parse(ConfigString input) {
		try {
			URI url = new URI(input.unwrapped());
			return url;
		} catch (URISyntaxException e) {
			throw new ResourceAdapterSwitchException("failed to parse resource from string ["+input+"] to url",e);
		}
	}

}
