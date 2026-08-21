package tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ClsCmnArg} の単体テストクラスです。
 */
public class ClsCmnArgTest {

	private Path tempDir;
	private Logger logger;

	/**
	 * テスト実行前の初期化処理です。
	 *
	 * @throws IOException 入出力例外
	 */
	@Before
	public void setUp() throws IOException {
		logger = LogManager.getLogger(ClsCmnArgTest.class);
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "RestClient", "ClsCmnArgTest");
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
		ClsCmnArg arg = new ClsCmnArg();
		arg.setLogger(logger);
		assertEquals(logger, arg.getLogger());

		Map<String, String> named = new LinkedHashMap<>();
		named.put("key1", "val1");
		arg.setNamedArgs(named);
		assertEquals(1, arg.getNamedArgs().size());
		assertEquals("val1", arg.getNamedArgs().get("key1"));

		Map<String, String> valKeys = new LinkedHashMap<>();
		valKeys.put("u", "url");
		arg.setPropValKeys(valKeys);
		assertEquals("url", arg.getPropValKeys().get("u"));

		Map<String, String> blnKeys = new LinkedHashMap<>();
		blnKeys.put("s", "silent");
		arg.setPropBlnKeys(blnKeys);
		assertEquals("silent", arg.getPropBlnKeys().get("s"));

		Map<String, List<String>> multi = new LinkedHashMap<>();
		List<String> list = new ArrayList<>();
		list.add("item1");
		multi.put("h", list);
		arg.setMultiValArgs(multi);
		assertEquals(1, arg.getMultiValArgs().get("h").size());

		List<String> dup = new ArrayList<>();
		dup.add("dupKey");
		arg.setDuplicateKeys(dup);
		assertTrue(arg.getDuplicateKeys().contains("dupKey"));

		List<String> stored = new ArrayList<>();
		stored.add("storedKey");
		arg.setStoredMultiKeys(stored);
		assertTrue(arg.getStoredMultiKeys().contains("storedKey"));
	}

	/**
	 * 数値判定メソッド群の動作検証テストです。
	 */
	@Test
	public void testNumericCheck() {
		ClsCmnArg arg = new ClsCmnArg(logger);

		assertTrue(arg.isInteger("123"));
		assertTrue(arg.isInteger("-123"));
		assertTrue(arg.isInteger("0"));
		assertFalse(arg.isInteger("abc"));
		assertFalse(arg.isInteger("12.34"));
		assertFalse(arg.isInteger(null));
		assertFalse(arg.isInteger(""));

		assertTrue(arg.isDouble("123.45"));
		assertTrue(arg.isDouble("-123.45"));
		assertTrue(arg.isDouble("0.0"));
		assertTrue(arg.isDouble("123"));
		assertFalse(arg.isDouble("abc"));
		assertFalse(arg.isDouble(null));
		assertFalse(arg.isDouble(""));

		assertTrue(arg.isNumeric("100"));
		assertTrue(arg.isNumeric("100.5"));
		assertTrue(arg.isNumeric("-50"));
		assertFalse(arg.isNumeric("text"));
		assertFalse(arg.isNumeric(null));
	}

	/**
	 * 値の設定および取得メソッドの動作検証テストです。
	 */
	@Test
	public void testSetAndGetValue() {
		ClsCmnArg arg = new ClsCmnArg(logger);

		arg.setValue("boolKey", true);
		assertTrue(arg.getValue("boolKey", false));
		arg.setValue("boolKey", false);
		assertFalse(arg.getValue("boolKey", true));

		arg.setValue("intKey", 42);
		assertEquals(42, arg.getValue("intKey", 0));

		arg.setValue("longKey", 9999999999L);
		assertEquals(9999999999L, arg.getValue("longKey", 0L));

		arg.setValue("strKey", "hello");
		assertEquals("hello", arg.getValue("strKey", "def"));
		assertEquals("def", arg.getValue("notFoundKey", "def"));

		Map<String, String> map = new LinkedHashMap<>();
		arg.setValue(map, "dblKey", 3.14);
		arg.setValue(map, "csKey", "UTF-8");
		assertEquals(Double.valueOf(3.14), arg.getValue(map, "dblKey", 0.0));
		assertEquals(StandardCharsets.UTF_8, arg.getValue(map, "csKey", StandardCharsets.US_ASCII));

		List<String> strList = new ArrayList<>();
		strList.add("a");
		strList.add("b");
		arg.setValue("listKey", strList);
		assertEquals(2, arg.getValue("listKey", new ArrayList<>()).size());
	}

	/**
	 * 引数パース処理の動作検証テストです。
	 */
	@Test
	public void testParseArgs() {
		ClsCmnArg arg = new ClsCmnArg(logger);
		Map<String, String> propValKeys = new LinkedHashMap<>();
		propValKeys.put("u", "url");
		arg.setPropValKeys(propValKeys);

		List<String> duplicateKeys = new ArrayList<>();
		duplicateKeys.add("header");
		arg.setDuplicateKeys(duplicateKeys);

		String[] args = new String[]{
				"-u", "https://example.com",
				"-v", "-vv",
				"-header", "H1: V1",
				"-header", "H2: V2",
				"--silent", "true",
				"-trace", "2",
				"-1"
		};

		arg.parseArgs(args, false);

		assertEquals("https://example.com", arg.getValue("url", ""));
		assertEquals(2, arg.getVerbose());
		assertEquals(2, arg.getValue("header", new ArrayList<>()).size());
		assertEquals(2, arg.getMultiValArgs().get("header").size());
		assertEquals("H1: V1", arg.getMultiValArgs().get("header").get(0));
		assertEquals("H2: V2", arg.getMultiValArgs().get("header").get(1));
	}

	/**
	 * 設定ファイル読み込み処理の動作検証テストです。
	 *
	 * @throws IOException 入出力例外
	 */
	@Test
	public void testLoadFileToMap() throws IOException {
		ClsCmnArg arg = new ClsCmnArg(logger);
		File confFile = tempDir.resolve("sample.conf").toFile();
		String content = "# Comment line\n" +
				"url = https://example.com\n" +
				"--timeout = 5000\n" +
				"method = GET\n";
		Files.write(confFile.toPath(), content.getBytes(StandardCharsets.UTF_8));

		assertTrue(arg.exists(confFile.getAbsolutePath()));

		boolean ok = arg.loadFileToMap("CONF", confFile.getAbsolutePath(), false, true, "UTF-8");
		assertTrue(ok);
		assertEquals("https://example.com", arg.getValue("url", ""));
		assertEquals("5000", arg.getValue("timeout", ""));
		assertEquals("GET", arg.getValue("method", ""));
	}
}
