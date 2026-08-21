package tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link RestClient} の単体テストクラスです。
 */
public class RestClientTest {

	private Path tempDir;

	/**
	 * テスト実行前の初期化処理です。
	 *
	 * @throws IOException 入出力例外
	 */
	@Before
	public void setUp() throws IOException {
		tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "UnitTest", "RestClient", "RestClientTest");
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
	 * GETリクエストの DryRun 実行テストです。
	 */
	@Test
	public void testDryRunGet() {
		String[] args = new String[]{
				"--url", "https://example.com/api/test",
				"-X", "GET",
				"--dryrun",
				"-v", "0",
				"--silent"
		};
		new RestClient(args, false);
	}

	/**
	 * POSTリクエストのファイル入力および出力指定付き DryRun 実行テストです。
	 *
	 * @throws IOException 入出力例外
	 */
	@Test
	public void testDryRunPostWithDataAndOutput() throws IOException {
		File reqFile = tempDir.resolve("request.json").toFile();
		String reqContent = "{\"message\":\"hello\"}";
		Files.write(reqFile.toPath(), reqContent.getBytes(StandardCharsets.UTF_8));

		File outFile = tempDir.resolve("response.json").toFile();

		String[] args = new String[]{
				"--url", "https://example.com/api/echo",
				"-X", "POST",
				"-i", reqFile.getAbsolutePath(),
				"-o", outFile.getAbsolutePath(),
				"--dryrun",
				"-v", "0"
		};
		new RestClient(args, false);

		assertTrue(outFile.exists());
		String outText = new String(Files.readAllBytes(outFile.toPath()), StandardCharsets.UTF_8);
		assertTrue(outText.contains("DRYRUN"));
	}

	/**
	 * ヘルプおよびサンプル設定出力オプションの実行テストです。
	 */
	@Test
	public void testUsageOptions() {
		String[] argsHelp = new String[]{"-h"};
		new RestClient(argsHelp, false);

		String[] argsSample = new String[]{"--show-sample-config"};
		new RestClient(argsSample, false);
	}
}
