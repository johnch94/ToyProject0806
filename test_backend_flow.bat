@echo off
chcp 65001 >nul
echo ================================================
echo 🧪 Riot API 백엔드 테스트
echo ================================================
echo.

set BASE_URL=http://localhost:8081

echo ⏳ 서버 상태 확인 중...
curl -s %BASE_URL%/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 서버가 실행되지 않았습니다!
    echo 💡 먼저 start_server.bat을 실행하세요
    echo.
    pause
    exit /b 1
)

echo ✅ 서버 실행 중! (포트: 8081)
echo.

echo 📋 테스트 시나리오:
echo 1. 🏥 헬스체크
echo 2. 🎮 플레이어 검색 (Faker)
echo 3. 🎯 간단한 플레이어 정보
echo 4. 📊 API 응답 시간 측정
echo.

echo ==========================================
echo 🏥 테스트 1: 헬스체크
echo ==========================================
curl -w "\n⏱️ 응답시간: %%{time_total}초 | 상태코드: %%{http_code}\n" %BASE_URL%/actuator/health 2>nul
echo.

echo ==========================================
echo 🎮 테스트 2: 플레이어 검색 (Faker)
echo ==========================================
echo 요청: GET %BASE_URL%/api/riot/player?gameName=Faker&tagLine=KR1
echo.
curl -X GET "%BASE_URL%/api/riot/player?gameName=Faker&tagLine=KR1" ^
     -H "Content-Type: application/json" ^
     -w "\n⏱️ 응답시간: %%{time_total}초 | 상태코드: %%{http_code}\n" ^
     2>nul
echo.
echo.

echo ==========================================
echo 🎯 테스트 3: 간단한 플레이어 정보
echo ==========================================
echo 요청: GET %BASE_URL%/api/riot/player/simple?gameName=Hide on bush&tagLine=KR1
echo.
curl -X GET "%BASE_URL%/api/riot/player/simple?gameName=Hide on bush&tagLine=KR1" ^
     -H "Content-Type: application/json" ^
     -w "\n⏱️ 응답시간: %%{time_total}초 | 상태코드: %%{http_code}\n" ^
     2>nul
echo.
echo.

echo ==========================================
echo 📊 테스트 4: 존재하지 않는 플레이어 (에러 처리)
echo ==========================================
echo 요청: GET %BASE_URL%/api/riot/player?gameName=NonExistentPlayer&tagLine=TEST
echo.
curl -X GET "%BASE_URL%/api/riot/player?gameName=NonExistentPlayer&tagLine=TEST" ^
     -H "Content-Type: application/json" ^
     -w "\n⏱️ 응답시간: %%{time_total}초 | 상태코드: %%{http_code}\n" ^
     2>nul
echo.
echo.

echo ✅ 백엔드 테스트 완료!
echo.
echo 📝 확인사항:
echo   - ✅ JSON 응답 형식 (ApiResponse 래퍼)
echo   - ✅ 에러 처리 (404, 500 등)
echo   - ✅ 로그 출력 (서버 콘솔 확인)
echo   - ✅ 응답 시간 측정
echo.
echo 🌐 추가 테스트:
echo   - Swagger UI: %BASE_URL%/swagger-ui.html
echo   - H2 DB 콘솔: %BASE_URL%/h2-console
echo.

pause
