package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * プロパティ設定、文字コード判定、JSON変換、文字列置換などの各種ユーティリティ機能を提供するクラスです。
 */
public class ClsProperties extends ClsCmnArg {

	public static final int LVL_DEBUG = -1;
	public static final int LVL_INFO = 0;
	public static final int LVL_WARN = 10;
	public static final int LVL_ERROR = 20;
	public static final int LVL_FATAL = 30;
	public static final int MSG_OK = 0;
	public static final int MSG_SKIP = 10;
	public static final int MSG_ERROR = 20;
	public static final int DEFAULT_TIMEOUT_CONNECT = 10000;
	public static final int DEFAULT_TIMEOUT_SOCKET = 180000;
	public static final int DEFAULT_TIMEOUT_REQ = 180000;
	public static final int DEFAULT_TIMEOUT_RES = 180000;
	public static final int DEFAULT_DRYRUN_HTTP_CODE = 200;
	public static final int DEFAULT_DRYRUN_ELAPS_MSEC = 500;
	public static final String DEFAULT_DRYRUN_HTTP_BODY = "{\"message\":\"DRYRUN\"}";
	public static final String DEFAULT_STR_ENCODING = "UTF-8";
	public static final String DEFAULT_PROTOCOL_VERSION = "TLSv1.2,TLSv1.3";
	public static final String DEFAULT_TIMEZONE_DIFF_HOUR = "GMT+9";
	public static final String DEFAULT_USER_AGENT = "";
	public static final Boolean DEFAULT_IS_PARSE_REQ_JSON = Boolean.FALSE;
	public static final Boolean DEFAULT_IS_PARSE_RES_JSON = Boolean.FALSE;
	public static final Boolean DEFAULT_IS_PARSE_REQ_PRETTY = Boolean.FALSE;
	public static final Boolean DEFAULT_IS_PARSE_RES_PRETTY = Boolean.FALSE;
	public static final Boolean DEFAULT_IS_LOG_PARSE_REQ_ERROR = Boolean.TRUE;
	public static final Boolean DEFAULT_IS_LOG_PARSE_RES_ERROR = Boolean.TRUE;
	public static final Boolean DEFAULT_IS_JSON_CHAR_ESCAPES = Boolean.FALSE;
	public static final Boolean DEFAULT_REPLACE_REQ_BODY_BEFORE_PARSE = Boolean.FALSE;
	public static final Boolean DEFAULT_REPLACE_RES_BODY_BEFORE_PARSE = Boolean.FALSE;
	public static final Boolean DEFAULT_REPLACE_REQ_BODY_AFTER_PARSE = Boolean.TRUE;
	public static final Boolean DEFAULT_REPLACE_RES_BODY_AFTER_PARSE = Boolean.TRUE;

	// CONFIG
	public static final String VERBOSE = "Verbose";
	public static final String PATHFCONF = "PathFConf";
	public static final String PATHFINPUT = "PathFInput";
	public static final String PATHFOUTPUT = "PathFOutput";
	public static final String URL = "Url";
	public static final String IPADDR = "ConnIpAddress";
	public static final String METHOD = "HttpMethod";
	public static final String METHOD_NUMBER = "HttpMethodNumber";
	public static final String PROXY = "Proxy";
	public static final String TIMEOUT_CONNECT = "ConnectTimeout";
	public static final String TIMEOUT_SOCKET = "SocketTimeout";
	public static final String TIMEOUT_REQ = "ConntionReqTimeout";
	public static final String TIMEOUT_RES = "ResponseTimeout";
	public static final String REQ_BODY = "DataBody";
	public static final String REQ_HEADERS = "Headers";
	public static final String IS_PARSE_REQ_JSON = "IsParseReqJson";
	public static final String IS_PARSE_RES_JSON = "IsParseResJson";
	public static final String IS_PARSE_REQ_PRETTY = "IsParseReqPretty";
	public static final String IS_PARSE_RES_PRETTY = "IsParseResPretty";
	public static final String IS_LOG_PARSE_REQ_ERROR = "IsLogParseReqJsonError";
	public static final String IS_LOG_PARSE_RES_ERROR = "IsLogParseResJsonError";
	public static final String PROTOCOL_VERSION = "SslVersion";
	public static final String AUTH_USER_BASIC = "BasicAuthUser";
	public static final String IS_DRYRUN = "IsDryRun";
	public static final String IS_TRACE_LOG = "IsTraceLog";
	public static final String IS_SILENT = "IsSilent";
	public static final String IS_INSECURE = "IsInSecure";
	public static final String CNF_FILE_ENCODING = "ConfigFileEncoding";
	public static final String REQ_FILE_ENCODING = "ReqFileEncoding";
	public static final String OUT_FILE_ENCODING = "OutFileEncoding";
	public static final String REPLACE_URL_RULES = "ReplaceUrlRules";
	public static final String REPLACEALL_REQ_BODY_RULES = "ReplaceAllReqBodyRules";
	public static final String REPLACEALL_RES_BODY_RULES = "ReplaceAllResBodyRules";
	public static final String REPLACE_REQ_BODY_RULES = "ReplaceReqBodyRules";
	public static final String REPLACE_RES_BODY_RULES = "ReplaceResBodyRules";
	public static final String REPLACE_REQ_BODY_BEFORE_PARSE = "ReplaceReqBodyBeforeParse";
	public static final String REPLACE_RES_BODY_BEFORE_PARSE = "ReplaceResBodyBeforeParse";
	public static final String REPLACE_REQ_BODY_AFTER_PARSE = "ReplaceReqBodyAfterParse";
	public static final String REPLACE_RES_BODY_AFTER_PARSE = "ReplaceResBodyAfterParse";
	public static final String DRYRUN_HTTP_CODE = "IntDryrunHttpCode";
	public static final String DRYRUN_HTTP_BODY = "StrDryrunHttpBody";
	public static final String DRYRUN_ELAPS_MSEC = "IntDryrunElapsMSec";
	public static final String HTTP_REQ_ENCODING = "HttpRequestEncoding";
	public static final String HTTP_RES_ENCODING = "HttpResponseEncoding";
	public static final String IS_EXIT_2XX_IF_CODE_IS_2XX = "Exit2xxIfHttpCodeIs2xx";
	public static final String IS_EXIT_CODE_IS_HTTP_CODE = "ExitCodeIsHttpCode";
	public static final String IS_EXIT_MINUS_ONE_IF_WARN = "ExitMinusOneIfWarn";
	public static final String IS_EXIT_MINUS_ONE_IF_ERROR = "ExitMinusOneIfError";
	public static final String IS_ERR_IF_REQ_BODY_IS_EMPTY = "ErrorIfReqBodyIsEmpty";
	public static final String IS_WARN_IF_KEY_NOT_FOUND = "IsWarnIfKeyNotFound";
	public static final String REQUEST_HEADER_LIST_KEY = "HttpHeader";
	public static final String TIMEZONE_DIFF_HOUR = "TimeZoneDiffHour";
	public static final String USER_AGENT = "UserAgent";
	public static final String IS_ENCODE_UNICODE_ESCAPES = "IsEncodeUnicodeEscapes";
	public static final String IS_JSON_CHAR_ESCAPES = "IsJsonCharEscapes";
	public static final String IS_CONVERT_UNICODE_TO_UTF8 = "IsConvertUnicodeToUtf8";
	public static final String EXTRACTION_KEY_CSV = "ExtractionKeyCsv";

	// INTERNAL ONLY
	public static final String IS_BASIC_AUTH = "IsBasicAuth";
	public static final String IS_PROXY_AUTH = "IsProxyAuth";
	public static final String IS_REQUIRED_REQ_BODY = "IsRequiredReqBody";
	public static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
	public static final String USER_AGENT_FIREFOX = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0";
	public static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0";
	public static final String IS_PARSE_ERROR = "IsParseError";
	public static final String REPLACE_REQ = "ReplaceReq";
	public static final String REPLACE_RES = "ReplaceRes";
	public static final String TRACE = "Trace";
	public static final String EXIT_TYPE = "ExitType";
	public static final String OPTIONS = "Opetions";
	public static final String OPTION_DELIMITER = "OptionDelimiter";
	public static final String DUMPARGS = "DumpArgs";
	public static final String ARG_DEF = "ArgDef";

	private static final Pattern UNICODE_HEX_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

	private Logger logger;
	private volatile Map<String, String> propMap = new LinkedHashMap<>();
	private volatile Map<String, String> reqHeadersMap = new LinkedHashMap<>();
	private volatile Map<String, String> replaceUrlMap = new LinkedHashMap<>();
	private volatile Map<String, String> replaceReqBodyMap = new LinkedHashMap<>();
	private volatile Map<String, String> replaceAllReqBodyMap = new LinkedHashMap<>();
	private volatile Map<String, String> replaceResBodyMap = new LinkedHashMap<>();
	private volatile Map<String, String> replaceAllResBodyMap = new LinkedHashMap<>();
	private volatile Map<String, String> userAgentMap = new LinkedHashMap<>();
	private volatile StringBuilder jsonBodyBuilder = new StringBuilder();

	/**
	 * ロガーを指定してインスタンスを初期化するコンストラクタです。
	 *
	 * @param logger ログ出力用ロガー
	 *
	 * <pre>
	 * ClsProperties prop = new ClsProperties(logger);
	 * </pre>
	 */
	public ClsProperties(Logger logger) {
		super(logger);
		this.logger = logger;
	}

	/**
	 * プロパティマップを設定します。
	 *
	 * @param propMap プロパティマップ
	 *
	 * <pre>
	 * prop.setPropMap(map);
	 * </pre>
	 */
	public void setPropMap(Map<String, String> propMap) {
		this.propMap = (propMap != null) ? propMap : new LinkedHashMap<>();
	}

	/**
	 * User-Agent マップを設定します。
	 *
	 * @param userAgentMap User-Agentマップ
	 *
	 * <pre>
	 * prop.setUserAgentMap(uaMap);
	 * </pre>
	 */
	public void setUserAgentMap(Map<String, String> userAgentMap) {
		this.userAgentMap = (userAgentMap != null) ? userAgentMap : new LinkedHashMap<>();
	}

	/**
	 * リクエストヘッダーマップを設定します。
	 *
	 * @param reqHeadersMap リクエストヘッダーマップ
	 *
	 * <pre>
	 * prop.setReqHeadersMap(headersMap);
	 * </pre>
	 */
	public void setReqHeadersMap(Map<String, String> reqHeadersMap) {
		this.reqHeadersMap = (reqHeadersMap != null) ? reqHeadersMap : new LinkedHashMap<>();
	}

	/**
	 * URL置換ルールマップを設定します。
	 *
	 * @param replaceUrlMap URL置換ルールマップ
	 *
	 * <pre>
	 * prop.setReplaceUrlMap(urlMap);
	 * </pre>
	 */
	public void setReplaceUrlMap(Map<String, String> replaceUrlMap) {
		this.replaceUrlMap = (replaceUrlMap != null) ? replaceUrlMap : new LinkedHashMap<>();
	}

	/**
	 * リクエストボディ置換ルールマップ（replace）を設定します。
	 *
	 * @param replaceReqBodyMap 置換ルールマップ
	 *
	 * <pre>
	 * prop.setReplaceReqBodyMap(map);
	 * </pre>
	 */
	public void setReplaceReqBodyMap(Map<String, String> replaceReqBodyMap) {
		this.replaceReqBodyMap = (replaceReqBodyMap != null) ? replaceReqBodyMap : new LinkedHashMap<>();
	}

	/**
	 * リクエストボディ置換ルールマップ（replaceAll）を設定します。
	 *
	 * @param replaceAllReqBodyMap 置換ルールマップ
	 *
	 * <pre>
	 * prop.setReplaceAllReqBodyMap(map);
	 * </pre>
	 */
	public void setReplaceAllReqBodyMap(Map<String, String> replaceAllReqBodyMap) {
		this.replaceAllReqBodyMap = (replaceAllReqBodyMap != null) ? replaceAllReqBodyMap : new LinkedHashMap<>();
	}

	/**
	 * レスポンスボディ置換ルールマップ（replace）を設定します。
	 *
	 * @param replaceResBodyMap 置換ルールマップ
	 *
	 * <pre>
	 * prop.setReplaceResBodyMap(map);
	 * </pre>
	 */
	public void setReplaceResBodyMap(Map<String, String> replaceResBodyMap) {
		this.replaceResBodyMap = (replaceResBodyMap != null) ? replaceResBodyMap : new LinkedHashMap<>();
	}

	/**
	 * レスポンスボディ置換ルールマップ（replaceAll）を設定します。
	 *
	 * @param replaceAllResBodyMap 置換ルールマップ
	 *
	 * <pre>
	 * prop.setReplaceAllResBodyMap(map);
	 * </pre>
	 */
	public void setReplaceAllResBodyMap(Map<String, String> replaceAllResBodyMap) {
		this.replaceAllResBodyMap = (replaceAllResBodyMap != null) ? replaceAllResBodyMap : new LinkedHashMap<>();
	}

	/**
	 * プロパティマップを取得します。
	 *
	 * @return プロパティマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getPropMap();
	 * </pre>
	 */
	public Map<String, String> getPropMap() {
		return propMap;
	}

	/**
	 * User-Agent マップを取得します。
	 *
	 * @return User-Agentマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; uaMap = prop.getUserAgentMap();
	 * </pre>
	 */
	public Map<String, String> getUserAgentMap() {
		return userAgentMap;
	}

	/**
	 * リクエストヘッダーマップを取得します。
	 *
	 * @return リクエストヘッダーマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; h = prop.getReqHeadersMap();
	 * </pre>
	 */
	public Map<String, String> getReqHeadersMap() {
		return reqHeadersMap;
	}

	/**
	 * リクエストヘッダーマップを取得します（互換用）。
	 *
	 * @return リクエストヘッダーマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; h = prop.getHMapRequestHeaders();
	 * </pre>
	 */
	public Map<String, String> getHMapRequestHeaders() {
		return reqHeadersMap;
	}

	/**
	 * URL置換ルールマップを取得します。
	 *
	 * @return URL置換ルールマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getReplaceUrlMap();
	 * </pre>
	 */
	public Map<String, String> getReplaceUrlMap() {
		return replaceUrlMap;
	}

	/**
	 * リクエストボディ置換ルールマップ（replace）を取得します。
	 *
	 * @return 置換ルールマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getReplaceReqBodyMap();
	 * </pre>
	 */
	public Map<String, String> getReplaceReqBodyMap() {
		return replaceReqBodyMap;
	}

	/**
	 * リクエストボディ置換ルールマップ（replaceAll）を取得します。
	 *
	 * @return 置換ルールマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getReplaceAllReqBodyMap();
	 * </pre>
	 */
	public Map<String, String> getReplaceAllReqBodyMap() {
		return replaceAllReqBodyMap;
	}

	/**
	 * レスポンスボディ置換ルールマップ（replace）を取得します。
	 *
	 * @return 置換ルールマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getReplaceResBodyMap();
	 * </pre>
	 */
	public Map<String, String> getReplaceResBodyMap() {
		return replaceResBodyMap;
	}

	/**
	 * レスポンスボディ置換ルールマップ（replaceAll）を取得します。
	 *
	 * @return 置換ルールマップ
	 *
	 * <pre>
	 * Map&lt;String, String&gt; map = prop.getReplaceAllResBodyMap();
	 * </pre>
	 */
	public Map<String, String> getReplaceAllResBodyMap() {
		return replaceAllResBodyMap;
	}

	/**
	 * JSONボディ構築用 StringBuilder を取得します。
	 *
	 * @return StringBuilder インスタンス
	 *
	 * <pre>
	 * StringBuilder sb = prop.getJsonBodyBuilder();
	 * </pre>
	 */
	public StringBuilder getJsonBodyBuilder() {
		return jsonBodyBuilder;
	}

	/**
	 * 組み込み User-Agent（Chrome, Firefox, Edge）の定義を初期化・登録します。
	 *
	 * <pre>
	 * prop.initUserAgentMap();
	 * </pre>
	 */
	public void initUserAgentMap() {
		userAgentMap.put("CHROME", ClsProperties.USER_AGENT_CHROME);
		userAgentMap.put("FIREFOX", ClsProperties.USER_AGENT_FIREFOX);
		userAgentMap.put("EDGE", ClsProperties.USER_AGENT_EDGE);
	}

	/**
	 * 組み込み User-Agent 定義を登録します。
	 *
	 * <pre>
	 * prop.setUserAgentMap();
	 * </pre>
	 */
	public void setUserAgentMap() {
		initUserAgentMap();
	}

	/**
	 * 登録されているリクエストヘッダー一覧をデバッグログに出力します。
	 *
	 * <pre>
	 * prop.listHttpHeaders();
	 * </pre>
	 */
	public void listHttpHeaders() {
		if (logger != null) {
			logger.debug("HTTPHEADERの数 = " + reqHeadersMap.size());
			for (Map.Entry<String, String> entry : reqHeadersMap.entrySet()) {
				logger.debug("HTTPHEADER = " + entry.getKey() + ": " + entry.getValue());
			}
		}
	}

	/**
	 * 設定パラメータ一覧を PrintStream に出力します。
	 *
	 * @param ps 出力先 PrintStream
	 *
	 * <pre>
	 * prop.list(System.out);
	 * </pre>
	 */
	public void list(PrintStream ps) {
		if (ps != null) {
			for (Map.Entry<String, String> entry : getNamedArgs().entrySet()) {
				ps.println(entry.getKey() + " = " + entry.getValue());
			}
		}
	}

	/**
	 * 設定パラメータ一覧を PrintWriter に出力します。
	 *
	 * @param pw 出力先 PrintWriter
	 *
	 * <pre>
	 * prop.list(new PrintWriter(System.out));
	 * </pre>
	 */
	public void list(PrintWriter pw) {
		if (pw != null) {
			for (Map.Entry<String, String> entry : getNamedArgs().entrySet()) {
				pw.println(entry.getKey() + " = " + entry.getValue());
			}
		}
	}

	/**
	 * 設定パラメータ一覧を文字列として取得します。
	 *
	 * @return パラメータ一覧文字列
	 *
	 * <pre>
	 * String listStr = prop.list();
	 * </pre>
	 */
	public String list() {
		String lf = System.getProperty("line.separator");
		StringBuilder buff = new StringBuilder(lf);
		for (Map.Entry<String, String> entry : getNamedArgs().entrySet()) {
			buff.append(entry.getKey()).append(" = ").append(entry.getValue()).append(lf);
		}
		return buff.toString();
	}

	/**
	 * UNIXエポック秒を指定書式の日付文字列に変換します。
	 *
	 * @param unixTime UNIX時間（秒）
	 * @param format 日付書式パターン（例: "yyyy/MM/dd HH:mm:ss"）
	 * @return フォーマットされた日付文字列
	 *
	 * <pre>
	 * String dateStr = prop.formatUnixTime(1700000000, "yyyy/MM/dd HH:mm:ss");
	 * </pre>
	 */
	public String formatUnixTime(int unixTime, String format) {
		return formatUnixTime(unixTime * 1000L, format);
	}

	/**
	 * UNIXエポックミリ秒を指定書式の日付文字列に変換します。
	 *
	 * @param millisec UNIX時間（ミリ秒）
	 * @param format 日付書式パターン
	 * @return フォーマットされた日付文字列
	 *
	 * <pre>
	 * String dateStr = prop.formatUnixTime(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss");
	 * </pre>
	 */
	public String formatUnixTime(long millisec, String format) {
		Instant instant = Instant.ofEpochMilli(millisec);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(resolveZoneId());
		return formatter.format(instant);
	}

	/**
	 * 指定ファイルの文字コードを自動判定して取得します。
	 *
	 * @param path 対象ファイルパス
	 * @return 判定された文字コード名（判定不能時は null）
	 *
	 * <pre>
	 * String enc = prop.detectCharset("data.txt");
	 * </pre>
	 */
	public String detectCharset(String path) {
		String encoding = null;
		try {
			File f = new File(path);
			encoding = UniversalDetector.detectCharset(f);
		} catch (Exception e) {
			if (logger != null) {
				logger.error("EXCEPTION detectCharset: " + path, e);
			}
		}
		return encoding;
	}

	/**
	 * ファイルを指定された文字コードで全行読み込み、文字列として取得します。
	 *
	 * @param path ファイルパス
	 * @param encodeName 文字コード名
	 * @return ファイル本文文字列
	 *
	 * <pre>
	 * String text = prop.readFile("sample.txt", "UTF-8");
	 * </pre>
	 */
	public String readFile(String path, String encodeName) {
		StringBuilder strBldr = new StringBuilder();
		String body = null;
		Charset charset = getValue(ClsProperties.REQ_FILE_ENCODING, Charset.forName(encodeName));
		try (FileInputStream fis = new FileInputStream(path);
		     InputStreamReader isr = new InputStreamReader(fis, charset);
		     BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				strBldr.append(line);
			}
			body = strBldr.toString();
		} catch (IOException ioex) {
			if (logger != null) {
				logger.error("EXCEPTION : " + path, ioex);
			}
		} catch (Exception ex) {
			if (logger != null) {
				logger.error("EXCEPTION : " + path, ex);
			}
		}
		return body;
	}

	/**
	 * 指定文字列を指定ファイルパスに指定文字セットで書き込みます。
	 *
	 * @param path 出力ファイルパス
	 * @param message 出力内容
	 * @param charset 文字セット
	 * @return 書き込み成功時は true、失敗時は false
	 *
	 * <pre>
	 * boolean ok = prop.writeFile("output.json", jsonStr, StandardCharsets.UTF_8);
	 * </pre>
	 */
	public boolean writeFile(String path, String message, Charset charset) {
		boolean isOk = true;
		if (message == null) {
			message = "";
		}
		try (FileOutputStream fos = new FileOutputStream(path);
		     OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
		     BufferedWriter bw = new BufferedWriter(osw)) {
			bw.write(message);
		} catch (IOException ioex) {
			isOk = false;
			if (logger != null) {
				logger.error("IOEXCEPTION : " + path, ioex);
			}
		} catch (Exception ex) {
			isOk = false;
			if (logger != null) {
				logger.error("EXCEPTION : " + path, ex);
			}
		}
		return isOk;
	}

	/**
	 * JSONファイルを読み込み、指定オプションに従って整形・変換して取得します。
	 *
	 * @param path ファイルパス
	 * @param encodeName 文字コード名
	 * @param isPretty Pretty整形を行うか否か
	 * @param isUnicodeEscape 特殊文字エスケープを行うか否か
	 * @param isRequest リクエストボディか否か
	 * @param isErrorMessage エラーメッセージを出力するか否か
	 * @return 整形後のJSON文字列
	 *
	 * <pre>
	 * String json = prop.readJsonFile("data.json", "UTF-8", true, false, true, true);
	 * </pre>
	 */
	public String readJsonFile(String path, String encodeName, Boolean isPretty, Boolean isUnicodeEscape, Boolean isRequest, Boolean isErrorMessage) {
		String jsonStr = readFile(path, encodeName);
		if (jsonStr == null || jsonStr.isEmpty()) {
			return null;
		}
		return parseJsonStr(jsonStr, isPretty, isUnicodeEscape, isRequest, isErrorMessage);
	}

	/**
	 * JSON文字列をパースし、指定オプションに従って整形・抽出・変換して取得します。
	 *
	 * @param jsonStr JSON文字列
	 * @param isPretty Pretty整形を行うか否か
	 * @param isUnicodeEscape 特殊文字エスケープを行うか否か
	 * @param isRequest リクエストボディか否か
	 * @param isErrorMessage エラーログを出力するか否か
	 * @return 整形後のJSON文字列
	 *
	 * <pre>
	 * String formatted = prop.parseJsonStr("{\"a\":1}", true, false, false, true);
	 * </pre>
	 */
	public String parseJsonStr(String jsonStr, Boolean isPretty, Boolean isUnicodeEscape, Boolean isRequest, Boolean isErrorMessage) {
		setValue(ClsProperties.IS_PARSE_ERROR, false);
		String jsonBody = jsonStr;
		if (jsonStr == null || jsonStr.isEmpty()) {
			if (logger != null) {
				logger.error("STRING IS NULL OR EMPTY");
			}
			return null;
		}
		if (getValue(ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE, ClsProperties.DEFAULT_REPLACE_REQ_BODY_BEFORE_PARSE)
				|| getValue(ClsProperties.REPLACE_RES_BODY_BEFORE_PARSE, ClsProperties.DEFAULT_REPLACE_RES_BODY_BEFORE_PARSE)) {
			jsonStr = replaceAllBody(jsonStr, isRequest);
			jsonStr = replaceBody(jsonStr, isRequest);
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(jsonStr, Object.class);
			jsonStr = mapper.writeValueAsString(json);

			if (!Boolean.TRUE.equals(isRequest) && !getValue(ClsProperties.EXTRACTION_KEY_CSV, "").isEmpty()) {
				String[] keyList = getValue(ClsProperties.EXTRACTION_KEY_CSV, "").split("[/]");
				for (String key : keyList) {
					if (!key.isEmpty()) {
						JsonNode node = mapper.readTree(jsonStr);
						if (node.has(key)) {
							jsonStr = node.get(key).toString();
						}
					}
				}
			}

			if (Boolean.TRUE.equals(isPretty)) {
				jsonBody = mapper.readTree(jsonStr).toPrettyString();
			} else {
				jsonBody = mapper.readTree(jsonStr).toString();
			}

			if (Boolean.TRUE.equals(isUnicodeEscape)) {
				mapper.getFactory().setCharacterEscapes(new CharacterEscapes() {
					private static final long serialVersionUID = 1L;
					private final int[] escapeCodes;
					{
						escapeCodes = standardAsciiEscapesForJSON();
						escapeCodes['"'] = CharacterEscapes.ESCAPE_STANDARD;
						escapeCodes['\''] = CharacterEscapes.ESCAPE_STANDARD;
						escapeCodes['/'] = CharacterEscapes.ESCAPE_STANDARD;
						escapeCodes['\n'] = CharacterEscapes.ESCAPE_STANDARD;
						escapeCodes['>'] = CharacterEscapes.ESCAPE_STANDARD;
						escapeCodes['<'] = CharacterEscapes.ESCAPE_STANDARD;
					}
					@Override
					public int[] getEscapeCodesForAscii() {
						return escapeCodes;
					}
					@Override
					public SerializableString getEscapeSequence(int ch) {
						return null;
					}
				});
				jsonBody = mapper.writeValueAsString(jsonBody);
			}
		} catch (Exception e) {
			if (Boolean.TRUE.equals(isErrorMessage) && logger != null) {
				logger.error("EXCEPTION : " + e.getMessage(), e);
				logger.error("--> JSON = " + jsonStr);
			}
			jsonBody = null;
			setValue(ClsProperties.IS_PARSE_ERROR, true);
		}
		return jsonBody;
	}

	/**
	 * 指定ミリ秒スリープします（最大500ミリ秒単位で分割実行）。
	 *
	 * @param sleepMillis スリープ時間（ミリ秒）
	 *
	 * <pre>
	 * prop.doSleep(500);
	 * </pre>
	 */
	public void doSleep(int sleepMillis) {
		int maxSleepCount = 500;
		int loopMSec = Math.min(sleepMillis, maxSleepCount);
		int maxLoopCount = (maxSleepCount < sleepMillis) ? sleepMillis / maxSleepCount : 1;
		try {
			for (int i = 0; i < maxLoopCount; i++) {
				Thread.sleep(loopMSec);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception ignored) {
			// ignore
		}
	}

	/**
	 * 文字列の前後空白をトリムします。空文字列の場合は null を返します。
	 *
	 * @param str 対象文字列
	 * @return トリム後の文字列（空の場合は null）
	 *
	 * <pre>
	 * String trimmed = prop.doTrim("  test  ");
	 * </pre>
	 */
	public String doTrim(String str) {
		if (str != null) {
			str = str.trim();
			if (str.isEmpty()) {
				str = null;
			}
		}
		return str;
	}

	/**
	 * CSV形式のキー=値リストを指定区切り文字で分割し、名前付き引数マップにマージ登録します。
	 *
	 * @param csv CSV文字列
	 * @param delimiter 区切り文字パターン
	 * @return 正常終了時は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = prop.splitMergeProp("k1=v1,k2=v2", "[,|]");
	 * </pre>
	 */
	public Boolean splitMergeProp(String csv, String delimiter) {
		boolean isOk = true;
		int i = 0;
		try {
			String[] tempList = csv.split(delimiter);
			for (String item : tempList) {
				String[] keyVal = item.split("=");
				if (keyVal.length >= 2) {
					String key = keyVal[0].trim();
					String val = keyVal[1].trim();
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("CONF[" + String.format("%03d", i + 1) + "] OPTIONS = " + key + ": " + val);
					}
					getNamedArgs().put(key, val);
					i++;
				}
			}
		} catch (Exception e) {
			isOk = false;
			if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
				logger.error("splitMergeProp error", e);
			}
		}
		return isOk;
	}

	/**
	 * ヘッダーCSV文字列（Key:Value形式）をパースし、リクエストヘッダーマップに登録します。
	 *
	 * @param headerCsv ヘッダーCSV文字列
	 * @return 正常終了時は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = prop.splitReqHeaders("Content-Type: application/json, Accept: * / *");
	 * </pre>
	 */
	public Boolean splitReqHeaders(String headerCsv) {
		boolean isOk = true;
		int i = 0;
		try {
			String[] tempList = headerCsv.split("[,|]");
			for (String header : tempList) {
				String[] keyVal = header.split(":");
				if (keyVal.length >= 2) {
					String headerKey = keyVal[0].trim().replaceAll("^\"", "").replaceAll("\"$", "");
					String headerVal = keyVal[1].trim().replaceAll("^\"", "").replaceAll("\"$", "");
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("CONF[" + String.format("%03d", i + 1) + "] HTTPHEADER = " + headerKey + ": " + headerVal);
					}
					reqHeadersMap.put(headerKey, headerVal);
					i++;
				}
			}
		} catch (Exception e) {
			isOk = false;
			if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
				logger.error("splitReqHeaders error", e);
			}
		}
		return isOk;
	}

	/**
	 * URL置換ルールCSV文字列（old=new形式）をパースし、URL置換マップに登録します。
	 *
	 * @param replaces 置換ルール文字列
	 * @return 正常終了時は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = prop.splitMapUrl("localhost=127.0.0.1,http://=https://");
	 * </pre>
	 */
	public Boolean splitMapUrl(String replaces) {
		boolean isOk = true;
		replaceUrlMap.clear();
		try {
			String[] tempList = replaces.split("[,|]");
			for (String keyValueStr : tempList) {
				String[] keyVal = keyValueStr.split("=");
				if (keyVal.length >= 2) {
					String key = keyVal[0].trim();
					String val = keyVal[1].trim();
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("key=" + key + " / val=" + val);
					}
					replaceUrlMap.put(key, val);
				}
			}
		} catch (Exception e) {
			isOk = false;
			if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
				logger.error("splitMapUrl error", e);
			}
		}
		return isOk;
	}

	/**
	 * 単純置換（replace）ルールCSV文字列をパースし、リクエスト/レスポンスボディ置換マップに登録します。
	 *
	 * @param replaces 置換ルール文字列
	 * @param isRequest リクエスト対象の場合は true、レスポンス対象の場合は false
	 * @return 正常終了時は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = prop.splitMapReplace("A=B,C=D", true);
	 * </pre>
	 */
	public Boolean splitMapReplace(String replaces, Boolean isRequest) {
		boolean isOk = true;
		String mapName;
		Map<String, String> rules;
		if (Boolean.TRUE.equals(isRequest)) {
			rules = replaceReqBodyMap;
			mapName = "replaceReqBodyMap";
		} else {
			rules = replaceResBodyMap;
			mapName = "replaceResBodyMap";
		}
		rules.clear();
		try {
			String[] tempList = replaces.split("[,|]");
			for (String keyValueStr : tempList) {
				String[] keyVal = keyValueStr.split("=");
				if (keyVal.length >= 2) {
					String key = keyVal[0].trim();
					String val = keyVal[1].trim();
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug(mapName + "[" + key + "] = " + val);
					}
					rules.put(key, val);
				}
			}
		} catch (Exception e) {
			isOk = false;
			if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
				logger.error("splitMapReplace error", e);
			}
		}
		return isOk;
	}

	/**
	 * 正規表現置換（replaceAll）ルールCSV文字列をパースし、リクエスト/レスポンスボディ置換マップに登録します。
	 *
	 * @param replaces 置換ルール文字列
	 * @param isRequest リクエスト対象の場合は true、レスポンス対象の場合は false
	 * @return 正常終了時は true、エラー時は false
	 *
	 * <pre>
	 * boolean ok = prop.splitReplaceAll("__RCRLF__=__EMPTY__", true);
	 * </pre>
	 */
	public Boolean splitReplaceAll(String replaces, Boolean isRequest) {
		boolean isOk = true;
		String mapName;
		Map<String, String> rules;
		if (Boolean.TRUE.equals(isRequest)) {
			rules = replaceAllReqBodyMap;
			mapName = "replaceAllReqBodyMap";
		} else {
			rules = replaceAllResBodyMap;
			mapName = "replaceAllResBodyMap";
		}
		rules.clear();
		try {
			String[] tempList = replaces.split("[,|]");
			for (String keyValueStr : tempList) {
				String[] keyVal = keyValueStr.split("=");
				if (keyVal.length >= 2) {
					String key = keyVal[0].trim();
					String val = keyVal[1].trim();
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug(mapName + "[" + key + "] = " + val);
					}
					rules.put(key, val);
				}
			}
		} catch (Exception e) {
			isOk = false;
			if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
				logger.error("splitReplaceAll error", e);
			}
		}
		return isOk;
	}

	/**
	 * 登録されたURL置換ルールを適用してURL文字列を置換します。
	 *
	 * @param url 置換前URL
	 * @return 置換後URL
	 *
	 * <pre>
	 * String replaced = prop.replaceUrl("http://localhost/api");
	 * </pre>
	 */
	public String replaceUrl(String url) {
		String replaced = url;
		if (url != null && !url.isEmpty()) {
			for (Map.Entry<String, String> entry : replaceUrlMap.entrySet()) {
				try {
					String key = entry.getKey();
					String val = entry.getValue();
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("replaceUrlMap[" + key + "] = " + val);
					}
					replaced = replaced.replace(key, val);
				} catch (Exception e) {
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.error("replaceUrl error", e);
					}
				}
			}
		}
		return replaced;
	}

	/**
	 * 単純置換（replace）ルールを本文文字列に適用します。
	 *
	 * @param body 置換前本文
	 * @param isRequest リクエスト対象か否か
	 * @return 置換後本文
	 *
	 * <pre>
	 * String res = prop.replaceBody("hello world", true);
	 * </pre>
	 */
	public String replaceBody(String body, Boolean isRequest) {
		String replaced = body;
		Map<String, String> rules = Boolean.TRUE.equals(isRequest) ? replaceReqBodyMap : replaceResBodyMap;
		if (body != null && !body.isEmpty()) {
			for (Map.Entry<String, String> entry : rules.entrySet()) {
				try {
					String key = entry.getKey();
					String val = entry.getValue();
					String targetKey = decodePlaceholder(key);
					String targetVal = decodePlaceholder(val);
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("[" + targetKey + "] => [" + targetVal + "]");
					}
					replaced = replaced.replace(targetKey, targetVal);
				} catch (Exception e) {
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.error("replaceBody error", e);
					}
				}
			}
		}
		return replaced;
	}

	/**
	 * 正規表現置換（replaceAll）ルールを本文文字列に適用します。
	 *
	 * @param body 置換前本文
	 * @param isRequest リクエスト対象か否か
	 * @return 置換後本文
	 *
	 * <pre>
	 * String res = prop.replaceAllBody("hello\r\nworld", false);
	 * </pre>
	 */
	public String replaceAllBody(String body, Boolean isRequest) {
		String replaced = body;
		Map<String, String> rules = Boolean.TRUE.equals(isRequest) ? replaceAllReqBodyMap : replaceAllResBodyMap;
		if (body != null && !body.isEmpty()) {
			for (Map.Entry<String, String> entry : rules.entrySet()) {
				try {
					String key = entry.getKey();
					String val = entry.getValue();
					String targetKey = decodeRegexPlaceholder(key);
					String targetVal = decodeRegexPlaceholder(val);
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.debug("[" + targetKey + "] => [" + targetVal + "]");
					}
					replaced = replaced.replaceAll(targetKey, targetVal);
				} catch (Exception e) {
					if (0 < getValue(ClsProperties.IS_TRACE_LOG, 0) && logger != null) {
						logger.error("replaceAllBody error", e);
					}
				}
			}
		}
		return replaced;
	}

	/**
	 * 文字列内の非英数字文字を Unicode エスケープシーケンス（\\uXXXX 形式）に変換します。
	 *
	 * @param orig 元文字列
	 * @return エスケープ変換後文字列
	 *
	 * <pre>
	 * String escaped = prop.encodeUnicodeEscapes("テスト");
	 * </pre>
	 */
	public String encodeUnicodeEscapes(String orig) {
		if (orig == null) {
			return "";
		}
		char[] charValue = orig.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char ch : charValue) {
			if (ch != '_' && !('0' <= ch && ch <= '9') && !('a' <= ch && ch <= 'z') && !('A' <= ch && ch <= 'Z')) {
				String unicodeCh = Integer.toHexString((int) ch);
				sb.append("\\u");
				for (int i = 0; i < 4 - unicodeCh.length(); i++) {
					sb.append("0");
				}
				sb.append(unicodeCh);
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Unicode エスケープ文字列（\\uXXXX 形式）をデコードして UTF-8 文字列に復元します。
	 *
	 * @param unicodeStr Unicodeエスケープ文字列
	 * @return デコード後文字列
	 *
	 * <pre>
	 * String text = prop.convertUnicode("\\\\u30c6\\\\u30b9\\\\u30c8");
	 * </pre>
	 */
	public String convertUnicode(String unicodeStr) {
		if (unicodeStr == null) {
			return null;
		}
		AtomicInteger lastIndex = new AtomicInteger(0);
		Matcher matcher = UNICODE_HEX_PATTERN.matcher(unicodeStr);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new java.util.Iterator<String>() {
			@Override
			public boolean hasNext() {
				return matcher.find();
			}
			@Override
			public String next() {
				String segment = unicodeStr.substring(lastIndex.getAndSet(matcher.end()), matcher.start());
				char decodedChar = (char) Integer.parseInt(matcher.group(1), 16);
				return segment + decodedChar;
			}
		}, Spliterator.ORDERED), false).collect(Collectors.joining()) + unicodeStr.substring(lastIndex.get());
	}

	/**
	 * Unicode エスケープ文字列を UTF-8 文字列に復元します。
	 *
	 * @param unicodeStr Unicodeエスケープ文字列
	 * @return デコード後文字列
	 *
	 * <pre>
	 * String text = prop.convertUnicodeToUtf8("\\u30c6\\u30b9\\u30c8");
	 * </pre>
	 */
	public String convertUnicodeToUtf8(String unicodeStr) {
		return convertUnicode(unicodeStr);
	}

	/**
	 * タイムゾーン設定を解決します。
	 *
	 * @return ZoneId インスタンス
	 */
	private ZoneId resolveZoneId() {
		String tzStr = getValue(ClsProperties.TIMEZONE_DIFF_HOUR, ClsProperties.DEFAULT_TIMEZONE_DIFF_HOUR);
		try {
			return ZoneId.of(tzStr);
		} catch (Exception e) {
			try {
				return ZoneId.of(tzStr, ZoneId.SHORT_IDS);
			} catch (Exception ex) {
				return ZoneId.systemDefault();
			}
		}
	}

	/**
	 * 特殊プレースホルダー文字列を実際の制御文字等にデコードします。
	 *
	 * @param str デコード対象文字列
	 * @return デコード後文字列
	 */
	private String decodePlaceholder(String str) {
		if (str == null) {
			return "";
		}
		return str.replace("__RTAB__", "\t")
				.replace("__CRLF__", "\n")
				.replace("__CR__", "\r")
				.replace("__LF__", "\f")
				.replace("__RSPACE__", " ")
				.replace("__TAB__", "\t")
				.replace("__SPACE__", " ")
				.replace("__EMPTY__", "")
				.replace("__DQ__", "\"")
				.replace("__SQ__", "'");
	}

	/**
	 * 正規表現用の特殊プレースホルダー文字列をデコードします。
	 *
	 * @param str デコード対象文字列
	 * @return デコード後文字列
	 */
	private String decodeRegexPlaceholder(String str) {
		if (str == null) {
			return "";
		}
		return str.replace("__RTAB__", "\t")
				.replace("__RCRLF__", "\n")
				.replace("__RCR__", "\r")
				.replace("__RLF__", "\f")
				.replace("__RSPACE__", "\\s")
				.replace("__TAB__", "\t")
				.replace("__CRLF__", "\\n")
				.replace("__CR__", "\\r")
				.replace("__LF__", "\\f")
				.replace("__SPACE__", " ")
				.replace("__EMPTY__", "")
				.replace("__DQ__", "\\\"")
				.replace("__SQ__", "\\\'");
	}
}
