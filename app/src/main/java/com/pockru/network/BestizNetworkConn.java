package com.pockru.network;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 이 클래스는 자동로그인을 위한 쿠키를 생성하고, 생성된 로그인 쿠키로 리퀘스트를 요청할 수 있다.
 * 
 * <p>
 * 예제) 
 * 	<blockquote><pre>
 *     AbstractLoginWebCrawl web = new Gmarket(id, password);
 *     web.requestLogin();
 *     boolean result = web.successLogin();
 *	   if (result) {
 *			web.requestGet(...); or web.requestPost(...);
 *     } else {
 *     		throw new AutoLoginCheckedException(CommonErrCode.ERR_0001, "xxxx");
 *     }
 *   </pre></blockquote>
 * </p>
 * 
 * <p>
 * <strong>* 기본 Request Property</strong> 
 *	 <ul>
 *		<li>setRequestProperty("Host", "XXX")</li>
 * 		<li>setRequestProperty("User-Agent", "Mozilla/5.0")</li>
 * 		<li>setRequestProperty("Accept", "*#47;*")</li>
 * 		<li>setRequestProperty("Accept-Language", "ko-kr,ko;q=0.8,en-us;q=0.5,en;q=0.3")</li>
 * 		<li>setRequestProperty("Connection", "keep-alive")</li>
 * 		<li>setRequestProperty("Content-Type","application/x-www-form-urlencoded")</li>
 * 		<li>setRequestProperty("Accept-Encoding", "gzip")</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>* Request Property 맵 생성 예제</strong> 
 * <blockquote><pre>
 *     Map<String, String> requestProperty = new HashMap<String, String>();
 *     requestProperty.put("Host", obj.getHost());
 *     requestProperty.put("User-Agent", USER_AGENT);
 *   </pre></blockquote>
 * </p>
 * 
 * @author junyeon kim
 * @version 1.0.0
 *
 */

public class BestizNetworkConn {
	
	/** The conn. */
	private HttpURLConnection conn = null;
	
	/** The login cookies. */
	private String loginCookies = "";
	
	private String loginRes = "";
	
	/** response code */
	int resCode;
	
	/** The site name. */
//	private String siteName;
	
	/**  USER AGENT. */
	protected String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/KRT16M) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
	
	/**  로그인 요청 후 응답 헤더 필드. */
	private Map<String,List<String>> headerFields;

	/**  로그인 폼 및 로그인 인증 URL 프로퍼티 로드. */
	private static Properties prop;
	
	
    /**
     * 생성자.
     *
     */
    private BestizNetworkConn(Context context){
//    	this.siteName = name;
    	try {
			InputStream is = context.getApplicationContext().getAssets().open("properties.xml");
			prop = new Properties();
			prop.loadFromXML(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private static BestizNetworkConn mInstance;

	public static BestizNetworkConn getInstance(Context context){
		if (mInstance == null) {
			mInstance = new BestizNetworkConn(context);
		}

		return mInstance;
	}

	/**
	 * 로그인 쿠키 반환.
	 *
	 * @return 로그인 쿠키
	 */
	public String getLoginCookies() {
		
		return loginCookies;
	}
	
	/**
	 * URL설정 파일로부터 키로서 해당 URL 반환.
	 *
	 * @param key the key
	 * @return  URL
	 */
	protected String getProperty(String key){
		String value = prop.getProperty(key);
		
		return value;
	}
	
	/**
	 * URL주소를 파라미터로 HttpURLConnection/HttpsURLConnection 커넥션을 생성하여 반환
	 * 
	 * HttpsURLConnection의 경우 알수없는 인증서로 인한 오류 방지로직 추가.
	 *
	 * @param url the url
	 * @return HttpURLConnection
	 * @throws Exception the exception
	 */
	private HttpURLConnection getConn(String url) throws Exception {
		URL obj = new URL(url);
        URLConnection uc = obj.openConnection();
        
		uc.setDoOutput(true);
		uc.setDoInput(true);
		uc.setUseCaches(true);
		
//		// 타임아웃 설정
//		int connTimeout = Integer.parseInt(getProperty("connection.timeout"));
//		int readTimeout = Integer.parseInt(getProperty("read.timeout"));
//		uc.setConnectTimeout(connTimeout);
//		uc.setReadTimeout(readTimeout);
		
		if (url.substring(0, 5).toLowerCase().equals("https")) {
			 /*s https의 경우 알수없는 인증서로 인한 오류 방지 */
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			      
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
  
                public void checkClientTrusted(java.security.cert
                        .X509Certificate[] certs, String authType) {
                }
  
                public void checkServerTrusted(java.security.cert
                        .X509Certificate[] certs, String authType) {
                }
            } };
  
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
  
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /*e https의 경우 알수없는 인증서로 인한 오류 방지 */
            
			return (HttpsURLConnection)uc;
		} else {
			return (HttpURLConnection)uc;
		}
	}
	
	/**
	 * GET방식으로 요청 URL만 설정하여 해당 URL로부터 응답결과 취득
	 * 
	 * <p>기본적인 Request Property 항목만 설정하여 요청</p>
	 * 
	 * <p>로그인 성공 후 요청시에는 응답쿠키도 같이 전송하기 때문에
	 * 로그인시에만 접근 가능한 페이지도 응답결과 취득 가능</p>
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없을 경우에는
	 *    응답결과가 깨질 가능성이 있으므로
	 *    {@link #requestGet(RequestInfo)} 호출하여 사용할 것
	 * </p>.
	 *
	 * @param url the url
	 * @return 해당 URL 응답결과(HTML)
	 * @throws Exception the exception
	 */
	public String requestGet(String url) throws Exception {
		Map<String, String> requestProperty = getRequestProperty(url);

		return requestGet(url, requestProperty);
	}
	
	/**
	 * GET방식으로 요청 URL과 Request Property맵을 설정하여 해당 URL로부터 응답결과 취득
	 * 
	 * <p>로그인 성공 후 요청시에는 응답쿠키도 같이 전송하기 때문에
	 * 로그인시에만 접근 가능한 페이지도 응답결과 취득 가능</p>
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없을 경우에는
	 *    응답결과가 깨질 가능성이 있으므로
	 *    {@link #requestGet(RequestInfo)} 호출하여 사용할 것
	 * </p>.
	 *
	 * @param url the url
	 * @param requestProperty the request property
	 * @return String 해당 URL 응답결과(HTML)
	 * @throws Exception Exception
	 */
	public String requestGet(String url, Map<String, String> requestProperty) throws Exception {
		try{
		conn = getConn(url);
	 
		conn.setRequestMethod("GET");
	
		// Request Property 설정
        for (String key : requestProperty.keySet()) {
        	conn.setRequestProperty(key, requestProperty.get(key));
        }
        
        String response = getResponse(conn);
        
		return response;
		} finally {
			conn.disconnect();
		}
	}
	
	/**
	 * GET방식으로 RequestInfo를 파라미터로 하여 요청 URL로부터 응답결과 취득
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없어 응답결과가 깨질 경우 사용</p>.
	 *
	 * @param requestInfo the request info
	 * @return String 		해당 URL 응답결과(HTML)
	 * @throws Exception Exception
	 */
	public String requestGet(RequestInfo requestInfo) throws Exception {
		try {
			String url;
			String params = requestInfo.getParams();
			if (TextUtils.isEmpty(params) == false) {
				url = requestInfo.getUrl() + "?" + params;
			} else {
				url = requestInfo.getUrl();
			}
			Map<String, String> requestProperty = requestInfo.getRequestProperty();
			if (requestProperty == null) {
				requestProperty = getRequestProperty(url);
			}
			conn = getConn(url);
			conn.setRequestMethod("GET");
			// Request Property 설정
			for (String key : requestProperty.keySet()) {
				conn.setRequestProperty(key, requestProperty.get(key));
			}
			// 응답결과 취득
			String encoding = requestInfo.getEncoding();
			String response = "";
			if (TextUtils.isEmpty(encoding) == false) {
				response = getResponse(conn, encoding);
			} else {
				response = getResponse(conn);
			}
			return response;
		} finally {
			conn.disconnect();
		}
	}
	
	/**
	 * POST방식으로 요청 URL만 설정하여 해당 URL로부터 응답결과 취득
	 * 
	 * <p>기본적인 Request Property 항목만 설정하여 요청</p>
	 * 
	 * <p>로그인 성공 후 요청시에는 응답쿠키도 같이 전송하기 때문에
	 * 로그인시에만 접근 가능한 페이지도 응답결과 취득 가능</p>
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없을 경우에는
	 *    응답결과가 깨질 가능성이 있으므로
	 *    {@link #requestGet(RequestInfo)} 호출하여 사용할 것
	 * </p>.
	 *
	 * @param url the url
	 * @param params the params
	 * @return 해당 URL 응답결과(HTML)
	 * @throws Exception the exception
	 */
	public String requestPost(String url, String params) throws Exception{
		Map<String, String> requestProperty = getRequestProperty(url);
		
		return requestPost(url, params, requestProperty);
	}
	
	/**
	 * POST방식으로 요청 URL과 Request Property맵을 설정하여 해당 URL로부터 응답결과 취득
	 * 
	 * <p>로그인 성공 후 요청시에는 응답쿠키도 같이 전송하기 때문에
	 * 로그인시에만 접근 가능한 페이지도 응답결과 취득 가능</p>
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없을 경우에는
	 *    응답결과가 깨질 가능성이 있으므로
	 *    {@link #requestGet(RequestInfo)} 호출하여 사용할 것
	 * </p>.
	 *
	 * @param url the url
	 * @param params the params
	 * @param requestProperty the request property
	 * @return String 해당 URL 응답결과(HTML)
	 * @throws Exception Exception
	 */
	public String requestPost(String url, String params, Map<String, String> requestProperty) throws Exception{
		try {
			conn = getConn(url);
			
			// Request Property 설정
			conn.setRequestMethod("POST");
			
			// Request Property 설정
			for (String key : requestProperty.keySet()) {
				conn.setRequestProperty(key, requestProperty.get(key));
			}
 
			if (TextUtils.isEmpty(params) == false) {
				OutputStream out = conn.getOutputStream();
				DataOutputStream wr = new DataOutputStream(out);
				wr.writeBytes(params);
				wr.flush();
				wr.close();				
			}
			
			String response = getResponse(conn);
			
			// 로그인 요청 응답 쿠키만 저장한다.
//			String authLoginUrl = getProperty(siteName + ".loginAuth.url");
//			if (authLoginUrl.startsWith(url)) {
//				setCookies(getCookies(conn));
//			}
			
			return response;
		} finally {
			conn.disconnect();
		}
	}

	/**
	 * POST방식으로 RequestInfo를 파라미터로 하여 요청 URL로부터 응답결과 취득
	 * 
	 * <p>응답헤더의 ContentType에 charset가 없어 응답결과가 깨질 경우 사용</p>.
	 *
	 * @param requestInfo the request info
	 * @return String 		해당 URL 응답결과(HTML)
	 * @throws Exception Exception
	 */
	public String requestPost(RequestInfo requestInfo) throws Exception{
		try {
//			Map<String, String> requestProperty = requestInfo.getRequestProperty();
			
			String url = requestInfo.getUrl();
//			String params = requestInfo.getParams();
			
//			if (requestProperty == null) {
//				requestProperty = getRequestProperty(url);
//			} 
			Map<String, String> requestProperty = getRequestProperty(url);
			
			if (requestInfo.getRequestProperty() != null) {
				requestProperty.putAll(requestInfo.getRequestProperty());
			}
			
			conn = getConn(url);
			
			// Request Property 설정
//			conn.setRequestMethod("POST");
			if (requestInfo.getEntity() != null) {
				conn.setRequestMethod("POST");
			} else {
				conn.setRequestMethod("GET");
			}
			
			// Request Property 설정
			for (String key : requestProperty.keySet()) {
				conn.setRequestProperty(key, requestProperty.get(key));
			}
			
			// 파라미터 설정
			if (requestInfo.getEntity() != null) {
				OutputStream out = conn.getOutputStream();
				requestInfo.getEntity().writeTo(out);		
				out.close();
			}
//			DataOutputStream wr = new DataOutputStream(out);
//			wr.writeBytes(params);
//			wr.flush();
//			wr.close();
			
			// 응답결과 취득
			String encoding = requestInfo.getEncoding();
			String response = "";
			if (TextUtils.isEmpty(encoding) == false) {
				response = getResponse(conn, encoding);
			} else {
				response = getResponse(conn);
			}
			
			// 로그인 요청 응답 쿠키만 저장한다.
//			String authLoginUrl = getProperty(siteName + ".loginAuth.url");
//			if (authLoginUrl.startsWith(url)) {
//				setCookies(getCookies(conn));
//			}
			
			return response.toString();
		} finally{
			conn.disconnect();
		}
	}
	
	
	/**
	 * 기본적인 request property를 설정
	 */
	private Map<String, String> getRequestProperty(String url) throws Exception {
		
		URL obj = new URL(url);
		Map<String, String> requestProperty = new HashMap<String, String>();
		requestProperty.put("Host", obj.getHost());
		requestProperty.put("User-Agent", USER_AGENT);
		requestProperty.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		requestProperty.put("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
		requestProperty.put("Connection", "keep-alive");
//		requestProperty.put("Accept-Encoding", "gzip, deflate, sdch");
		requestProperty.put("Content-Type", "application/x-www-form-urlencoded");
//		requestProperty.put("Cookie", CookieManager.getInstance().getCookie(url));
//		if (TextUtils.isEmpty(loginCookies) == false) {
//			requestProperty.put("Cookie", loginCookies);
//		}
		
		return requestProperty;
	}
	
	/**
	 * HttpURLConnection으로부터 응답문자열을 취득
	 */
	private String getResponse(HttpURLConnection conn) throws Exception {
		
		return getResponse(conn, getEncoding(conn));
	}
	
	/**
	 * HttpURLConnection으로부터 응답문자열을 취득
	 */
	private String getResponse(HttpURLConnection conn, String encoding) throws Exception {
		resCode = conn.getResponseCode();
//		if (HttpURLConnection.HTTP_OK != responseCode && HttpURLConnection.HTTP_MOVED_TEMP != responseCode) {
//			throw new Exception(CommonErrCode.ERR_0001, String.valueOf(responseCode));
//		}
		
		InputStream input = decompressStream(conn);
		BufferedReader in = new BufferedReader(new InputStreamReader(input, encoding));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
//			response.append("\n");
		}
		in.close();
		
		return response.toString();
	}
	
	/**
	 * 헤더로부터 웹페이지 인코딩을 취득.
	 *
	 * @param conn the conn
	 * @return UTF-8/EUC-KR
	 */
	private String getEncoding(HttpURLConnection conn){
		String headerType = conn.getContentType();
		if (headerType != null && headerType.toUpperCase().indexOf("EUC-KR") > -1){
			return "EUC-KR";
		}
		return "EUC-KR";
	}
	
	/**
	 * gzip 사용해서 압축했을 경우 압축을 해제한다.
	 *
	 * @param conn the conn
	 * @return 압축해제된 InputStream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private InputStream decompressStream(HttpURLConnection conn) throws IOException {
		
		InputStream input = conn.getErrorStream();
	    if (input == null) {
	    	input = conn.getInputStream();
	    	if ("gzip".equals(conn.getContentEncoding())) {
				input = new GZIPInputStream(input);
			}
	    }
		
		return input;
	}

	/**
	 * 로그인 요청 응답결과로부터 쿠키를 취득
	 * 
	 * 이 쿠키를 이용하여 로그인이 필요한 페이지의 접근이 가능해짐
	 *
	 * @param conn 로그인 요청 응답 커넥션
	 * @return cookie String
	 */
	protected String getCookies(HttpURLConnection conn) throws Exception {
		Map<String,List<String>> m = conn.getHeaderFields();
		headerFields = conn.getHeaderFields();
      
		if(m.containsKey("Set-Cookie")) {
		    boolean isFirst = true;
		    StringBuilder sb = new StringBuilder();
		      
		    for(String cookie : m.get("Set-Cookie")) {
		        if(isFirst)
		            isFirst = false;
		        else
		            sb.append(";");
		        sb.append(cookie);
		    }
		    return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 *  HeaderFields 반환
	 *
	 * @return Header Fields
	 */
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}
	
	/**
	 * 로그인 요청 응답결과로부터 HeaderFields 취득하여 저장
	 *
	 * @param headerFields_ HeaderFields
	 */
	protected void setHeaderFields(Map<String, List<String>> headerFields_ ) {
		this.headerFields = headerFields_;
	}

	public String getLoginRes() {
		return loginRes;
	}

	public int getResCode(){
		return resCode;
	}

	public void cancel(){
		if (conn != null) conn.disconnect();
	}
}
