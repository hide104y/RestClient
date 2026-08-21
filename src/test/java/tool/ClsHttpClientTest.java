package tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ClsHttpClient} の単体テストクラスです。
 */
public class ClsHttpClientTest {

	private Path tempDir;
	private Logger logger;

	/**
	 * テスト実行前の初期化処理です。
	 *
	 * @throws IOException 入出力例外
	 */
	@Before
	public void setUp() throws IOException {
		logger = LogManager.getLogger(ClsHttpClientTest.class);
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "RestClient", "ClsHttpClientTest");
		if (!Files.exists(tempDir)) {
			Files.createDirectories(tempDir);
		}
	}

	/**
	 * テスト実行後の後処理です。
	 *
	 * @throws IOException 入出力例外
	 */
	@After
	public void tearDown() throws IOException {
		if (Files.exists(tempDir)) {
			Files.walk(tempDir)
					.map(Path::toFile)
					.sorted((o1, o2) -> -o1.compareTo(o2))
					.forEach(File::delete);
		}
	}

	/**
	 * HTTPステータスコードメジャー区分取得処理のテストです。
	 */
	@Test
	public void testGetMajorCode() {
		ClsProperties prop = new ClsProperties(logger);
		ClsHttpClient client = new ClsHttpClient(logger, prop);

		assertEquals(-1, client.getMajorCode(-1));
		assertEquals(2, client.getMajorCode(200));
		assertEquals(2, client.getMajorCode(204));
		assertEquals(3, client.getMajorCode(301));
		assertEquals(4, client.getMajorCode(404));
		assertEquals(5, client.getMajorCode(500));
	}

	/**
	 * レスポンス情報クリア処理のテストです。
	 */
	@Test
	public void testClearResponse() {
		ClsProperties prop = new ClsProperties(logger);
		ClsHttpClient client = new ClsHttpClient(logger, prop);

		client.clearResponse();
		assertEquals(-1, client.getHttpCode());
		assertEquals("", client.getResponseBody());
		assertEquals("", client.getResVersion());
		assertEquals("", client.getReasonPhrase());
		assertEquals(0, client.getResponseHeaders().size());
	}

	/**
	 * 初期化設定パターンのテストです。
	 */
	@Test
	public void testInitVariations() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.URL, "https://localhost:8443/api/test");
		prop.setValue(ClsProperties.IS_INSECURE, true);
		prop.setValue(ClsProperties.AUTH_USER_BASIC, "admin:secret");
		prop.setValue(ClsProperties.USER_AGENT, "TestAgent/1.0");

		ClsHttpClient client = new ClsHttpClient(logger, prop);
		boolean initOk = client.init();
		assertTrue(initOk);

		client.terminate();
	}

	/**
	 * DryRun 接続モードの動作検証テストです。
	 */
	@Test
	public void testDryRunConnect() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.URL, "https://example.com/api");
		prop.setValue(ClsProperties.IS_DRYRUN, true);
		prop.setValue(ClsProperties.DRYRUN_HTTP_CODE, 200);
		prop.setValue(ClsProperties.DRYRUN_HTTP_BODY, "{\"status\":\"ok\"}");
		prop.setValue(ClsProperties.DRYRUN_ELAPS_MSEC, 10);

		ClsHttpClient client = new ClsHttpClient(logger, prop);
		assertTrue(client.init());

		int code = client.connect();
		assertEquals(200, code);
		assertEquals("{\"status\":\"ok\"}", client.getResponseBody());
		assertEquals("HTTP/1.1", client.getResVersion());
		assertEquals("DRYRUN", client.getReasonPhrase());

		client.terminate();
	}

	/**
	 * リクエストヘッダー表示メソッドのテストです。
	 */
	@Test
	public void testShowReqHeaders() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.VERBOSE, 1);
		ClsHttpClient client = new ClsHttpClient(logger, prop);

		Header[] headers = new Header[]{
				new BasicHeader("Content-Type", "application/json")
		};
		assertTrue(client.showReqHeaders(headers, "GET", "/api"));
	}

	/**
	 * プロキシ設定を指定した初期化のテストです。
	 */
	@Test
	public void testInitWithProxy() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.URL, "http://example.com/api");
		prop.setValue(ClsProperties.PROXY, "http://proxy.example.com:8080");
		prop.setValue(ClsProperties.IS_TRACE_LOG, 1);

		ClsHttpClient client = new ClsHttpClient(logger, prop);
		boolean initOk = client.init();
		assertTrue(initOk);

		client.terminate();
	}

	/**
	 * プロキシ認証設定を指定した初期化のテストです。
	 */
	@Test
	public void testInitWithProxyAuth() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.URL, "http://example.com/api");
		prop.setValue(ClsProperties.PROXY, "http://testuser:testpass@proxy.example.com:8080");
		prop.setValue(ClsProperties.IS_TRACE_LOG, 1);

		ClsHttpClient client = new ClsHttpClient(logger, prop);
		boolean initOk = client.init();
		assertTrue(initOk);
		assertTrue(prop.getValue(ClsProperties.IS_PROXY_AUTH, false));

		client.terminate();
	}
}
