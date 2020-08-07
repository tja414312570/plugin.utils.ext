package com.yanan.framework.resource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.utils.resource.Resource;

@Register(attribute= {"http","https"},model=ProxyModel.CGLIB)
public class HttpResourceLoader implements ResourceLoader{

	private Logger logger;
	@Override
	public Resource getResource(String urlPath) {
		try {
			logger.info("request http resource ["+urlPath+"]");
			HttpURLConnection httpURLConnection = get(urlPath);
			Map<String, List<String>> responseHeaderFields = httpURLConnection.getHeaderFields();
			responseHeaderFields.forEach((key,value)->{
				logger.info("response header:"+key+"="+value);
			});
			//			System.out.println(httpURLConnection.getInputStream().available());
			// 5. 如果返回值正常，数据在网络中是以流的形式得到服务端返回的数据
//			if (code != 200) 
//				throw new 
				// 正常响应
			return new HttpResource(httpURLConnection);
		} catch (Exception e) {
			throw new ResourceLoaderException("failed get http resource at url:"+urlPath,e);
		}
	}
	public HttpURLConnection get(String urlPath) throws IOException{
		URL url = new URL(urlPath);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		  httpURLConnection.setDoOutput(false);
          // 设置是否从httpUrlConnection读入
          httpURLConnection.setDoInput(true);
          // 设置请求方式　默认为GET
          httpURLConnection.setRequestMethod("GET");
          // 设置是否使用缓存
          httpURLConnection.setUseCaches(true);
          // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
          httpURLConnection.setInstanceFollowRedirects(true);
          // 设置超时时间
          httpURLConnection.setConnectTimeout(3000);
          //打印请求头信息
          Map<String, List<String>> requerstProperties = httpURLConnection.getRequestProperties();
			requerstProperties.forEach((key,value)->{
				logger.info("request header:"+key+"="+value);
			});
          // 连接
          httpURLConnection.connect();
		return httpURLConnection;
		
	}

}
