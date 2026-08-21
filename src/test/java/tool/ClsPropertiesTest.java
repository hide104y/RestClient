package tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ClsProperties} の単体テストクラスです。
 */
public class ClsPropertiesTest {

	private Path tempDir;
	private Logger logger;

	/**
	 * テスト実行前の初期化処理です。
	 *
	 * @throws IOException 入出力例外
	 */
	@Before
	public void setUp() throws IOException {
		logger = LogManager.getLogger(ClsPropertiesTest.class);
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "RestClient", "ClsPropertiesTest");
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
	 * GetterおよびSetterメソッドの動作検証テストです。
	 */
	@Test
	public void testGettersAndSetters() {
		ClsProperties prop = new ClsProperties(logger);

		Map<String, String> map = new LinkedHashMap<>();
		map.put("key", "val");
		prop.setPropMap(map);
		assertEquals(1, prop.getPropMap().size());

		prop.setUserAgentMap(map);
		assertEquals(1, prop.getUserAgentMap().size());

		prop.setReqHeadersMap(map);
		assertEquals(1, prop.getReqHeadersMap().size());

		prop.setReplaceUrlMap(map);
		assertEquals(1, prop.getReplaceUrlMap().size());

		prop.setReplaceReqBodyMap(map);
		assertEquals(1, prop.getReplaceReqBodyMap().size());

		prop.setReplaceAllReqBodyMap(map);
		assertEquals(1, prop.getReplaceAllReqBodyMap().size());

		prop.setReplaceResBodyMap(map);
		assertEquals(1, prop.getReplaceResBodyMap().size());

		prop.setReplaceAllResBodyMap(map);
		assertEquals(1, prop.getReplaceAllResBodyMap().size());

		assertNotNull(prop.getJsonBodyBuilder());
	}

	/**
	 * User-Agent 初期化機能のテストです。
	 */
	@Test
	public void testUserAgentInit() {
		ClsProperties prop = new ClsProperties(logger);
		prop.initUserAgentMap();
		assertEquals(3, prop.getUserAgentMap().size());
		assertTrue(prop.getUserAgentMap().containsKey("CHROME"));
		assertTrue(prop.getUserAgentMap().containsKey("FIREFOX"));
		assertTrue(prop.getUserAgentMap().containsKey("EDGE"));
	}

	/**
	 * UNIX時間フォーマット処理のテストです。
	 */
	@Test
	public void testFormatUnixTime() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue(ClsProperties.TIMEZONE_DIFF_HOUR, "GMT+9");

		// 0 epoch (1970/01/01 09:00:00 JST)
		String formatted = prop.formatUnixTime(0, "yyyy/MM/dd HH:mm:ss");
		assertEquals("1970/01/01 09:00:00", formatted);

		String formattedMillis = prop.formatUnixTime(0L, "yyyy/MM/dd HH:mm:ss");
		assertEquals("1970/01/01 09:00:00", formattedMillis);
	}

	/**
	 * ファイル読み書きおよび文字コード判定のテストです。
	 *
	 * @throws IOException 入出力例外
	 */
	@Test
	public void testFileReadWriteAndDetectCharset() throws IOException {
		ClsProperties prop = new ClsProperties(logger);
		File testFile = tempDir.resolve("test_utf8.txt").toFile();
		String sampleText = "{\"message\":\"こんにちは世界\"}";

		boolean writeOk = prop.writeFile(testFile.getAbsolutePath(), sampleText, StandardCharsets.UTF_8);
		assertTrue(writeOk);

		String detected = prop.detectCharset(testFile.getAbsolutePath());
		assertNotNull(detected);

		String readContent = prop.readFile(testFile.getAbsolutePath(), "UTF-8");
		assertEquals(sampleText, readContent);
	}

	/**
	 * JSON文字列パースおよび Pretty 整形・ノード抽出のテストです。
	 */
	@Test
	public void testParseJsonStr() {
		ClsProperties prop = new ClsProperties(logger);
		String rawJson = "{\"name\":\"John\",\"age\":30}";

		String parsed = prop.parseJsonStr(rawJson, false, false, false, true);
		assertNotNull(parsed);
		assertTrue(parsed.contains("\"name\":\"John\""));

		String pretty = prop.parseJsonStr(rawJson, true, false, false, true);
		assertNotNull(pretty);
		assertTrue(pretty.contains("\n"));

		// ノード抽出テスト
		prop.setValue(ClsProperties.EXTRACTION_KEY_CSV, "name");
		String extracted = prop.parseJsonStr(rawJson, false, false, false, true);
		assertEquals("\"John\"", extracted);
		prop.setValue(ClsProperties.EXTRACTION_KEY_CSV, "");

		// 不正なJSON
		String invalid = prop.parseJsonStr("invalid json", false, false, false, false);
		assertNull(invalid);
		assertTrue(prop.getValue(ClsProperties.IS_PARSE_ERROR, false));
	}

	/**
	 * トリム処理およびスリープ処理のテストです。
	 */
	@Test
	public void testDoTrimAndDoSleep() {
		ClsProperties prop = new ClsProperties(logger);
		assertEquals("abc", prop.doTrim("  abc  "));
		assertNull(prop.doTrim("   "));
		assertNull(prop.doTrim(null));

		long start = System.currentTimeMillis();
		prop.doSleep(50);
		long elapsed = System.currentTimeMillis() - start;
		assertTrue(30 <= elapsed);
	}

	/**
	 * 各種置換ルールパースおよび置換処理のテストです。
	 */
	@Test
	public void testSplitAndReplaces() {
		ClsProperties prop = new ClsProperties(logger);

		// splitMergeProp
		assertTrue(prop.splitMergeProp("k1=v1,k2=v2", "[,|]"));
		assertEquals("v1", prop.getValue("k1", ""));
		assertEquals("v2", prop.getValue("k2", ""));

		// splitReqHeaders
		assertTrue(prop.splitReqHeaders("Content-Type: application/json, X-Api-Key: secret123"));
		assertEquals("application/json", prop.getReqHeadersMap().get("Content-Type"));
		assertEquals("secret123", prop.getReqHeadersMap().get("X-Api-Key"));

		// splitMapUrl & replaceUrl
		assertTrue(prop.splitMapUrl("localhost=127.0.0.1,api=v1/api"));
		String replacedUrl = prop.replaceUrl("http://localhost/api/test");
		assertEquals("http://127.0.0.1/v1/api/test", replacedUrl);

		// splitMapReplace & replaceBody
		assertTrue(prop.splitMapReplace("foo=bar,TEST=PROD", true));
		String replacedBody = prop.replaceBody("foo is TEST", true);
		assertEquals("bar is PROD", replacedBody);

		// splitReplaceAll & replaceAllBody
		assertTrue(prop.splitReplaceAll("__RTAB__=__EMPTY__,__DQ__='", false));
		String replacedAll = prop.replaceAllBody("\t\"sample\"", false);
		assertEquals("'sample'", replacedAll);
	}

	/**
	 * Unicodeエスケープ変換およびデコードのテストです。
	 */
	@Test
	public void testUnicodeConversion() {
		ClsProperties prop = new ClsProperties(logger);

		String orig = "Hello 世界";
		String escaped = prop.encodeUnicodeEscapes(orig);
		assertNotNull(escaped);
		assertTrue(escaped.contains("\\u"));

		String decoded = prop.convertUnicode(escaped);
		assertEquals(orig, decoded);
		assertEquals(orig, prop.convertUnicodeToUtf8(escaped));
	}

	/**
	 * パラメータ一覧出力処理のテストです。
	 */
	@Test
	public void testListOutputs() {
		ClsProperties prop = new ClsProperties(logger);
		prop.setValue("param1", "val1");
		prop.setValue("param2", "val2");

		String listStr = prop.list();
		assertTrue(listStr.contains("param1 = val1"));
		assertTrue(listStr.contains("param2 = val2"));
	}
}
