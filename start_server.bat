@echo off
chcp 65001 >nul
echo ================================================
echo 🚀 Spring Boot 서버 실행 중...
echo ================================================
echo.

cd C:\spring\toyproject\ToyProject0806\ToyProject0806

echo 📋 현재 디렉토리: %cd%
echo.

echo ⚙️ Gradle 빌드 확인 중...
call gradlew clean build -x test
echo.

if %errorlevel% neq 0 (
    echo ❌ 빌드 실패! 오류를 확인하세요.
    pause
    exit /b 1
)

echo ✅ 빌드 성공!
echo.

echo 🌟 서버 실행 중... (포트: 8081)
echo 📝 로그에서 "Started DemoApplication" 메시지를 확인하세요
echo 🌐 API 테스트: http://localhost:8081/api/riot/player?gameName=Faker&tagLine=KR1
echo 🗄️ H2 DB 콘솔: http://localhost:8081/h2-console
echo 📚 Swagger UI: http://localhost:8081/swagger-ui.html
echo.
echo ⚠️ 서버 종료: Ctrl+C
echo.

call gradlew bootRun --args="--spring.profiles.active=test"
