# Báo cáo kiểm tra Part 3–4

**Ngày:** 2026-09-30  
**Workspace:** MSS301 / slot7

## Kết quả đã xác nhận

| Hạng mục                 | Kết quả | Bằng chứng                                                                                                                          |
| ------------------------ | ------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| Build API Gateway        | Đạt     | Người dùng xác nhận lệnh `mvn -f .\api-gateway\pom.xml -DskipTests package` chạy thành công. Đây là build/package, không chạy test. |
| MySQL cho Keycloak       | Đạt     | Container `keycloak-mysql` ở trạng thái `Up (healthy)`.                                                                             |
| Keycloak 24.0.1          | Đạt     | Container `keycloak` ở trạng thái `Up`, publish `8181:8080`; log xác nhận `Keycloak 24.0.1 ... started`.                            |
| Khởi tạo schema Keycloak | Đạt     | Log Liquibase ghi nhận 121 change sets được áp dụng.                                                                                |
| Realm ứng dụng           | Đạt     | Người dùng xác nhận đã tạo realm `spring-microservices-realm` trong Admin Console.                                                  |

## Test chưa xác nhận

| Hạng mục                                            | Trạng thái                                                         |
| --------------------------------------------------- | ------------------------------------------------------------------ |
| Order Service + OpenFeign thủ công (T3)             | Chưa chạy/xác nhận.                                                |
| Order Service + WireMock (T4, 2 integration tests)  | Test source đã có; chưa chạy.                                      |
| API Gateway routing (T5)                            | Chưa chạy/xác nhận.                                                |
| Keycloak client, lấy token và discovery (T6.2–T6.6) | Chưa xác nhận client hoặc lấy token thành công.                    |
| Gateway security và end-to-end (T7–T8)              | Chưa chạy/xác nhận.                                                |
| Gateway automated tests (T9)                        | Chưa chạy; `ApiGatewaySecurityTests.java` chưa có trong workspace. |

## Kết luận

Đã xác nhận build/package của API Gateway và khởi động thành công hạ tầng Keycloak/MySQL. Chưa có kết quả test chức năng hoặc integration test để kết luận Part 3–4 đã pass end-to-end.
