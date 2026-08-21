package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

/**
 * コマンドライン引数および設定ファイルからのパラメータ解析を行う共通引数処理クラスです。
 */
public class ClsCmnArg {

	public static final int USAGE_NONE = 0;
	public static final int USAGE_USAGE = 1;
	public static final int USAGE_SHOW_SAMPLE_CONFIG = 2;

	private static final Pattern OPTION_PREFIX_PATTERN = Pattern.compile("^\\-{1,2}([^\\-].*)$");
	private static final Pattern KEY_VAL_LINE_PATTERN = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.+)\\s*$");
	private static final Pattern COMMENT_LINE_PATTERN = Pattern.compile("^\\s*#.*");
	private static final Pattern MULTI_V_PATTERN = Pattern.compile("^v+$");

	private Logger logger;
	private volatile Map<String, String> namedArgs = new LinkedHashMap<>();
	private volatile Map<String, String> propValKeys = new LinkedHashMap<>();
	private volatile Map<String, String> propBlnKeys = new LinkedHashMap<>();
	private volatile Map<String, List<String>> multiValArgs = new LinkedHashMap<>();
	private volatile List<String> duplicateKeys = new ArrayList<>();
	private volatile List<String> storedMultiKeys = new ArrayList<>();
	private volatile int verbose = 0;
	private volatile int traceLog = 0;
	private volatile int usageFlag = USAGE_NONE;

	/**
	 * デフォルトコンストラクタです。
	 *
	 * <pre>
	 * ClsCmnArg cmnArg = new ClsCmnArg();
	 * </pre>
	 */
	public ClsCmnArg() {
	}

	/**
	 * ロガーを指定してインスタンスを初期化するコンストラクタです。
	 *
	 * @param logger ログ出力用ロガー
	 *
	 * <pre>
	 * ClsCmnArg cmnArg = new ClsCmnArg(logger);
	 * </pre>
	 */
	public ClsCmnArg(Logger logger) {
		this.logger = logger;
	}

	/**
	 * ロガーを設定します。
	 *
	 * @param logger ログ出力用ロガー
	 *
	 * <pre>
	 * cmnArg.setLogger(logger);
	 * </pre>
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * 名前付き引数マップを設定します。
	 *
	 * @param namedArgs 名前付き引数マップ
	 *
	 * <pre>
	 * cmnArg.setNamedArgs(namedArgsMap);
	 * </pre>
	 */
	public void setNamedArgs(Map<String, String> namedArgs) {
		this.namedArgs = (namedArgs != null) ? namedArgs : new LinkedHashMap<>();
	}

	/**
	 * 値付きプロパティキーのエイリアスマップを設定します。
	 *
	 * @param propValKeys 値付きプロパティキーマップ
	 *
	 * <pre>
	 * cmnArg.setPropValKeys(valKeys);
	 * </pre>
	 */
	public void setPropValKeys(Map<String, String> propValKeys) {
		this.propValKeys = (propValKeys != null) ? propValKeys : new LinkedHashMap<>();
	}

	/**
	 * 真偽値プロパティキーのエイリアスマップを設定します。
	 *
	 * @param propBlnKeys 真偽値プロパティキーマップ
	 *
	 * <pre>
	 * cmnArg.setPropBlnKeys(blnKeys);
	 * </pre>
	 */
	public void setPropBlnKeys(Map<String, String> propBlnKeys) {
		this.propBlnKeys = (propBlnKeys != null) ? propBlnKeys : new LinkedHashMap<>();
	}

	/**
	 * 複数値保持用マップを設定します。
	 *
	 * @param multiValArgs 複数値保持用マップ
	 *
	 * <pre>
	 * cmnArg.setMultiValArgs(multiValArgsMap);
	 * </pre>
	 */
	public void setMultiValArgs(Map<String, List<String>> multiValArgs) {
		this.multiValArgs = (multiValArgs != null) ? multiValArgs : new LinkedHashMap<>();
	}

	/**
	 * 重複を許可するキーリストを設定します。
	 *
	 * @param duplicateKeys 重複許可キーリスト
	 *
	 * <pre>
	 * cmnArg.setDuplicateKeys(dupKeys);
	 * </pre>
	 */
	public void setDuplicateKeys(List<String> duplicateKeys) {
		this.duplicateKeys = (duplicateKeys != null) ? duplicateKeys : new ArrayList<>();
	}

	/**
	 * 既に格納された複数値キーのリストを設定します。
	 *
	 * @param storedMultiKeys 格納済みキーリスト
	 *
	 * <pre>
	 * cmnArg.setStoredMultiKeys(storedKeys);
	 * </pre>
	 */
	public void setStoredMultiKeys(List<String> storedMultiKeys) {
		this.storedMultiKeys = (storedMultiKeys != null) ? storedMultiKeys : new ArrayList<>();
	}

	/**
	 * ロガーを取得します。
	 *
	 * @return ロガーインスタンス
	 *
	 * <pre>
	 * Logger logger = cmnArg.getLogger();
	 * </pre>
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * 名前付き引数マップを取得します。
	 *
	 * @return 名前付き引数マップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; args = cmnArg.getNamedArgs();
	 * </pre>
	 */
	public Map<String, String> getNamedArgs() {
		return namedArgs;
	}

	/**
	 * 値付きプロパティキーのエイリアスマップを取得します。
	 *
	 * @return 値付きプロパティキーマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; valKeys = cmnArg.getPropValKeys();
	 * </pre>
	 */
	public Map<String, String> getPropValKeys() {
		return propValKeys;
	}

	/**
	 * 真偽値プロパティキーのエイリアスマップを取得します。
	 *
	 * @return 真偽値プロパティキーマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; blnKeys = cmnArg.getPropBlnKeys();
	 * </pre>
	 */
	public Map<String, String> getPropBlnKeys() {
		return propBlnKeys;
	}

	/**
	 * 複数値保持用マップを取得します。
	 *
	 * @return 複数値保持用マップ
	 *
	 * <pre>
	 * Map&lt;String, List&lt;String&gt;&gt; multi = cmnArg.getMultiValArgs();
	 * </pre>
	 */
	public Map<String, List<String>> getMultiValArgs() {
		return multiValArgs;
	}

	/**
	 * 重複を許可するキーリストを取得します。
	 *
	 * @return 重複許可キーリスト
	 *
	 * <pre>
	 * List&lt;String&gt; dupKeys = cmnArg.getDuplicateKeys();
	 * </pre>
	 */
	public List<String> getDuplicateKeys() {
		return duplicateKeys;
	}

	/**
	 * 既に格納された複数値キーのリストを取得します。
	 *
	 * @return 格納済みキーリスト
	 *
	 * <pre>
	 * List&lt;String&gt; storedKeys = cmnArg.getStoredMultiKeys();
	 * </pre>
	 */
	public List<String> getStoredMultiKeys() {
		return storedMultiKeys;
	}

	/**
	 * 冗長出力レベルを取得します。
	 *
	 * @return 冗長レベル
	 *
	 * <pre>
	 * int verbose = cmnArg.getVerbose();
	 * </pre>
	 */
	public int getVerbose() {
		return verbose;
	}

	/**
	 * Usage表示フラグを取得します（互換用）。
	 *
	 * @return Usageフラグ定数値
	 *
	 * <pre>
	 * int flg = cmnArg.getUsageFlg();
	 * </pre>
	 */
	public int getUsageFlg() {
		return usageFlag;
	}

	/**
	 * Usage表示フラグを取得します。
	 *
	 * @return Usageフラグ定数値
	 *
	 * <pre>
	 * int flg = cmnArg.getUsageFlag();
	 * </pre>
	 */
	public int getUsageFlag() {
		return usageFlag;
	}

	/**
	 * コマンドライン引数配列をパースして名前付き引数マップに格納します。
	 *
	 * @param args 引数配列
	 * @param ignoreCase 大文字小文字を区別しないか否か
	 *
	 * <pre>
	 * cmnArg.parseArgs(new String[]{"-url", "http://example.com", "-v"}, false);
	 * </pre>
	 */
	public void parseArgs(String[] args, boolean ignoreCase) {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			String key = "";
			String value = "";
			String arg = ignoreCase ? args[i].toLowerCase() : args[i];
			boolean isMatch = false;
			Matcher keyMatcher = OPTION_PREFIX_PATTERN.matcher(arg);
			if (keyMatcher.find()) {
				key = keyMatcher.group(1).trim();
				isMatch = true;
				if (isNumeric(key)) {
					isMatch = false;
				}
			}
			if (isMatch) {
				if (i < args.length - 1) {
					value = args[i + 1];
					Matcher valMatcher = OPTION_PREFIX_PATTERN.matcher(value);
					if (valMatcher.find()) {
						if (!isNumeric(value)) {
							value = "";
						}
					}
				}
				setNamedArg("ARG", i + 1, key, value, true);
			}
		}
	}

	/**
	 * コマンドライン引数配列をパースして名前付き引数マップに格納します（Boolean ラッパー版）。
	 *
	 * @param args 引数配列
	 * @param ignoreCase 大文字小文字を区別しないか否か
	 *
	 * <pre>
	 * cmnArg.parseArgs(args, Boolean.FALSE);
	 * </pre>
	 */
	public void parseArgs(String[] args, Boolean ignoreCase) {
		parseArgs(args, Boolean.TRUE.equals(ignoreCase));
	}

	/**
	 * 名前付き引数マップへキーと値を設定・登録します。
	 *
	 * @param name ログ出力用プレフィックス
	 * @param row 行インデックスまたは引数番号
	 * @param key キー文字列
	 * @param value 値文字列
	 * @param overwrite 既存値を上書きするか否か
	 *
	 * <pre>
	 * cmnArg.setNamedArg("ARG", 1, "url", "https://example.com", true);
	 * </pre>
	 */
	public void setNamedArg(String name, int row, String key, String value, boolean overwrite) {
		if (key == null || value == null) {
			return;
		}
		key = key.trim();
		value = value.trim();

		if ("h".equals(key)) {
			if (value.isEmpty()) {
				key = "help";
				usageFlag = USAGE_USAGE;
			}
		}
		if ("show-sample-config".equals(key)) {
			if (value.isEmpty()) {
				key = "help";
				usageFlag = USAGE_SHOW_SAMPLE_CONFIG;
			}
		}
		if (MULTI_V_PATTERN.matcher(key).matches()) {
			if (value.isEmpty()) {
				value = String.valueOf(key.length());
			}
			if (!isInteger(value)) {
				value = "1";
			}
			verbose = Integer.parseInt(value);
			key = "v";
		}
		if ("brief".equals(key)) {
			if (isInteger(value)) {
				int tempInt;
				try {
					tempInt = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					tempInt = 1;
				}
				value = String.valueOf(tempInt * -1);
			} else {
				value = "-1";
			}
			verbose = Integer.parseInt(value);
			key = "v";
		}
		if ("trace".equals(key)) {
			if (isInteger(value)) {
				traceLog = Integer.parseInt(value);
			} else {
				traceLog = 1;
			}
			value = String.valueOf(traceLog);
		}

		if (propValKeys.containsKey(key)) {
			key = propValKeys.get(key);
		}
		if (propBlnKeys.containsKey(key)) {
			key = propBlnKeys.get(key);
			value = "false".equalsIgnoreCase(value) ? "false" : "true";
		}

		boolean isSetValue = true;
		if (namedArgs.containsKey(key) && !overwrite) {
			isSetValue = false;
		}

		value = value.replaceAll("^\\-", "-");

		if (duplicateKeys.contains(key)) {
			if (!storedMultiKeys.contains(key)) {
				if (0 < traceLog && logger != null) {
					logger.debug(name + "[" + String.format("%03d", row + 1) + "][O] MULTI : " + key + " = " + value);
				}
				List<String> list = multiValArgs.computeIfAbsent(key, k -> new ArrayList<>());
				list.add(value);
			} else {
				if (0 < traceLog && logger != null) {
					logger.debug(name + "[" + String.format("%03d", row + 1) + "][-] MULTI : " + key + " = " + value);
				}
			}
		} else {
			if (0 < traceLog && logger != null) {
				String flag = isSetValue ? "[O]" : "[-]";
				logger.debug(name + "[" + String.format("%03d", row + 1) + "]" + flag + " NAMED : " + key + " = " + value);
			}
		}

		if (isSetValue) {
			namedArgs.put(key, value);
		}
	}

	/**
	 * 名前付き引数マップへキーと値を設定します（Boolean ラッパー版）。
	 *
	 * @param name ログ出力用プレフィックス
	 * @param row 行インデックス
	 * @param key キー文字列
	 * @param value 値文字列
	 * @param overwrite 上書きフラグ
	 *
	 * <pre>
	 * cmnArg.setNamedArg("ARG", 1, "url", "https://example.com", Boolean.TRUE);
	 * </pre>
	 */
	public void setNamedArg(String name, int row, String key, String value, Boolean overwrite) {
		setNamedArg(name, row, key, value, Boolean.TRUE.equals(overwrite));
	}

	/**
	 * 文字列が数値（整数または浮動小数点数）か判定します。
	 *
	 * @param str 判定対象文字列
	 * @return 数値の場合は true、それ以外は false
	 *
	 * <pre>
	 * boolean num = cmnArg.isNumeric("123.45");
	 * </pre>
	 */
	public boolean isNumeric(String str) {
		return isInteger(str) || isDouble(str);
	}

	/**
	 * 文字列が整数値か判定します。
	 *
	 * @param str 判定対象文字列
	 * @return 整数の場合は true、それ以外は false
	 *
	 * <pre>
	 * boolean isInt = cmnArg.isInteger("100");
	 * </pre>
	 */
	public boolean isInteger(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		try {
			Integer.parseInt(str.trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 文字列が浮動小数点数値か判定します。
	 *
	 * @param str 判定対象文字列
	 * @return 浮動小数点数の場合は true、それ以外は false
	 *
	 * <pre>
	 * boolean isDbl = cmnArg.isDouble("3.14");
	 * </pre>
	 */
	public boolean isDouble(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		try {
			Double.parseDouble(str.trim());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 指定マップに boolean 値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 真偽値
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(map, "isDebug", true);
	 * </pre>
	 */
	public Map<String, String> setValue(Map<String, String> map, String key, boolean value) {
		map.put(key, value ? "true" : "false");
		return map;
	}

	/**
	 * 指定マップに int 値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 整数値
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(map, "timeout", 5000);
	 * </pre>
	 */
	public Map<String, String> setValue(Map<String, String> map, String key, int value) {
		map.put(key, String.valueOf(value));
		return map;
	}

	/**
	 * 指定マップに long 値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 長整数値
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(map, "timestamp", 1600000000000L);
	 * </pre>
	 */
	public Map<String, String> setValue(Map<String, String> map, String key, long value) {
		map.put(key, String.valueOf(value));
		return map;
	}

	/**
	 * 指定マップに文字列値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 文字列値
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(map, "url", "https://example.com");
	 * </pre>
	 */
	public Map<String, String> setValue(Map<String, String> map, String key, String value) {
		map.put(key, value);
		return map;
	}

	/**
	 * 指定マップにリスト値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 文字列リスト
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(multiMap, "headers", headerList);
	 * </pre>
	 */
	public Map<String, List<String>> setValue(Map<String, List<String>> map, String key, List<String> value) {
		map.put(key, value);
		return map;
	}

	/**
	 * 指定マップに double 値を設定します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param value 浮動小数点数値
	 * @return 設定後のマップ
	 *
	 * <pre>
	 * cmnArg.setValue(map, "rate", 1.5);
	 * </pre>
	 */
	public Map<String, String> setValue(Map<String, String> map, String key, double value) {
		map.put(key, String.valueOf(value));
		return map;
	}

	/**
	 * インスタンスの名前付き引数マップに double 値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 浮動小数点数値
	 *
	 * <pre>
	 * cmnArg.setValue("factor", 2.5);
	 * </pre>
	 */
	public void setValue(String key, double value) {
		namedArgs.put(key, String.valueOf(value));
	}

	/**
	 * インスタンスの名前付き引数マップに boolean 値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 真偽値
	 *
	 * <pre>
	 * cmnArg.setValue("silent", true);
	 * </pre>
	 */
	public void setValue(String key, boolean value) {
		namedArgs.put(key, value ? "true" : "false");
	}

	/**
	 * インスタンスの名前付き引数マップに int 値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 整数値
	 *
	 * <pre>
	 * cmnArg.setValue("retry", 3);
	 * </pre>
	 */
	public void setValue(String key, int value) {
		namedArgs.put(key, String.valueOf(value));
	}

	/**
	 * インスタンスの名前付き引数マップに long 値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 長整数値
	 *
	 * <pre>
	 * cmnArg.setValue("maxSize", 1048576L);
	 * </pre>
	 */
	public void setValue(String key, long value) {
		namedArgs.put(key, String.valueOf(value));
	}

	/**
	 * インスタンスの名前付き引数マップに文字列値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 文字列値
	 *
	 * <pre>
	 * cmnArg.setValue("method", "GET");
	 * </pre>
	 */
	public void setValue(String key, String value) {
		namedArgs.put(key, value);
	}

	/**
	 * インスタンスの複数値引数マップにリスト値を設定します。
	 *
	 * @param key キー文字列
	 * @param value 文字列リスト
	 *
	 * <pre>
	 * cmnArg.setValue("headers", headerList);
	 * </pre>
	 */
	public void setValue(String key, List<String> value) {
		multiValArgs.put(key, value);
	}

	/**
	 * 指定マップから文字列値を取得します。キーが存在しない場合はデフォルト値を返します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した文字列値
	 *
	 * <pre>
	 * String url = cmnArg.getValue(map, "url", "http://localhost");
	 * </pre>
	 */
	public String getValue(Map<String, String> map, String key, String defaultValue) {
		if (map == null || key == null || key.isEmpty()) {
			return defaultValue;
		}
		String value = map.getOrDefault(key, defaultValue);
		if ("null".equalsIgnoreCase(value)) {
			return null;
		}
		return value;
	}

	/**
	 * 指定マップから boolean 値を取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した boolean 値
	 *
	 * <pre>
	 * boolean silent = cmnArg.getValue(map, "silent", false);
	 * </pre>
	 */
	public boolean getValue(Map<String, String> map, String key, boolean defaultValue) {
		String valStr = getValue(map, key, String.valueOf(defaultValue));
		if ("true".equalsIgnoreCase(valStr)) {
			return true;
		} else if ("false".equalsIgnoreCase(valStr)) {
			return false;
		}
		return defaultValue;
	}

	/**
	 * 指定マップから int 値を取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した int 値
	 *
	 * <pre>
	 * int timeout = cmnArg.getValue(map, "timeout", 10000);
	 * </pre>
	 */
	public int getValue(Map<String, String> map, String key, int defaultValue) {
		String valStr = getValue(map, key, String.valueOf(defaultValue));
		try {
			return Integer.parseInt(valStr);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 指定マップから long 値を取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した long 値
	 *
	 * <pre>
	 * long limit = cmnArg.getValue(map, "limit", 1000L);
	 * </pre>
	 */
	public long getValue(Map<String, String> map, String key, long defaultValue) {
		String valStr = getValue(map, key, String.valueOf(defaultValue));
		try {
			return Long.parseLong(valStr);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 指定マップから Double 値を取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した Double 値
	 *
	 * <pre>
	 * Double rate = cmnArg.getValue(map, "rate", 1.0);
	 * </pre>
	 */
	public Double getValue(Map<String, String> map, String key, Double defaultValue) {
		String valStr = getValue(map, key, String.valueOf(defaultValue));
		try {
			return Double.parseDouble(valStr);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 指定マップから Charset を取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param encoding デフォルト文字セット
	 * @return 取得した Charset
	 *
	 * <pre>
	 * Charset cs = cmnArg.getValue(map, "charset", StandardCharsets.UTF_8);
	 * </pre>
	 */
	public Charset getValue(Map<String, String> map, String key, Charset encoding) {
		String tempStr = getValue(map, key, "");
		if (tempStr == null || tempStr.isEmpty()) {
			return encoding;
		}
		try {
			return Charset.forName(tempStr);
		} catch (Exception e) {
			return encoding;
		}
	}

	/**
	 * 指定マップから文字列リストを取得します。
	 *
	 * @param map 対象マップ
	 * @param key キー文字列
	 * @param defaultValue デフォルト値リスト
	 * @return 取得したリスト
	 *
	 * <pre>
	 * List&lt;String&gt; headers = cmnArg.getValue(map, "headers", new ArrayList&lt;&gt;());
	 * </pre>
	 */
	public List<String> getValue(Map<String, List<String>> map, String key, List<String> defaultValue) {
		if (map != null && key != null && !key.isEmpty() && map.containsKey(key)) {
			return map.get(key);
		}
		return defaultValue;
	}

	/**
	 * 名前付き引数から文字列値を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した文字列値
	 *
	 * <pre>
	 * String url = cmnArg.getValue("url", "https://localhost");
	 * </pre>
	 */
	public String getValue(String key, String defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 名前付き引数から boolean 値を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した boolean 値
	 *
	 * <pre>
	 * boolean isDryrun = cmnArg.getValue("dryrun", false);
	 * </pre>
	 */
	public boolean getValue(String key, boolean defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 名前付き引数から int 値を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した int 値
	 *
	 * <pre>
	 * int verbose = cmnArg.getValue("v", 0);
	 * </pre>
	 */
	public int getValue(String key, int defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 名前付き引数から long 値を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した long 値
	 *
	 * <pre>
	 * long timeout = cmnArg.getValue("timeout", 180000L);
	 * </pre>
	 */
	public long getValue(String key, long defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 名前付き引数から Double 値を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値
	 * @return 取得した Double 値
	 *
	 * <pre>
	 * Double d = cmnArg.getValue("factor", 1.0);
	 * </pre>
	 */
	public Double getValue(String key, Double defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 名前付き引数から Charset を取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト文字セット
	 * @return 取得した Charset
	 *
	 * <pre>
	 * Charset cs = cmnArg.getValue("encoding", StandardCharsets.UTF_8);
	 * </pre>
	 */
	public Charset getValue(String key, Charset defaultValue) {
		return getValue(namedArgs, key, defaultValue);
	}

	/**
	 * 複数値引数マップから文字列リストを取得します。
	 *
	 * @param key キー文字列
	 * @param defaultValue デフォルト値リスト
	 * @return 取得したリスト
	 *
	 * <pre>
	 * List&lt;String&gt; headers = cmnArg.getValue("Headers", new ArrayList&lt;&gt;());
	 * </pre>
	 */
	public List<String> getValue(String key, List<String> defaultValue) {
		return getValue(multiValArgs, key, defaultValue);
	}

	/**
	 * 指定ファイルパスが存在するか判定します。
	 *
	 * @param path ファイルパス
	 * @return ファイルが存在する場合は true、それ以外は false
	 *
	 * <pre>
	 * boolean exists = cmnArg.exists("config.properties");
	 * </pre>
	 */
	public boolean exists(String path) {
		if (path != null && !path.trim().isEmpty()) {
			return new File(path).exists();
		}
		return false;
	}

	/**
	 * 指定設定ファイルを読み込み、キー=値のペアを引数マップに展開・登録します。
	 *
	 * @param name ログ用プレフィックス
	 * @param path ファイルパス
	 * @param overwrite 上書きを許可するか否か
	 * @param removeMinusFromKey キーの先頭ハイフンを除去するか否か
	 * @param encodeName ファイル文字コード
	 * @return 正常に読み込めた場合は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = cmnArg.loadFileToMap("CNF", "conf.txt", false, false, "UTF-8");
	 * </pre>
	 */
	public boolean loadFileToMap(String name, String path, boolean overwrite, boolean removeMinusFromKey, String encodeName) {
		boolean isOk = true;
		int row = 0;
		for (String key : duplicateKeys) {
			if (multiValArgs.containsKey(key) && !storedMultiKeys.contains(key)) {
				storedMultiKeys.add(key);
			}
		}
		try (FileInputStream fis = new FileInputStream(path);
		     InputStreamReader isr = new InputStreamReader(fis, Charset.forName(encodeName));
		     BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				row++;
				if (!COMMENT_LINE_PATTERN.matcher(line).matches()) {
					Matcher keyValMatcher = KEY_VAL_LINE_PATTERN.matcher(line);
					if (keyValMatcher.find()) {
						String key = keyValMatcher.group(1);
						String val = keyValMatcher.group(2);
						if (removeMinusFromKey) {
							Matcher keyMatcher = OPTION_PREFIX_PATTERN.matcher(key);
							if (keyMatcher.find()) {
								key = keyMatcher.group(1).trim();
							}
						}
						setNamedArg(name, row, key, val, overwrite);
					}
				}
			}
		} catch (IOException ioex) {
			isOk = false;
			if (logger != null) {
				logger.error("IOEXCEPTION : " + row + " : " + path, ioex);
			}
		} catch (Exception ex) {
			isOk = false;
			if (logger != null) {
				logger.error("EXCEPTION : " + row + " : " + path, ex);
			}
		}
		return isOk;
	}

	/**
	 * 設定ファイルを読み込むラッパーメソッドです（Boolean ラッパー版）。
	 *
	 * @param name ログ用プレフィックス
	 * @param path ファイルパス
	 * @param overwrite 上書きフラグ
	 * @param removeMinusFromKey ハイフン除去フラグ
	 * @param encodeName 文字コード
	 * @return 成否
	 *
	 * <pre>
	 * Boolean ok = cmnArg.loadFileToMap("CNF", "conf.txt", Boolean.FALSE, Boolean.FALSE, "UTF-8");
	 * </pre>
	 */
	public Boolean loadFileToMap(String name, String path, Boolean overwrite, Boolean removeMinusFromKey, String encodeName) {
		return loadFileToMap(name, path, Boolean.TRUE.equals(overwrite), Boolean.TRUE.equals(removeMinusFromKey), encodeName);
	}
}
