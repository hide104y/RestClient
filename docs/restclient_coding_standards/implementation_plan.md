# 実装計画: RestClient クラス名の大文字化（PascalCase適用）

## 1. 概要
「クラス名は単語の先頭を大文字にする」というコーディング規約に基づき、`cls` から始まっていたクラス名を `Cls`（PascalCase / UpperCamelCase）に変更し、ファイル名およびプロジェクト内の全参照箇所を更新します。

## 2. 変更対象クラス・ファイル一覧

| 変更前（クラス名 / ファイル） | 変更後（クラス名 / ファイル） |
| :--- | :--- |
| `clsCmnArg` / `src/main/java/tool/clsCmnArg.java` | `ClsCmnArg` / `src/main/java/tool/ClsCmnArg.java` |
| `clsHttpClient` / `src/main/java/tool/clsHttpClient.java` | `ClsHttpClient` / `src/main/java/tool/ClsHttpClient.java` |
| `clsProperties` / `src/main/java/tool/clsProperties.java` | `ClsProperties` / `src/main/java/tool/ClsProperties.java` |
| `clsCmnArgTest` / `src/test/java/tool/clsCmnArgTest.java` | `ClsCmnArgTest` / `src/test/java/tool/ClsCmnArgTest.java` |
| `clsHttpClientTest` / `src/test/java/tool/clsHttpClientTest.java` | `ClsHttpClientTest` / `src/test/java/tool/ClsHttpClientTest.java` |
| `clsPropertiesTest` / `src/test/java/tool/clsPropertiesTest.java` | `ClsPropertiesTest` / `src/test/java/tool/ClsPropertiesTest.java` |
| `RestClient.java`, `RestClientTest.java` | 内部のクラス参照（`ClsProperties`, `ClsHttpClient`, `ClsCmnArg` 等）を更新 |

## 3. 手順
1. `src/main/java/tool/ClsCmnArg.java` を作成し、旧 `clsCmnArg.java` を削除。
2. `src/main/java/tool/ClsHttpClient.java` を作成し、旧 `clsHttpClient.java` を削除。
3. `src/main/java/tool/ClsProperties.java` を作成し、旧 `clsProperties.java` を削除。
4. `src/main/java/tool/RestClient.java` を更新。
5. `src/test/java/tool/ClsCmnArgTest.java` を作成し、旧 `clsCmnArgTest.java` を削除。
6. `src/test/java/tool/ClsHttpClientTest.java` を作成し、旧 `clsHttpClientTest.java` を削除。
7. `src/test/java/tool/ClsPropertiesTest.java` を作成し、旧 `clsPropertiesTest.java` を削除。
8. `src/test/java/tool/RestClientTest.java` を確認・更新。
9. `mvn clean test` および `mvn package` で検証。
