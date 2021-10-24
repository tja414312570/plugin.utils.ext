package com.yanan.framework.resource.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;

@Adapter(input = {String.class},target = URI.class)
@Register
public class String2URIAdapter implements ResourceAdapter<String, URI> {

	@Override
	public URI parse(String input) {
		try {
			URI url = new URI(input);
			return url;
		} catch (URISyntaxException e) {
			throw new ResourceAdapterSwitchException("failed to parse resource from string ["+input+"] to url",e);
		}
	}

}
