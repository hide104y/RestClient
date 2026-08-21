package tool;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST API クライアントのメインエントリーポイントクラスです。
 */
public class RestClient {

	private Logger logger;
	private ClsProperties prop;
	private Map<String, String> argsMap = new LinkedHashMap<>();
	private Map<String, List<String>> multiValArgs = new LinkedHashMap<>();
	private boolean isKillJvm;
	private long startTime;
	private long endTime;
	private String className = Thread.currentThread().getStackTrace()[1].getClassName();

	/**
	 * コマンドライン実行時のメインエントリーポイントです。
	 *
	 * @param args コマンドライン引数配列
	 *
	 * <pre>
	 * RestClient.main(new String[]{"--url", "https://example.com/api", "-X", "GET"});
	 * </pre>
	 */
	public static void main(String[] args) {
		new RestClient(args, true);
	}

	/**
	 * 引数配列および JVM 終了フラグを指定して RestClient を実行するコンストラクタです。
	 *
	 * @param args コマンドライン引数配列
	 * @param isKillJvm 処理完了時に System.exit() を呼び出して JVM を終了するか否か
	 *
	 * <pre>
	 * RestClient client = new RestClient(new String[]{"--url", "https://example.com"}, false);
	 * </pre>
	 */
	public RestClient(String[] args, boolean isKillJvm) {
		exec(args, isKillJvm);
	}

	/**
	 * REST リクエストの構築・実行・レスポンス処理を行うメイン処理メソッドです。
	 *
	 * @param args 引数配列
	 * @param isKillJvm JVM終了フラグ
	 *
	 * <pre>
	 * exec(args, false);
	 * </pre>
	 */
	private void exec(String[] args, boolean isKillJvm) {
		ClsHttpClient httpClient = null;
		boolean isOk = true;
		String options = null;
		String optionDelimiter = "[,|]";
		this.isKillJvm = isKillJvm;
		this.logger = LogManager.getLogger(RestClient.class);
		this.prop = new ClsProperties(logger);
		this.prop.setLogger(logger);
		this.startTime = System.currentTimeMillis();
		String tempStr;
		int tempInt;

		int httpCode = ClsProperties.LVL_ERROR;
		int exitCode = ClsProperties.LVL_ERROR;

		Map<String, String> propValKeys = new LinkedHashMap<>();
		Map<String, String> propBlnKeys = new LinkedHashMap<>();
		List<String> duplicateKeys = new ArrayList<>();

		propValKeys.put("c", ClsProperties.PATHFCONF);
		propValKeys.put("conf", ClsProperties.PATHFCONF);
		propValKeys.put("i", ClsProperties.PATHFINPUT);
		propValKeys.put("input", ClsProperties.PATHFINPUT);
		propValKeys.put("o", ClsProperties.PATHFOUTPUT);
		propValKeys.put("output", ClsProperties.PATHFOUTPUT);
		propValKeys.put("url", ClsProperties.URL);
		propValKeys.put("ip", ClsProperties.IPADDR);
		propValKeys.put("x", ClsProperties.PROXY);
		propValKeys.put("proxy", ClsProperties.PROXY);
		propValKeys.put("X", ClsProperties.METHOD);
		propValKeys.put("request", ClsProperties.METHOD);
		propValKeys.put("method", ClsProperties.METHOD);
		propValKeys.put("d", ClsProperties.REQ_BODY);
		propValKeys.put("data", ClsProperties.REQ_BODY);
		propValKeys.put("H", ClsProperties.REQ_HEADERS);
		propValKeys.put("header", ClsProperties.REQ_HEADERS);
		propBlnKeys.put("preq", ClsProperties.IS_PARSE_REQ_JSON);
		propBlnKeys.put("pres", ClsProperties.IS_PARSE_RES_JSON);
		propValKeys.put("timeout", ClsProperties.TIMEOUT_CONNECT);
		propValKeys.put("u", ClsProperties.AUTH_USER_BASIC);
		propValKeys.put("user", ClsProperties.AUTH_USER_BASIC);
		propBlnKeys.put("k", ClsProperties.IS_INSECURE);
		propBlnKeys.put("insecure", ClsProperties.IS_INSECURE);
		propValKeys.put("ssl-ver", ClsProperties.PROTOCOL_VERSION);
		propValKeys.put("v", ClsProperties.VERBOSE);
		propBlnKeys.put("silent", ClsProperties.IS_SILENT);
		propBlnKeys.put("trace", ClsProperties.TRACE);
		propBlnKeys.put("dryrun", ClsProperties.IS_DRYRUN);
		propValKeys.put("exit", ClsProperties.EXIT_TYPE);
		propValKeys.put("A", ClsProperties.USER_AGENT);
		propValKeys.put("user-agent", ClsProperties.USER_AGENT);
		propValKeys.put("options", ClsProperties.OPTIONS);
		propValKeys.put("option-delimiter", ClsProperties.OPTION_DELIMITER);
		propBlnKeys.put("json-escapes", ClsProperties.IS_JSON_CHAR_ESCAPES);
		propBlnKeys.put("unicode-escapes", ClsProperties.IS_ENCODE_UNICODE_ESCAPES);
		propBlnKeys.put("unicode-to-utf8", ClsProperties.IS_CONVERT_UNICODE_TO_UTF8);
		propValKeys.put("node", ClsProperties.EXTRACTION_KEY_CSV);
		propValKeys.put("replace-url", ClsProperties.REPLACE_URL_RULES);
		propValKeys.put("replace-reqbody", ClsProperties.REPLACE_REQ_BODY_RULES);
		propValKeys.put("replace-resbody", ClsProperties.REPLACE_RES_BODY_RULES);
		propValKeys.put("replaceall-reqbody", ClsProperties.REPLACEALL_REQ_BODY_RULES);
		propValKeys.put("replaceall-resbody", ClsProperties.REPLACEALL_RES_BODY_RULES);
		propValKeys.put("replace-req", ClsProperties.REPLACE_REQ);
		propValKeys.put("replace-res", ClsProperties.REPLACE_RES);
		propValKeys.put("dumpargs", ClsProperties.DUMPARGS);
		propValKeys.put("arg-def", ClsProperties.ARG_DEF);
		duplicateKeys.add(ClsProperties.REQ_HEADERS);
		duplicateKeys.add(ClsProperties.REQ_BODY);

		prop.setNamedArgs(argsMap);
		prop.setPropValKeys(propValKeys);
		prop.setPropBlnKeys(propBlnKeys);
		prop.setDuplicateKeys(duplicateKeys);
		prop.setMultiValArgs(multiValArgs);
		prop.parseArgs(args, false);

		if (argsMap.containsKey(ClsProperties.ARG_DEF)) {
			String path = prop.getValue(ClsProperties.ARG_DEF, "");
			if (prop.exists(path)) {
				String encodeName = prop.getValue(ClsProperties.CNF_FILE_ENCODING, ClsProperties.DEFAULT_STR_ENCODING);
				if (encodeName == null || encodeName.isEmpty() || "AUTO".equalsIgnoreCase(encodeName)) {
					encodeName = prop.detectCharset(path);
				}
				if (encodeName == null || encodeName.isEmpty() || "UNKNOWN".equalsIgnoreCase(encodeName)) {
					encodeName = ClsProperties.DEFAULT_STR_ENCODING;
				}
				prop.loadFileToMap("DEF", path, false, true, encodeName);
			} else {
				System.err.println("No such a file (ARG --arg-def) : " + path);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}

		if (argsMap.containsKey(ClsProperties.PATHFCONF)) {
			String path = prop.getValue(ClsProperties.PATHFCONF, "");
			if (prop.exists(path)) {
				String encodeName = prop.getValue(ClsProperties.CNF_FILE_ENCODING, ClsProperties.DEFAULT_STR_ENCODING);
				if (encodeName == null || encodeName.isEmpty() || "AUTO".equalsIgnoreCase(encodeName)) {
					encodeName = prop.detectCharset(path);
				}
				if (encodeName == null || encodeName.isEmpty() || "UNKNOWN".equalsIgnoreCase(encodeName)) {
					encodeName = ClsProperties.DEFAULT_STR_ENCODING;
				}
				prop.loadFileToMap("CNF", path, false, false, encodeName);
			} else {
				System.err.println("No such a file (ARG -c) : " + path);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}

		if (argsMap.containsKey(ClsProperties.REPLACE_REQ)) {
			tempStr = prop.getValue(ClsProperties.REPLACE_REQ, "after");
			if (tempStr.equalsIgnoreCase("before")) {
				prop.setValue(ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE, true);
				prop.setValue(ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE, false);
			} else {
				prop.setValue(ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE, false);
				prop.setValue(ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE, true);
			}
		}

		if (argsMap.containsKey(ClsProperties.REPLACE_RES)) {
			tempStr = prop.getValue(ClsProperties.REPLACE_RES, "after");
			if (tempStr.equalsIgnoreCase("before")) {
				prop.setValue(argsMap, ClsProperties.REPLACE_RES_BODY_BEFORE_PARSE, true);
				prop.setValue(argsMap, ClsProperties.REPLACE_RES_BODY_AFTER_PARSE, false);
			} else {
				prop.setValue(ClsProperties.REPLACE_RES_BODY_BEFORE_PARSE, false);
				prop.setValue(ClsProperties.REPLACE_RES_BODY_AFTER_PARSE, true);
			}
		}

		tempInt = prop.getValue(ClsProperties.TIMEOUT_CONNECT, -1);
		prop.setValue(ClsProperties.TIMEOUT_CONNECT, (tempInt >= 0 ? tempInt : ClsProperties.DEFAULT_TIMEOUT_CONNECT));
		if (!argsMap.containsKey(ClsProperties.TIMEOUT_SOCKET)) {
			prop.setValue(ClsProperties.TIMEOUT_SOCKET, (tempInt >= 0 ? tempInt : ClsProperties.DEFAULT_TIMEOUT_SOCKET));
		}
		if (!argsMap.containsKey(ClsProperties.TIMEOUT_REQ)) {
			prop.setValue(ClsProperties.TIMEOUT_REQ, (tempInt >= 0 ? tempInt : ClsProperties.DEFAULT_TIMEOUT_REQ));
		}
		if (!argsMap.containsKey(ClsProperties.TIMEOUT_RES)) {
			prop.setValue(ClsProperties.TIMEOUT_RES, (tempInt >= 0 ? tempInt : ClsProperties.DEFAULT_TIMEOUT_RES));
		}

		if (argsMap.containsKey(ClsProperties.TRACE)) {
			tempInt = prop.getValue(ClsProperties.TRACE, 1);
			prop.setValue(ClsProperties.IS_TRACE_LOG, tempInt);
		}

		tempStr = prop.getValue(ClsProperties.EXIT_TYPE, "").toLowerCase();
		if ("httpcode".equals(tempStr) || "httpstatus".equals(tempStr) || "http".equals(tempStr)) {
			prop.setValue(ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE, true);
			prop.setValue(ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX, false);
		} else if ("normal".equals(tempStr)) {
			prop.setValue(ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE, false);
			prop.setValue(ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX, true);
		} else {
			prop.setValue(ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE, false);
			prop.setValue(ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX, false);
		}

		if (argsMap.containsKey(ClsProperties.OPTIONS)) {
			tempStr = prop.getValue(argsMap, ClsProperties.OPTIONS, "");
			if (!tempStr.isEmpty()) {
				options = tempStr;
			}
		}

		if (argsMap.containsKey(ClsProperties.OPTION_DELIMITER)) {
			tempStr = prop.getValue(argsMap, ClsProperties.OPTION_DELIMITER, "");
			if (!tempStr.isEmpty()) {
				optionDelimiter = tempStr;
			}
		}

		if (argsMap.containsKey(ClsProperties.DUMPARGS)) {
			argsMap.forEach((k, v) -> logger.info("PROP : " + k + " = " + v));
		}

		propBlnKeys.clear();
		propValKeys.clear();

		if (options != null && !options.isEmpty()) {
			prop.splitMergeProp(options, optionDelimiter);
		}

		switch (prop.getUsageFlag()) {
			case ClsCmnArg.USAGE_USAGE:
				showUsage(ClsProperties.LVL_WARN);
				return;
			case ClsCmnArg.USAGE_SHOW_SAMPLE_CONFIG:
				showSampleConfig();
				return;
			default:
				break;
		}

		if (prop.getValue(ClsProperties.URL, "").isEmpty()) {
			System.err.println("Please specify the URL");
			showUsage(ClsProperties.LVL_ERROR);
			return;
		}

		if (prop.getValue(ClsProperties.VERBOSE, 0) > 1) {
			System.out.println("===<<< [" + className + "] START : " + prop.formatUnixTime(startTime, "yyyy/MM/dd HH:mm:ss") + ">>>===");
			logger.info("VERBOSE  : " + prop.getValue(ClsProperties.VERBOSE, 0));
		}

		if (multiValArgs.containsKey(ClsProperties.REQ_HEADERS)) {
			List<String> list = prop.getValue(ClsProperties.REQ_HEADERS, new ArrayList<>());
			list.forEach(prop::splitReqHeaders);
		}

		if (multiValArgs.containsKey(ClsProperties.REQ_BODY)) {
			List<String> list = prop.getValue(ClsProperties.REQ_BODY, new ArrayList<>());
			list.forEach(prop.getJsonBodyBuilder()::append);
		}

		if (!prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT).isEmpty()) {
			prop.setUserAgentMap();
			tempStr = prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT).toUpperCase();
			if (tempStr.length() < 15 && prop.getUserAgentMap().containsKey(tempStr)) {
				prop.setValue(ClsProperties.USER_AGENT, prop.getUserAgentMap().get(tempStr));
			}
			prop.getUserAgentMap().clear();
		}

		if (prop.getReplaceAllReqBodyMap().isEmpty()) {
			tempStr = prop.getValue(ClsProperties.REPLACEALL_REQ_BODY_RULES, "");
			if (!tempStr.isEmpty() && !prop.splitReplaceAll(tempStr, true)) {
				System.err.println("ERROR OCCURED (CONFIG " + ClsProperties.REPLACEALL_REQ_BODY_RULES + ") : " + tempStr);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}
		if (prop.getReplaceAllResBodyMap().isEmpty()) {
			tempStr = prop.getValue(ClsProperties.REPLACEALL_RES_BODY_RULES, "");
			if (!tempStr.isEmpty() && !prop.splitReplaceAll(tempStr, false)) {
				System.err.println("ERROR OCCURED (CONFIG " + ClsProperties.REPLACEALL_RES_BODY_RULES + ") : " + tempStr);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}
		if (prop.getReplaceReqBodyMap().isEmpty()) {
			tempStr = prop.getValue(ClsProperties.REPLACE_REQ_BODY_RULES, "");
			if (!tempStr.isEmpty() && !prop.splitMapReplace(tempStr, true)) {
				System.err.println("ERROR OCCURED (CONFIG " + ClsProperties.REPLACE_REQ_BODY_RULES + ") : " + tempStr);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}
		if (prop.getReplaceResBodyMap().isEmpty()) {
			tempStr = prop.getValue(ClsProperties.REPLACE_RES_BODY_RULES, "");
			if (!tempStr.isEmpty() && !prop.splitMapReplace(tempStr, false)) {
				System.err.println("ERROR OCCURED (CONFIG " + ClsProperties.REPLACE_RES_BODY_RULES + ") : " + tempStr);
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}
		if (!prop.getValue(ClsProperties.REPLACE_URL_RULES, "").isEmpty()) {
			if (!prop.splitMapUrl(prop.getValue(ClsProperties.REPLACE_URL_RULES, ""))) {
				System.err.println("ERROR OCCURED (CONFIG " + ClsProperties.REPLACE_URL_RULES + ") : " + prop.getValue(ClsProperties.REPLACE_URL_RULES, ""));
				showUsage(ClsProperties.LVL_ERROR);
				return;
			}
		}

		if (prop.getValue(ClsProperties.PATHFINPUT, "").isEmpty()) {
			if (prop.getJsonBodyBuilder().length() > 0) {
				String body = prop.getJsonBodyBuilder().toString();
				if (prop.getValue(ClsProperties.IS_PARSE_REQ_JSON, ClsProperties.DEFAULT_IS_PARSE_REQ_JSON)) {
					body = prop.parseJsonStr(
							body,
							prop.getValue(ClsProperties.IS_PARSE_REQ_PRETTY, ClsProperties.DEFAULT_IS_PARSE_REQ_PRETTY),
							prop.getValue(ClsProperties.IS_JSON_CHAR_ESCAPES, ClsProperties.DEFAULT_IS_JSON_CHAR_ESCAPES),
							true,
							prop.getValue(ClsProperties.IS_LOG_PARSE_REQ_ERROR, ClsProperties.DEFAULT_IS_LOG_PARSE_REQ_ERROR)
						);
				}
				if (prop.getValue(ClsProperties.IS_PARSE_ERROR, false)) {
					isOk = false;
				}
				if (isOk) {
					prop.setValue(ClsProperties.REQ_BODY, body);
				}
			}
		} else {
			if (prop.exists(prop.getValue(ClsProperties.PATHFINPUT, ""))) {
				String encoding = prop.getValue(ClsProperties.REQ_FILE_ENCODING, ClsProperties.DEFAULT_STR_ENCODING);
				if (encoding == null || encoding.isEmpty() || "AUTO".equalsIgnoreCase(encoding)) {
					encoding = prop.detectCharset(prop.getValue(ClsProperties.PATHFINPUT, ""));
				}
				if (encoding == null || encoding.isEmpty() || "UNKNOWN".equalsIgnoreCase(encoding)) {
					encoding = ClsProperties.DEFAULT_STR_ENCODING;
				}
				if (prop.getValue(ClsProperties.IS_PARSE_REQ_JSON, ClsProperties.DEFAULT_IS_PARSE_REQ_JSON)) {
					String body = prop.readJsonFile(
							prop.getValue(ClsProperties.PATHFINPUT, ""),
							encoding,
							prop.getValue(ClsProperties.IS_PARSE_REQ_PRETTY, ClsProperties.DEFAULT_IS_PARSE_REQ_PRETTY),
							prop.getValue(ClsProperties.IS_JSON_CHAR_ESCAPES, ClsProperties.DEFAULT_IS_JSON_CHAR_ESCAPES),
							true,
							prop.getValue(ClsProperties.IS_LOG_PARSE_REQ_ERROR, ClsProperties.DEFAULT_IS_LOG_PARSE_REQ_ERROR)
						);
					if (prop.getValue(ClsProperties.IS_PARSE_ERROR, false)) {
						isOk = false;
					}
					if (isOk) {
						prop.setValue(ClsProperties.REQ_BODY, body);
					}
				} else {
					String body = prop.readFile(prop.getValue(ClsProperties.PATHFINPUT, ""), encoding);
					prop.setValue(ClsProperties.REQ_BODY, body);
				}
			}
		}

		if (!prop.getValue(ClsProperties.AUTH_USER_BASIC, "").isEmpty()) {
			prop.setValue(ClsProperties.IS_BASIC_AUTH, true);
		} else {
			prop.setValue(ClsProperties.IS_BASIC_AUTH, false);
		}

		String httpMethod = prop.getValue(ClsProperties.METHOD, "GET").toUpperCase();
		prop.setValue(ClsProperties.IS_REQUIRED_REQ_BODY, false);
		if ("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod)) {
			prop.setValue(ClsProperties.IS_REQUIRED_REQ_BODY, true);
		} else if ("DELETE".equals(httpMethod) || "HEAD".equals(httpMethod) || "TRACE".equals(httpMethod) || "OPTIONS".equals(httpMethod)) {
			// NONE
		} else {
			prop.setValue(ClsProperties.METHOD, "GET");
		}

		if (prop.getValue(ClsProperties.REQ_BODY, "").isEmpty()) {
			if (prop.getValue(ClsProperties.IS_REQUIRED_REQ_BODY, false)) {
				if (prop.getValue(ClsProperties.IS_ERR_IF_REQ_BODY_IS_EMPTY, false)) {
					logger.error("Request Body is empty : " + httpMethod);
					showUsage(ClsProperties.LVL_ERROR);
					return;
				}
			}
		} else {
			if (prop.getValue(ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE, ClsProperties.DEFAULT_REPLACE_REQ_BODY_AFTER_PARSE)) {
				String body = prop.replaceAllBody(prop.getValue(ClsProperties.REQ_BODY, ""), true);
				body = prop.replaceBody(body, true);
				prop.setValue(ClsProperties.REQ_BODY, body);
			}
		}

		String url = prop.replaceUrl(prop.getValue(ClsProperties.URL, ""));
		prop.setValue(ClsProperties.URL, url);

		prop.getReplaceReqBodyMap().clear();
		prop.getReplaceAllReqBodyMap().clear();
		prop.getReplaceUrlMap().clear();

		if (isOk && prop.getVerbose() > 2) {
			logger.info("################################################################################");
			logger.info("# 接続設定");
			logger.info("################################################################################");
			if (!prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT).isEmpty()) {
				logger.info("USERAGENT : " + prop.getValue(ClsProperties.USER_AGENT, ClsProperties.DEFAULT_USER_AGENT));
			}
			if (!prop.getValue(ClsProperties.AUTH_USER_BASIC, "").isEmpty()) {
				String[] userPass = prop.getValue(ClsProperties.AUTH_USER_BASIC, "").split(":");
				logger.info("AUTH      : USERNAME = " + userPass[0].trim() + " / PASSWORD = " + userPass[1].trim().replaceAll("[0-9aA-zZ_$#!]", "*"));
			}
			if (!prop.getValue(ClsProperties.PROXY, "").isEmpty()) {
				logger.info("PROXY     : " + prop.getValue(ClsProperties.PROXY, ""));
			}
			logger.info("INSECURE  : " + prop.getValue(ClsProperties.IS_INSECURE, false));
			logger.info("SSLVER    : " + prop.getValue(ClsProperties.PROTOCOL_VERSION, ClsProperties.DEFAULT_PROTOCOL_VERSION));
		}

		if (isOk) {
			httpClient = new ClsHttpClient(logger, prop);
			isOk = httpClient.init();
		}

		if (isOk) {
			if (prop.getValue(ClsProperties.VERBOSE, 0) > 2) {
				logger.info("################################################################################");
				logger.info("# 接続実行");
				logger.info("################################################################################");
				logger.info("METHOD    : " + prop.getValue(ClsProperties.METHOD, "GET"));
				logger.info("URL       : " + prop.getValue(ClsProperties.URL, ""));
				if (!prop.getValue(ClsProperties.IPADDR, "").isEmpty()) {
					logger.info("IP        : " + prop.getValue(ClsProperties.IPADDR, ""));
				}
				if (!prop.getValue(ClsProperties.REQ_BODY, "").isEmpty()) {
					logger.info("BODY      : \n" + prop.getValue(ClsProperties.REQ_BODY, ""));
				}
			}

			httpCode = httpClient.connect();
			String resBody;

			if (prop.getValue(ClsProperties.VERBOSE, 0) > 0) {
				if (prop.getValue(ClsProperties.VERBOSE, 0) > 2) {
					logger.info("################################################################################");
					logger.info("# レスポンス内容");
					logger.info("################################################################################");
				}
				System.out.println("> " + httpClient.getResVersion() + " " + httpCode + " " + httpClient.getReasonPhrase());
				if (!httpClient.getResponseHeaders().isEmpty()) {
					httpClient.getResponseHeaders().forEach(h -> System.out.println("> " + h));
					System.out.println(">");
				}
			}

			if (prop.getValue(ClsProperties.IS_PARSE_RES_JSON, ClsProperties.DEFAULT_IS_PARSE_RES_JSON)) {
				resBody = prop.parseJsonStr(
						httpClient.getResponseBody(),
						prop.getValue(ClsProperties.IS_PARSE_RES_PRETTY, ClsProperties.DEFAULT_IS_PARSE_RES_PRETTY),
						false,
						false,
						prop.getValue("IsLogParseResJsonError", ClsProperties.DEFAULT_IS_LOG_PARSE_RES_ERROR)
					);
				if (prop.getValue(ClsProperties.IS_PARSE_ERROR, false)) {
					isOk = false;
				}
			} else {
				resBody = httpClient.getResponseBody();
			}

			try {
				if (resBody != null && !resBody.isEmpty() && prop.getValue(ClsProperties.IS_CONVERT_UNICODE_TO_UTF8, false)) {
					resBody = prop.convertUnicode(resBody);
				}
			} catch (Exception e) {
				logger.error("EXCEPTION : " + e.getMessage());
			}

			if (prop.getValue(ClsProperties.REPLACE_RES_BODY_AFTER_PARSE, ClsProperties.DEFAULT_REPLACE_RES_BODY_AFTER_PARSE)) {
				resBody = prop.replaceAllBody(resBody, false);
				resBody = prop.replaceBody(resBody, false);
			}

			if (resBody != null && !resBody.isEmpty()) {
				if (!prop.getValue(ClsProperties.IS_SILENT, false)) {
					System.out.println(resBody.trim());
				}
			}

			if (prop.getValue(ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE, false)) {
				exitCode = httpCode;
			} else {
				if (httpCode < 0) {
					exitCode = ClsProperties.LVL_ERROR;
				} else if (ClsProperties.LVL_WARN == httpCode) {
					exitCode = ClsProperties.LVL_WARN;
				} else if (ClsProperties.LVL_ERROR == httpCode) {
					exitCode = ClsProperties.LVL_ERROR;
				} else if (0 < httpCode && httpCode < 200) {
					exitCode = 0;
				} else if (200 <= httpCode && httpCode < 400) {
					if (prop.getValue(ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX, false)) {
						exitCode = httpCode;
					} else {
						exitCode = 0;
					}
				} else if (400 <= httpCode) {
					exitCode = ClsProperties.LVL_ERROR;
				}
			}
			if (!isOk) {
				exitCode = ClsProperties.LVL_ERROR;
			}

			if (isOk) {
				if (!prop.getValue(ClsProperties.PATHFOUTPUT, "").isEmpty()) {
					if (!prop.writeFile(prop.getValue(ClsProperties.PATHFOUTPUT, ""), resBody, Charset.forName(prop.getValue(ClsProperties.OUT_FILE_ENCODING, ClsProperties.DEFAULT_STR_ENCODING)))) {
						exitCode = ClsProperties.LVL_ERROR;
					}
				}
			}
		}

		if (httpClient != null) {
			httpClient.clearResponse();
			httpClient.terminate();
		}

		terminate(exitCode);
	}

	/**
	 * プログラムの終了処理を実行し、設定に応じて JVM を終了します。
	 *
	 * @param exitCode 終了コード
	 *
	 * <pre>
	 * terminate(0);
	 * </pre>
	 */
	private void terminate(int exitCode) {
		if (exitCode == 10 && prop.getValue(ClsProperties.IS_EXIT_MINUS_ONE_IF_WARN, false)) {
			exitCode = -1;
		}
		if (exitCode == 20 && prop.getValue(ClsProperties.IS_EXIT_MINUS_ONE_IF_ERROR, false)) {
			exitCode = -1;
		}
		endTime = System.currentTimeMillis();
		double elapsed = (double) (endTime - startTime) / 1000.0;
		if (prop.getValue(ClsProperties.VERBOSE, 0) > 1) {
			System.out.println("===<<< [" + className + "] EXIT (" + exitCode + ") : " + prop.formatUnixTime(endTime, "yyyy/MM/dd HH:mm:ss") + " : " + elapsed + " sec>>>===");
		}
		if (isKillJvm) {
			System.exit(exitCode);
		}
	}

	/**
	 * コマンドラインの使用方法（Usage）および現在設定値を標準出力に出力します。
	 *
	 * @param exitCode Usage表示後の終了コード
	 *
	 * <pre>
	 * showUsage(ClsProperties.LVL_ERROR);
	 * </pre>
	 */
	private void showUsage(int exitCode) {
		System.out.println("");
		System.out.println("Usage:   java -jar RestClient.jar [option...]");
		System.out.println("");
		System.out.println("Basic options:");
		System.out.println("  -c path                       CONFIG FILE PATH : see --show-sample-config (現在値=" + prop.getValue(ClsProperties.PATHFCONF, "") + ")");
		System.out.println("  -i path                       UPLOAD JSON FILE PATH                       (現在値=" + prop.getValue(ClsProperties.PATHFINPUT, "") + ")");
		System.out.println("  -o path                       RESPONSE OUTPUT FILE PATH                   (現在値=" + prop.getValue(ClsProperties.PATHFOUTPUT, "") + ")");
		System.out.println("");
		System.out.println("Options for not use or override configuration file:");
		System.out.println("  --url url                     URL                                                      (現在値=" + prop.getValue(ClsProperties.URL, "") + ")");
		System.out.println("  --ip ipaddr                   IP ADDRESS                                               (現在値=" + prop.getValue(ClsProperties.IPADDR, "") + ")");
		System.out.println("  -x|--proxy url                PROXY SERVER ex.) http://user:pass@host:port             (現在値=" + prop.getValue(ClsProperties.PROXY, "") + ")");
		System.out.println("  -X|--request method           HTTP METHOD:GET|POST|PUT|PATCH|DELETE|HEAD|TRACE|OPTIONS (現在値=" + prop.getValue(ClsProperties.METHOD, "") + ")");
		if (multiValArgs.containsKey(ClsProperties.REQ_BODY)) {
			List<String> list = prop.getValue(ClsProperties.REQ_BODY, new ArrayList<>());
			for (String item : list) {
				System.out.println("  -d|--data str                 UPLOAD REQUEST BODY                                      (現在値=" + item + ")");
			}
		}
		if (multiValArgs.containsKey(ClsProperties.REQ_HEADERS)) {
			List<String> list = prop.getValue(ClsProperties.REQ_HEADERS, new ArrayList<>());
			for (String item : list) {
				System.out.println("  -H|-header \"str1,str2\"        HEADER                                                   (現在値=" + item + ")");
			}
		}
		System.out.println("  --preq                        PARSE JSON FLAG:REQUEST BODY                             (現在値=" + prop.getValue(ClsProperties.IS_PARSE_REQ_JSON, false) + ")");
		System.out.println("  --pres                        PARSE JSON FLAG:RESPONSE BODY                            (現在値=" + prop.getValue(ClsProperties.IS_PARSE_RES_JSON, false) + ")");
		System.out.println("  --timeout msec                TIMEOUT MSEC                                             (現在値=" + prop.getValue(ClsProperties.TIMEOUT_CONNECT, ClsProperties.DEFAULT_TIMEOUT_CONNECT) + ")");
		System.out.println("  -u|--user user:password       SERVER USER AND PASSWORD                                 (現在値=" + prop.getValue(ClsProperties.AUTH_USER_BASIC, "") + ")");
		System.out.println("  -k|--insecure                 ALLOW INSECURE SERVER CONNECTION                         (現在値=" + prop.getValue(ClsProperties.IS_INSECURE, false) + ")");
		System.out.println("  --ssl-ver csv                 TLSv1.1,TLSv1.2,TLSv1.3                                  (現在値=" + prop.getValue(ClsProperties.PROTOCOL_VERSION, ClsProperties.DEFAULT_PROTOCOL_VERSION) + ")");
		System.out.println("  -v|-vv|-vvv                   VERBOSE                                                  (現在値=" + prop.getValue(ClsProperties.VERBOSE, 0) + ")");
		System.out.println("  --silent                      DONOT SHOW RES BODY                                      (現在値=" + prop.getValue(ClsProperties.IS_SILENT, false) + ")");
		System.out.println("  --trace                       SHOW TRACE LOG                                           (現在値=" + prop.getValue(ClsProperties.IS_TRACE_LOG, 0) + ")");
		System.out.println("  --dryrun                      DRYRUN                                                   (現在値=" + prop.getValue(ClsProperties.IS_DRYRUN, false) + ")");
		System.out.println("  --exit default|http|normal    EXIT CODE 0/10/20|HTTP-STATUS-CODE|0/HTTP-STATUS-CODE    (現在値=" + prop.getValue(ClsProperties.EXIT_TYPE, "default") + ")");
		System.out.println("  -A|--user-agent ua            USER-AGENT ex.)chrome|firefox|edge|...                   (現在値=" + prop.getValue(ClsProperties.USER_AGENT, "") + ")");
		System.out.println("  --options csv                 KEY1=VAL1,KEY1=VAL2 (ex." + ClsProperties.TIMEOUT_CONNECT + "=1000," + ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE + "=true)");
		System.out.println("  --option-delimiter char       options csv delimiter (default = [,|]");
		System.out.println("  --json-escapes                JSON CHARACTOR ESCAPES FLAG AT JSON PARSE                (現在値=" + prop.getValue(ClsProperties.IS_JSON_CHAR_ESCAPES, false) + ")");
		System.out.println("  --unicode-escapes             ENCODE UNICODE ESCAPES FLAG                              (現在値=" + prop.getValue(ClsProperties.IS_ENCODE_UNICODE_ESCAPES, false) + ")");
		System.out.println("  --unicode-to-utf8             CONVERT RESPONCE BODY FROM UNICODE ESCAPES TO UTF-8      (現在値=" + prop.getValue(ClsProperties.IS_CONVERT_UNICODE_TO_UTF8, false) + ")");
		System.out.println("  --node                        RESPONCE EXTRACT JSON NODE KEY AT JSON PARSE             (現在値=" + prop.getValue(ClsProperties.EXTRACTION_KEY_CSV, "") + ")");
		System.out.println("");
		System.out.println("Replace options:");
		System.out.println("  --replace-url a=b,c=d        REPLACE URL         (現在値=" + prop.getValue(ClsProperties.REPLACE_URL_RULES, "") + ")");
		System.out.println("  --replace-reqbody a=b,c=d    REPLACE REQ BODY    (現在値=" + prop.getValue(ClsProperties.REPLACE_REQ_BODY_RULES, "") + ")");
		System.out.println("  --replace-resbody a=b,c=d    REPLACE RES BODY    (現在値=" + prop.getValue(ClsProperties.REPLACE_RES_BODY_RULES, "") + ")");
		System.out.println("  --replaceall-reqbody a=b,c=d REPLACEALL REQ BODY (現在値=" + prop.getValue(ClsProperties.REPLACEALL_REQ_BODY_RULES, "") + ")");
		System.out.println("  --replaceall-resbody a=b,c=d REPLACEALL RES BODY (現在値=" + prop.getValue(ClsProperties.REPLACEALL_RES_BODY_RULES, "") + ")");
		System.out.println("  --replace-req before|after   REPLCAE REQ RULES BEFORE OR AFTER JSON PARSE (現在値=" + (prop.getValue(ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE, false) ? "before" : "after") + ")");
		System.out.println("  --replace-res before|after   REPLCAE RES RULES BEFORE OR AFTER JSON PARSE (現在値=" + (prop.getValue(ClsProperties.REPLACE_RES_BODY_BEFORE_PARSE, false) ? "before" : "after") + ")");
		System.out.println("");
		System.out.println("Help options:");
		System.out.println("  -h                           SHOW THIS HELP MESSAGE (現在値=" + (prop.getUsageFlag() == ClsProperties.USAGE_USAGE ? "true" : "false") + ")");
		System.out.println("  --show-sample-config         SHOW SAMPLE CONFIG     (現在値=" + (prop.getUsageFlag() == ClsProperties.USAGE_SHOW_SAMPLE_CONFIG ? "true" : "false") + ")");
		System.out.println("");
		System.out.println("exit code: NORMAL=0 / WARN=10 / ERROR=20 or HTTPCODE(200以外)");
		System.out.println("");
		terminate(exitCode);
	}

	/**
	 * 設定ファイルのサンプル定義を標準出力に出力します。
	 *
	 * <pre>
	 * showSampleConfig();
	 * </pre>
	 */
	private void showSampleConfig() {
		System.out.println("################################################################################");
		System.out.println("# 接続先");
		System.out.println("################################################################################");
		System.out.println("# 【必須】URL");
		System.out.println("# ---> 引数：--url url | --options " + ClsProperties.URL + "=url");
		System.out.println("" + ClsProperties.URL + " = https://localhost/cgi-bin/action.cgi");
		System.out.println("# 接続先IPアドレス：hosts＆DNS参照する場合はコメント化");
		System.out.println("# ---> 引数：--ip ipaddr | --options " + ClsProperties.IPADDR + "=ipaddr");
		System.out.println("" + ClsProperties.IPADDR + " = 127.0.0.1");
		System.out.println("# HTTP METHOD (初期値=POST)：GET | POST | PUT | DELETE | HEAD | OPTIONS | TRACE");
		System.out.println("# ---> 引数：-X|--request method | --options " + ClsProperties.METHOD + "=method");
		System.out.println("" + ClsProperties.METHOD + " = POST");
		System.out.println("################################################################################");
		System.out.println("# リクエストヘッダー：列挙して下さい");
		System.out.println("################################################################################");
		System.out.println("# ---> 引数：-H|-header \\\"str1,str2\\\" | --options \"" + ClsProperties.REQ_HEADERS + "=header1,header2\"");
		System.out.println("" + ClsProperties.REQUEST_HEADER_LIST_KEY + " = Content-Type: application/json; charset=UTF-8");
		System.out.println("" + ClsProperties.REQUEST_HEADER_LIST_KEY + " = api-key: XXXXXXXXXXXXXX");
		System.out.println("################################################################################");
		System.out.println("# リクエストBODY");
		System.out.println("################################################################################");
		System.out.println("# 送信データの文字列");
		System.out.println("# ---> 引数：-d|--data str | --options " + ClsProperties.REQ_BODY + "=str");
		System.out.println("" + ClsProperties.REQ_BODY + " = str");
		System.out.println("# 送信データのファイルパス");
		System.out.println("# ---> 引数：-i path | --options " + ClsProperties.PATHFINPUT + "=path");
		System.out.println("" + ClsProperties.PATHFINPUT + " = path");
		System.out.println("# 送信データのJSONパーサー適用フラグ (初期値=" + ClsProperties.DEFAULT_IS_PARSE_REQ_JSON + ")");
		System.out.println("# ---> 引数：--preq | --options " + ClsProperties.IS_PARSE_REQ_JSON + "=true");
		System.out.println("" + ClsProperties.IS_PARSE_REQ_JSON + " = " + ClsProperties.DEFAULT_IS_PARSE_REQ_JSON);
		System.out.println("# JSONパーサー時のPretty書式変換フラグ (初期値=" + ClsProperties.DEFAULT_IS_PARSE_REQ_PRETTY + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_PARSE_REQ_PRETTY + "=true");
		System.out.println("" + ClsProperties.IS_PARSE_REQ_PRETTY + " = " + ClsProperties.DEFAULT_IS_PARSE_REQ_PRETTY);
		System.out.println("# JSONパース時の特殊文字エスケープフラグ (初期値=" + ClsProperties.DEFAULT_IS_JSON_CHAR_ESCAPES + ")");
		System.out.println("# ---> 引数：--preq --json-escapes | --options " + ClsProperties.IS_PARSE_RES_JSON + "=true," + ClsProperties.IS_JSON_CHAR_ESCAPES + "=true");
		System.out.println("" + ClsProperties.IS_JSON_CHAR_ESCAPES + " = false");
		System.out.println("# JSONパースエラー時のエラーメッセージ出力フラグ (初期値=" + ClsProperties.DEFAULT_IS_LOG_PARSE_RES_ERROR + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_LOG_PARSE_REQ_ERROR + "=true");
		System.out.println("" + ClsProperties.IS_LOG_PARSE_REQ_ERROR + " = " + ClsProperties.DEFAULT_IS_LOG_PARSE_RES_ERROR);
		System.out.println("# リクエストBODYのUnicodeエスケープフラグ (初期値=false)");
		System.out.println("# ---> 引数：--unicode-escapes | --options " + ClsProperties.IS_ENCODE_UNICODE_ESCAPES + "=true");
		System.out.println("" + ClsProperties.IS_ENCODE_UNICODE_ESCAPES + " = false");
		System.out.println("# 引数「-i path」で指定した入力ファイルの文字コード (初期値=" + ClsProperties.DEFAULT_STR_ENCODING + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.REQ_FILE_ENCODING + "=enc");
		System.out.println("" + ClsProperties.REQ_FILE_ENCODING + " = AUTO");
		System.out.println("# リクエストBODYが空の場合に異常終了するか否か (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_ERR_IF_REQ_BODY_IS_EMPTY + "=true");
		System.out.println("" + ClsProperties.IS_ERR_IF_REQ_BODY_IS_EMPTY + " = false");
		System.out.println("# REQUENT BODYの置換カンマ区切りリスト：replaceall()版");
		System.out.println("# ---> 引数：--replaceall-reqbody csv | --options " + ClsProperties.REPLACEALL_REQ_BODY_RULES + "=csv");
		System.out.println("" + ClsProperties.REPLACEALL_REQ_BODY_RULES + " = __RCRLF__=__EMPTY__,__RCR__=__EMPTY__,__RLF__=__EMPTY__,__RTAB__=__EMPTY__,__RSPACE__+=__SPACE__");
		System.out.println("# REQUENT BODYの置換カンマ区切りリスト：replace()版");
		System.out.println("# ---> 引数：--replace-reqbody csv | --options " + ClsProperties.REPLACE_REQ_BODY_RULES + "=csv");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_RULES + " = A=B,C=D");
		System.out.println("# JSONパース事前置換フラグ (初期値=" + ClsProperties.DEFAULT_REPLACE_REQ_BODY_BEFORE_PARSE + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE + "=true");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE + " = true");
		System.out.println("# JSONパース事後置換フラグ (初期値=" + ClsProperties.DEFAULT_REPLACE_REQ_BODY_AFTER_PARSE + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE + "=true");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE + " = false");
		System.out.println("################################################################################");
		System.out.println("# レスポンスBODY");
		System.out.println("################################################################################");
		System.out.println("# JSONパーサーフラグ (初期値=" + ClsProperties.DEFAULT_IS_PARSE_RES_JSON + ")");
		System.out.println("# ---> 引数：--pres | --options " + ClsProperties.IS_PARSE_RES_JSON + "=true");
		System.out.println("" + ClsProperties.IS_PARSE_RES_JSON + " = false");
		System.out.println("# JSONパーサー時のPretty書式変換フラグ (初期値=" + ClsProperties.DEFAULT_IS_PARSE_RES_PRETTY + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_PARSE_RES_PRETTY + "=true");
		System.out.println("" + ClsProperties.IS_PARSE_RES_PRETTY + " = false");
		System.out.println("# JSONパースエラー時のエラーメッセージ出力フラグ (初期値=" + ClsProperties.DEFAULT_IS_LOG_PARSE_RES_ERROR + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_LOG_PARSE_RES_ERROR + "=true");
		System.out.println("" + ClsProperties.IS_LOG_PARSE_RES_ERROR + " = true");
		System.out.println("# レスポンスBODYのUnicodeエスケープ文字列からUTF-8への変換フラグ (初期値=false)");
		System.out.println("# ---> 引数：--unicode-to-utf8 | --options " + ClsProperties.IS_CONVERT_UNICODE_TO_UTF8 + "=true");
		System.out.println("" + ClsProperties.IS_CONVERT_UNICODE_TO_UTF8 + " = false");
		System.out.println("# レスポンスBODYの置換カンマ区切りリスト：replaceall()版");
		System.out.println("# ---> 引数：--replaceall-reqbody csv | --options " + ClsProperties.REPLACEALL_REQ_BODY_RULES + "=csv");
		System.out.println("" + ClsProperties.REPLACEALL_REQ_BODY_RULES + " = __RCRLF__=__EMPTY__,__RCR__=__EMPTY__,__RLF__=__EMPTY__,__RTAB__=__EMPTY__,__RSPACE__+=__SPACE__");
		System.out.println("# レスポンスBODYの置換カンマ区切りリスト：replace()版");
		System.out.println("# ---> 引数：--replace-reqbody csv | --options " + ClsProperties.REPLACE_REQ_BODY_RULES + "=csv");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_RULES + " = A=B,C=D");
		System.out.println("# JSONパース事前置換フラグ (初期値=" + ClsProperties.DEFAULT_REPLACE_RES_BODY_BEFORE_PARSE + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE + "=true");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_BEFORE_PARSE + " = true");
		System.out.println("# JSONパース事後置換フラグ (初期値=" + ClsProperties.DEFAULT_REPLACE_RES_BODY_AFTER_PARSE + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE + "=true");
		System.out.println("" + ClsProperties.REPLACE_REQ_BODY_AFTER_PARSE + " = false");
		System.out.println("# 出力ファイルパス");
		System.out.println("# ---> 引数：-o path | --options " + ClsProperties.PATHFOUTPUT + "=path");
		System.out.println("" + ClsProperties.PATHFOUTPUT + " = path");
		System.out.println("# 引数「-o path」で指定した出力ファイルの文字コード (初期値=" + ClsProperties.DEFAULT_STR_ENCODING + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.OUT_FILE_ENCODING + "=" + ClsProperties.DEFAULT_STR_ENCODING);
		System.out.println("" + ClsProperties.OUT_FILE_ENCODING + " = " + ClsProperties.DEFAULT_STR_ENCODING);
		System.out.println("# 値取得キー名");
		System.out.println("# ---> 引数：--node node | --options " + ClsProperties.EXTRACTION_KEY_CSV + "=/key1/key2");
		System.out.println("" + ClsProperties.EXTRACTION_KEY_CSV + " = \"\"");
		System.out.println("################################################################################");
		System.out.println("# HTTPクライアント機能：Java Apache HttpClient 5.2：PROXY");
		System.out.println("################################################################################");
		System.out.println("# プロキシ接続文字列");
		System.out.println("# ---> 引数：-x|--proxy url | --options " + ClsProperties.PROXY + "=proxy-url");
		System.out.println("" + ClsProperties.PROXY + " = http://username:password@fqdn:port");
		System.out.println("################################################################################");
		System.out.println("# HTTPクライアント機能：Java Apache HttpClient 5.2：タイムアウト(ミリ秒)");
		System.out.println("################################################################################");
		System.out.println("# ---> 引数：--options " + ClsProperties.TIMEOUT_CONNECT + "=msec");
		System.out.println("" + ClsProperties.TIMEOUT_CONNECT + " = " + ClsProperties.DEFAULT_TIMEOUT_CONNECT);
		System.out.println("# ---> 引数：--options " + ClsProperties.TIMEOUT_SOCKET + "=msec");
		System.out.println("" + ClsProperties.TIMEOUT_SOCKET + " = " + ClsProperties.DEFAULT_TIMEOUT_SOCKET);
		System.out.println("# ---> 引数：--options " + ClsProperties.TIMEOUT_REQ + "=msec");
		System.out.println("" + ClsProperties.TIMEOUT_REQ + " = " + ClsProperties.DEFAULT_TIMEOUT_REQ);
		System.out.println("# ---> 引数：--options " + ClsProperties.TIMEOUT_RES + "=msec");
		System.out.println("" + ClsProperties.TIMEOUT_RES + " = " + ClsProperties.DEFAULT_TIMEOUT_RES);
		System.out.println("################################################################################");
		System.out.println("HTTPクライアント機能：その他");
		System.out.println("################################################################################");
		System.out.println("# HTTP REQUEST ENCODING (初期値=UTF-8)");
		System.out.println("# ---> 引数：--options " + ClsProperties.HTTP_REQ_ENCODING + "=enc");
		System.out.println("" + ClsProperties.HTTP_REQ_ENCODING + " = UTF-8");
		System.out.println("# HTTP RESPONCE ENCODING (初期値=UTF-8)");
		System.out.println("# ---> 引数：--options " + ClsProperties.HTTP_RES_ENCODING + "=enc");
		System.out.println("" + ClsProperties.HTTP_RES_ENCODING + " = UTF-8");
		System.out.println("# SSL証明書検証無効化フラグ (初期値=false)");
		System.out.println("# ---> 引数：-k|--insecure " + ClsProperties.IS_INSECURE + "=true");
		System.out.println("" + ClsProperties.IS_INSECURE + " = false");
		System.out.println("# TLSバージョン");
		System.out.println("# ---> 引数：--ssl-ver csv " + ClsProperties.IS_INSECURE + "=csv");
		System.out.println("" + ClsProperties.PROTOCOL_VERSION + " = TLSv1.2,TLSv1.3");
		System.out.println("# USER-AGENT");
		System.out.println("# ---> 引数：-A|--user-agent ua " + ClsProperties.USER_AGENT + "=ua");
		System.out.println("" + ClsProperties.USER_AGENT + " = " + ClsProperties.USER_AGENT_CHROME);
		System.out.println("# BASIC認証ユーザ：USER:PASS");
		System.out.println("# ---> 引数：-u|--user user:password " + ClsProperties.AUTH_USER_BASIC + "=user:pass");
		System.out.println("" + ClsProperties.AUTH_USER_BASIC + " = user:pass");
		System.out.println("# URLの置換カンマ区切りリスト");
		System.out.println("# ---> 引数：--replace-url csv " + ClsProperties.REPLACE_URL_RULES + "=csv");
		System.out.println("" + ClsProperties.REPLACE_URL_RULES + " = a=b,c=d");
		System.out.println("################################################################################");
		System.out.println("デバッグ：HTTP接続確認");
		System.out.println("################################################################################");
		System.out.println("# HTTP非接続確認モード (初期値=false)");
		System.out.println("# ---> 引数：--dryrun | --options " + ClsProperties.IS_DRYRUN + "=true");
		System.out.println("" + ClsProperties.IS_DRYRUN + " = false");
		System.out.println("# ---> 引数：--options " + ClsProperties.DRYRUN_HTTP_CODE + "=http-status-code");
		System.out.println("" + ClsProperties.DRYRUN_HTTP_CODE + " = 200");
		System.out.println("# ---> 引数：--options " + ClsProperties.DRYRUN_HTTP_BODY + "=str");
		System.out.println("" + ClsProperties.DRYRUN_HTTP_BODY + " = {\"TicketID\":\"72905\",\"eventid\":\"1677942121375\",\"message\":null}");
		System.out.println("# ---> 引数：--options " + ClsProperties.DRYRUN_ELAPS_MSEC + "=msec");
		System.out.println("" + ClsProperties.DRYRUN_ELAPS_MSEC + " = 500");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_TRACE_LOG + "=num");
		System.out.println("" + ClsProperties.IS_TRACE_LOG + " = 0");
		System.out.println("################################################################################");
		System.out.println("デバッグ・終了コード");
		System.out.println("################################################################################");
		System.out.println("# EXITCODEは常にHTTPCODEとする場合はtrueを指定 (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE + "=true");
		System.out.println("" + ClsProperties.IS_EXIT_CODE_IS_HTTP_CODE + " = false");
		System.out.println("# HTTPCODEが200の場合、EXITCODEを200にする場合はtrueを指定 (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX + "=true");
		System.out.println("" + ClsProperties.IS_EXIT_2XX_IF_CODE_IS_2XX + " = false");
		System.out.println("# 異常終了時に20ではなく-1を返却する場合はtrueを指定 (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_EXIT_MINUS_ONE_IF_ERROR + "=true");
		System.out.println("" + ClsProperties.IS_EXIT_MINUS_ONE_IF_ERROR + " = false");
		System.out.println("# 警告時に20ではなく-1を返却する場合はtrueを指定 (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_EXIT_MINUS_ONE_IF_WARN + "=true");
		System.out.println("" + ClsProperties.IS_EXIT_MINUS_ONE_IF_WARN + " = false");
		System.out.println("# 設定項目がこのファイルに記載のない場合に警告出力フラグ (初期値=false)");
		System.out.println("# ---> 引数：--options " + ClsProperties.IS_WARN_IF_KEY_NOT_FOUND + "=true");
		System.out.println("" + ClsProperties.IS_WARN_IF_KEY_NOT_FOUND + " = false");
		System.out.println("################################################################################");
		System.out.println("デバッグ・その他");
		System.out.println("################################################################################");
		System.out.println("# 冗長ログ出力レベル：引数 -v | -vv | -vvv | -v 数字 (初期値=0)");
		System.out.println("# ---> 引数：-v|--vv num | --options " + ClsProperties.VERBOSE + "=num");
		System.out.println("" + ClsProperties.VERBOSE + " = 2");
		System.out.println("# 冗長ログ出力レベル：引数 --trace");
		System.out.println("# ---> 引数：-v|--vv num | --options " + ClsProperties.IS_TRACE_LOG + "=0");
		System.out.println("" + ClsProperties.IS_TRACE_LOG + " = 0");
		System.out.println("# 冗長ログ出力レベル：引数 --trace");
		System.out.println("# ---> 引数：--silent | --options " + ClsProperties.IS_SILENT + "=true");
		System.out.println("" + ClsProperties.IS_SILENT + " = false");
		System.out.println("# このファイルの文字コード (初期値=" + ClsProperties.DEFAULT_STR_ENCODING + ")");
		System.out.println("# ---> 引数：--options " + ClsProperties.CNF_FILE_ENCODING + "=AUTO");
		System.out.println("################################################################################");
		terminate(0);
	}
}
