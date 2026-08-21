package tool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.logging.log4j.Logger;

/**
 * Apache HttpClient 5 を用いて HTTP/HTTPS リクエストの送受信を行う通信クライアントクラスです。
 */
public class ClsHttpClient {

	private volatile ClsProperties prop;
	private Logger logger;
	private BasicHttpClientConnectionManager connMgr;
	private CloseableHttpClient httpClient;
	private List<String> responseHeaders = new ArrayList<>();
	private Charset reqEncoding = Charset.forName(ClsProperties.DEFAULT_STR_ENCODING);
	private Charset resEncoding = Charset.forName(ClsProperties.DEFAULT_STR_ENCODING);
	private String responseBody = "";
	private String resVersion = "";
	private String reasonPhrase = "";
	private int httpCode = -1;
	private int verbose = -1;
	private int traceLog = -1;

	/**
	 * ロガーおよびプロパティ管理オブジェクトを指定してインスタンスを初期化するコンストラクタです。
	 *
	 * @param logger ログ出力用ロガー
	 * @param prop プロパティ管理オブジェクト
	 *
	 * <pre>
	 * ClsHttpClient client = new ClsHttpClient(logger, prop);
	 * </pre>
	 */
	public ClsHttpClient(Logger logger, ClsProperties prop) {
		this.logger = logger;
		this.prop = prop;
	}

	/**
	 * 受信したレスポンスヘッダー一覧を取得します（短縮形）。
	 *
	 * @return レスポンスヘッダー文字列のリスト
	 *
	 * <pre>
	 * List&lt;String&gt; headers = client.getResHeaders();
	 * </pre>
	 */
	public List<String> getResHeaders() {
		return responseHeaders;
	}

	/**
	 * 受信したレスポンスヘッダー一覧を取得します。
	 *
	 * @return レスポンスヘッダー文字列のリスト
	 *
	 * <pre>
	 * List&lt;String&gt; headers = client.getResponseHeaders();
	 * </pre>
	 */
	public List<String> getResponseHeaders() {
		return responseHeaders;
	}

	/**
	 * 受信したレスポンスボディ文字列を取得します。
	 *
	 * @return レスポンス本文文字列
	 *
	 * <pre>
	 * String body = client.getResponseBody();
	 * </pre>
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * 受信した HTTP レスポンスバージョンを取得します。
	 *
	 * @return HTTP バージョン文字列（例: HTTP/1.1）
	 *
	 * <pre>
	 * String version = client.getResVersion();
	 * </pre>
	 */
	public String getResVersion() {
		return resVersion;
	}

	/**
	 * 受信した HTTP レスポンスの Reason Phrase を取得します。
	 *
	 * @return Reason Phrase 文字列（例: OK）
	 *
	 * <pre>
	 * String reason = client.getReasonPhrase();
	 * </pre>
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * 受信した HTTP ステータスコードを取得します。
	 *
	 * @return HTTP ステータスコード（例: 200）
	 *
	 * <pre>
	 * int status = client.getHttpCode();
	 * </pre>
	 */
	public int getHttpCode() {
		return httpCode;
	}

	/**
	 * 前回の通信レスポンス情報をクリアします。
	 *
	 * <pre>
	 * client.clearResponse();
	 * </pre>
	 */
	public void clearResponse() {
		responseHeaders.clear();
		httpCode = -1;
		resVersion = "";
		reasonPhrase = "";
		responseBody = "";
	}

	/**
	 * プロパティ設定に基づき HttpClient および接続マネージャーを初期化します。
	 *
	 * @return 初期化に成功した場合は true、失敗した場合は false
	 *
	 * <pre>
	 * boolean ok = client.init();
	 * </pre>
	 */
	public Boolean init() {
		boolean isOk = true;
		DnsResolver dnsResolver;
		long initStartTime = System.currentTimeMillis();
		long startTime = initStartTime;
		long endTime;
		reqEncoding = Charset.forName(prop.getValue(ClsProperties.HTTP_REQ_ENCODING, ClsProperties.DEFAULT_STR_ENCODING));
		resEncoding = Charset.forName(prop.getValue(ClsProperties.HTTP_RES_ENCODING, ClsProperties.DEFAULT_STR_ENCODING));
		verbose = prop.getValue(ClsProperties.VERBOSE, 0);
		traceLog = prop.getValue(ClsProperties.IS_TRACE_LOG, 0);

		try {
			dnsResolver = new SystemDefaultDnsResolver() {
				@Override
				public InetAddress[] resolve(final String host) throws UnknownHostException {
					String customIp = prop.getValue(ClsProperties.IPADDR, "");
					if (!customIp.isEmpty()) {
						return new InetAddress[] { InetAddress.getByName(customIp) };
					} else {
						return super.resolve(host);
					}
				}
			};

			if (0 < traceLog && logger != null) {
				endTime = System.currentTimeMillis();
				double elapsed = (double) (endTime - startTime) / 1000.0;
				logger.debug("DNSResolver Setting Time : " + elapsed + " sec");
				startTime = System.currentTimeMillis();
			}

			ConnectionConfig connConfig = ConnectionConfig.custom()
				.setConnectTimeout(prop.getValue(ClsProperties.TIMEOUT_CONNECT, ClsProperties.DEFAULT_TIMEOUT_CONNECT), TimeUnit.MILLISECONDS)
				.setSocketTimeout(prop.getValue(ClsProperties.TIMEOUT_SOCKET, ClsProperties.DEFAULT_TIMEOUT_SOCKET), TimeUnit.MILLISECONDS)
				.build();

			RequestConfig reqConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(prop.getValue(ClsProperties.TIMEOUT_REQ, ClsProperties.DEFAULT_TIMEOUT_REQ), TimeUnit.MILLISECONDS)
				.setResponseTimeout(prop.getValue(ClsProperties.TIMEOUT_RES, ClsProperties.DEFAULT_TIMEOUT_RES), TimeUnit.MILLISECONDS)
				.build();

			if (0 < traceLog && logger != null) {
				endTime = System.currentTimeMillis();
				double elapsed = (double) (endTime - startTime) / 1000.0;
				logger.debug("Timeout Setting Time : " + elapsed + " sec");
				startTime = System.currentTimeMillis();
			}

			TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory sslConnSocketFactory;
			if (prop.getValue(ClsProperties.IS_INSECURE, false)) {
				if (0 < traceLog && logger != null) {
					logger.debug("SET : NoopHostnameVerifier");
				}
				sslConnSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			} else {
				if (0 < traceLog && logger != null) {
					logger.debug("SET : DefaultHostnameVerifier");
				}
				sslConnSocketFactory = new SSLConnectionSocketFactory(sslContext, prop.getValue(ClsProperties.PROTOCOL_VERSION, ClsProperties.DEFAULT_PROTOCOL_VERSION).split("[,|]"), null, new DefaultHostnameVerifier());
			}
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", new PlainConnectionSocketFactory())
				.register("https", sslConnSocketFactory)
				.build();

			if (0 < traceLog && logger != null) {
				endTime = System.currentTimeMillis();
				double elapsed = (double) (endTime - startTime) / 1000.0;
				logger.debug("SocketFactoryRegistry Building Time : " + elapsed + " sec");
				startTime = System.currentTimeMillis();
			}

			connMgr = new BasicHttpClientConnectionManager(
					socketFactoryRegistry,
					ManagedHttpClientConnectionFactory.builder().build(),
					null,
					dnsResolver
				);
			connMgr.setConnectionConfig(connConfig);

			if (0 < traceLog && logger != null) {
				endTime = System.currentTimeMillis();
				double elapsed = (double) (endTime - startTime) / 1000.0;
				logger.debug("ConnectionManager Building Time : " + elapsed + " sec");
				startTime = System.currentTimeMillis();
			}

			HttpClientBuilder clientBuilder = HttpClients.custom();
			clientBuilder = clientBuilder.setConnectionManager(connMgr);
			clientBuilder = clientBuilder.setDefaultRequestConfig(reqConfig);

			if (!prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT).isEmpty()) {
				clientBuilder = clientBuilder.setUserAgent(prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT));
			}

			java.net.URL proxyUrl = null;
			String userInfo = null;
			if (!prop.getValue(ClsProperties.PROXY, "").isEmpty()) {
				try {
					proxyUrl = new java.net.URL(prop.getValue(ClsProperties.PROXY, ""));
				} catch (Exception e) {
					if (logger != null) {
						logger.error("Invalid Argument : -proxy " + prop.getValue(ClsProperties.PROXY, ""));
					}
					throw e;
				}
				userInfo = proxyUrl.getUserInfo();
				if (userInfo != null && !userInfo.isEmpty()) {
					prop.setValue(ClsProperties.IS_PROXY_AUTH, true);
				}
				clientBuilder.setProxy(new HttpHost(proxyUrl.getProtocol(), proxyUrl.getHost(), proxyUrl.getPort()));
				if (0 < traceLog && logger != null) {
					logger.debug("SET : PROXY = " + proxyUrl.getProtocol() + "://" + proxyUrl.getHost() + ":" + proxyUrl.getPort());
				}
			}

			if (prop.getValue(ClsProperties.AUTH_USER_BASIC, "").isEmpty()) {
				prop.setValue(ClsProperties.IS_BASIC_AUTH, false);
			} else {
				prop.setValue(ClsProperties.IS_BASIC_AUTH, true);
			}

			if (prop.getValue(ClsProperties.IS_BASIC_AUTH, false) || prop.getValue(ClsProperties.IS_PROXY_AUTH, false)) {
				BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
				if (prop.getValue(ClsProperties.IS_BASIC_AUTH, false)) {
					String[] userPass = prop.getValue(ClsProperties.AUTH_USER_BASIC, "").split(":");
					java.net.URL url = null;
					try {
						url = new java.net.URL(prop.getValue(ClsProperties.URL, ""));
					} catch (Exception ignored) {
						// ignore
					}
					if (url != null && userPass.length >= 2) {
						credsProvider.setCredentials(
								new AuthScope(url.getHost(), url.getPort()),
								new UsernamePasswordCredentials(userPass[0].trim(), userPass[1].trim().toCharArray())
							);
						if (0 < traceLog && logger != null) {
							logger.debug("SET : BASIC AUTH : HOST = " + url.getHost() + " / PORT = " + url.getPort() + " / USER = " + userPass[0].trim() + " / PASS = " + userPass[1].trim());
						}
					}
				}
				if (prop.getValue(ClsProperties.IS_PROXY_AUTH, false) && proxyUrl != null && userInfo != null) {
					String[] userInfoParts = userInfo.split(":");
					String user = userInfoParts[0].trim();
					String pass = userInfoParts.length > 1 ? userInfoParts[1].trim() : "";
					credsProvider.setCredentials(
							new AuthScope(proxyUrl.getHost(), proxyUrl.getPort()),
							new UsernamePasswordCredentials(user, pass.toCharArray())
						);
					if (0 < traceLog && logger != null) {
						logger.debug("SET : PROXY AUTH : HOST = " + proxyUrl.getHost() + " / PORT = " + proxyUrl.getPort() + " / USER = " + user + " / PASS = " + pass);
					}
				}
				clientBuilder = clientBuilder.setDefaultCredentialsProvider(credsProvider);
			}
			httpClient = clientBuilder.build();

			if (0 < traceLog && logger != null) {
				endTime = System.currentTimeMillis();
				double elapsed = (double) (endTime - startTime) / 1000.0;
				logger.debug("httpclient Building Time : " + elapsed + " sec");
			}
		} catch (Exception ex) {
			isOk = false;
			if (logger != null) {
				logger.error("EXCEPTION : " + ex.getMessage(), ex);
			}
		}

		if ((0 < traceLog || verbose > 2) && logger != null) {
			endTime = System.currentTimeMillis();
			double elapsed = (double) (endTime - initStartTime) / 1000.0;
			logger.debug("Total init() Elapsed Time : " + elapsed + " sec");
		}
		return isOk;
	}

	/**
	 * 設定されたリクエスト情報に基づいて HTTP リクエストを送信し、レスポンスを取得します。
	 *
	 * @return HTTP ステータスコード
	 *
	 * <pre>
	 * int status = client.connect();
	 * </pre>
	 */
	public int connect() {
		long startTime = System.currentTimeMillis();
		String url = prop.getValue(ClsProperties.URL, "");
		String reqBody = prop.getValue(ClsProperties.REQ_BODY, "");
		clearResponse();

		try {
			if (reqBody != null && !reqBody.isEmpty() && prop.getValue(ClsProperties.IS_ENCODE_UNICODE_ESCAPES, false)) {
				reqBody = prop.encodeUnicodeEscapes(reqBody);
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.error("EXCEPTION : " + e.getMessage());
			}
		}

		if (0 < traceLog && logger != null) {
			logger.debug("METHOD       = " + prop.getValue(ClsProperties.METHOD, "GET"));
			logger.debug("URL          = " + url);
			if (!reqBody.isEmpty()) {
				logger.debug("REQUEST BODY = " + reqBody);
			}
		}

		try {
			if (prop.getValue(ClsProperties.IS_DRYRUN, false)) {
				resVersion = "HTTP/1.1";
				reasonPhrase = "DRYRUN";
				httpCode = prop.getValue(ClsProperties.DRYRUN_HTTP_CODE, ClsProperties.DEFAULT_DRYRUN_HTTP_CODE);
				responseBody = prop.getValue(ClsProperties.DRYRUN_HTTP_BODY, ClsProperties.DEFAULT_DRYRUN_HTTP_BODY);
				prop.doSleep(prop.getValue(ClsProperties.DRYRUN_ELAPS_MSEC, ClsProperties.DEFAULT_DRYRUN_ELAPS_MSEC));
			} else {
				String httpMethod = prop.getValue(ClsProperties.METHOD, "GET");
				ClassicHttpRequest request = createHttpRequest(httpMethod, url);

				prop.getHMapRequestHeaders().forEach(request::setHeader);

				if (!reqBody.isEmpty() && request instanceof HttpEntityContainer) {
					((HttpEntityContainer) request).setEntity(new StringEntity(reqBody, reqEncoding));
				}

				showReqHeaders(request.getHeaders(), request.getMethod(), request.getPath());
				httpClient.execute(request, response -> {
					resVersion = response.getVersion() != null ? response.getVersion().toString() : "";
					reasonPhrase = response.getReasonPhrase();
					httpCode = response.getCode();
					Arrays.stream(response.getHeaders()).map(Header::toString).forEach(responseHeaders::add);

					HttpEntity entity = response.getEntity();
					if (entity != null) {
						responseBody = EntityUtils.toString(entity, resEncoding);
						EntityUtils.consume(entity);
					} else {
						responseBody = "";
					}
					return httpCode;
				});
			}
		} catch (Exception ex) {
			if (logger != null) {
				logger.error("EXCEPTION : " + ex.getMessage(), ex);
			}
		}

		if ((0 < traceLog || verbose > 2) && logger != null) {
			long endTime = System.currentTimeMillis();
			double elapsed = (double) (endTime - startTime) / 1000.0;
			logger.debug("Connection Time : " + elapsed + " sec");
		}
		return httpCode;
	}

	/**
	 * HttpClient およびコネクションマネージャーのリソースをクローズして解放します。
	 *
	 * <pre>
	 * client.terminate();
	 * </pre>
	 */
	public void terminate() {
		long startTime = System.currentTimeMillis();
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (Exception ignored) {
				// ignore
			}
			httpClient = null;
		}
		if (connMgr != null) {
			try {
				connMgr.close();
			} catch (Exception ignored) {
				// ignore
			}
			connMgr = null;
		}

		if ((0 < traceLog || verbose > 2) && logger != null) {
			long endTime = System.currentTimeMillis();
			double elapsed = (double) (endTime - startTime) / 1000.0;
			logger.debug("Total terminate() Elapsed Time : " + elapsed + " sec");
		}
	}

	/**
	 * リクエストヘッダー情報を標準出力に出力します。
	 *
	 * @param headers ヘッダー配列
	 * @param method HTTPメソッド名
	 * @param path リクエストパス
	 * @return 常に true
	 *
	 * <pre>
	 * client.showReqHeaders(request.getHeaders(), "GET", "/api/test");
	 * </pre>
	 */
	public Boolean showReqHeaders(Header[] headers, String method, String path) {
		if (verbose > 0 && headers != null) {
			System.out.println("< " + method + " " + path);
			for (Header header : headers) {
				System.out.println("< " + header.toString());
			}
			System.out.println("<");
		}
		return true;
	}

	/**
	 * HTTPステータスコードの百の位（1〜5）を取得します。
	 *
	 * @param fullCode HTTPステータスコード（例: 200, 404, 500）
	 * @return ステータスコードのメジャー区分（例: 2, 4, 5。0未満の場合は -1）
	 *
	 * <pre>
	 * int major = client.getMajorCode(200); // 2
	 * </pre>
	 */
	public int getMajorCode(int fullCode) {
		if (fullCode < 0) {
			return -1;
		}
		return fullCode / 100;
	}

	/**
	 * 指定された HTTP メソッド名と URL から適切なリクエストオブジェクトを生成します。
	 *
	 * @param method HTTPメソッド名
	 * @param url 送信先URL
	 * @return ClassicHttpRequest インスタンス
	 *
	 * <pre>
	 * ClassicHttpRequest req = client.createHttpRequest("POST", "https://example.com");
	 * </pre>
	 */
	private ClassicHttpRequest createHttpRequest(String method, String url) {
		switch (method.toUpperCase()) {
			case "POST":
				return new HttpPost(url);
			case "PUT":
				return new HttpPut(url);
			case "PATCH":
				return new HttpPatch(url);
			case "DELETE":
				return new HttpDelete(url);
			case "HEAD":
				return new HttpHead(url);
			case "TRACE":
				return new HttpTrace(url);
			case "OPTIONS":
				return new HttpOptions(url);
			case "GET":
			default:
				return new HttpGet(url);
		}
	}
}
