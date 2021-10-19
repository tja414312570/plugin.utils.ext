package com.yanan.framework.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;

import com.yanan.utils.resource.Resource;
import com.yanan.utils.string.StringUtil;

/**
 * HTTP资源
 * @author yanan
 *
 */
public class HttpResource implements Resource{
	/**
	 * 代理输入流
	 * @author yanan
	 *
	 */
	static class ProxyInputStream extends InputStream{
		private InputStream inputStream;
		public ProxyInputStream(HttpURLConnection httpURLConnction) throws IOException {
			super();
			this.httpURLConnction = httpURLConnction;
			this.inputStream = this.httpURLConnction.getInputStream();
		}

		private HttpURLConnection httpURLConnction;
		public int read() throws IOException{
			return inputStream.read();
		}

	    public int read(byte b[]) throws IOException {
	        return inputStream.read(b);
	    }

	    public int read(byte b[], int off, int len) throws IOException {
	    	return inputStream.read(b, off, len);
	    }
	    public long skip(long n) throws IOException {
	    	return inputStream.skip(n);
	    }

	    public int available() throws IOException {
	        return inputStream.available();
	    }
	    public void close() throws IOException {
	    	inputStream.close();
	    	httpURLConnction.disconnect();
	    }

	    public synchronized void mark(int readlimit) {
	    	inputStream.mark(readlimit);
	    }

	    public synchronized void reset() throws IOException {
	    	inputStream.reset();
	    }

	    public boolean markSupported() {
	        return inputStream.markSupported();
	    }

	}
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String FILE_NAME_TOKEN = "filename";
	private static final int SUB_INDEX_LEN = 9;
	private String name;
	private String path;
	private InputStream inputStream;
	private HttpURLConnection httpURLConnection;
	public HttpResource(HttpURLConnection httpURLConnection) throws IOException {
		super();
		this.httpURLConnection = httpURLConnection;
		this.inputStream = new ProxyInputStream(httpURLConnection);
		this.path = this.httpURLConnection.getURL().getPath();
		this.name = this.httpURLConnection.getURL().getFile();
		String cd = this.httpURLConnection.getHeaderField(CONTENT_DISPOSITION);
		int index = StringUtil.indexOf(cd,FILE_NAME_TOKEN);
		if(index > -1) {
			this.name = cd.substring(index+SUB_INDEX_LEN);
		}
	}
	public HttpURLConnection getHttpUrlConnection() {
		return this.httpURLConnection;
	}
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isDirect() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long lastModified() {
		return httpURLConnection.getLastModified();
	}

	@Override
	public long size() throws IOException {
		return httpURLConnection.getContentLengthLong();
	}

	@Override
	public List<? extends Resource> listResource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.inputStream;
	}

	@Override
	public String getName() {
		return name;
	}

}
