@echo off
chcp 65001 >nul
echo ================================================
echo 🎮 Riot API 통합 테스트 (간소화 버전)
echo ================================================
echo.

set BASE_URL=http://localhost:8080/api/riot

echo 📋 테스트할 API 목록:
echo 1. GET %BASE_URL%/player ^(플레이어 종합 정보^)
echo 2. GET %BASE_URL%/player/simple ^(간단한 플레이어 정보^)
echo 3. GET %BASE_URL%/matches ^(최근 경기 목록^)
echo 4. GET %BASE_URL%/match/{matchId} ^(경기 상세^)
echo.

echo ⏳ 서버 연결 확인 중...
curl -s %BASE_URL%/player?gameName=Faker&tagLine=KR1 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 서버가 실행되지 않았습니다. 먼저 서버를 시작하세요:
    echo    ./gradlew bootRun
    echo.
    pause
    exit /b 1
)

echo ✅ 서버 연결 성공!
echo.

echo 🎯 테스트 1: 플레이어 종합 정보 조회
echo URL: %BASE_URL%/player?gameName=Faker&tagLine=KR1
echo.
curl -X GET "%BASE_URL%/player?gameName=Faker&tagLine=KR1" ^
     -H "Content-Type: application/json" ^
     -w "\n상태코드: %%{http_code}\n응답시간: %%{time_total}초\n" ^
     2>nul | jq .
echo.
echo ================================
echo.

echo 🎯 테스트 2: 간단한 플레이어 정보
echo URL: %BASE_URL%/player/simple?gameName=Hide on bush&tagLine=KR1
echo.
curl -X GET "%BASE_URL%/player/simple?gameName=Hide on bush&tagLine=KR1" ^
     -H "Content-Type: application/json" ^
     -w "\n상태코드: %%{http_code}\n응답시간: %%{time_total}초\n" ^
     2>nul | jq .
echo.
echo ================================
echo.

echo 🎯 테스트 3: 최근 경기 목록 (PUUID 필요)
echo 참고: 실제 PUUID는 위 응답에서 확인하세요
echo URL: %BASE_URL%/matches?puuid=실제PUUID&count=3
echo.
echo "💡 PUUID 예시만 표시 (실제 테스트 시 위 응답의 PUUID 사용)"
echo.

echo ✅ 통합 완료! 변경사항:
echo.
echo 🗑️ 제거된 파일:
echo   - RiotController.java ^(중복^)
echo   - RiotService.java ^(중복^)
echo   - ChampionMasteryResponse.java ^(복잡함^)
echo   - RiorResponse.java ^(오타/중복^)
echo.
echo ✨ 남은 핵심 기능:
echo   - 플레이어 검색 ^(MVP^)
echo   - 랭크 조회 ^(필수^)
echo   - 경기 내역 ^(추가 가치^)
echo.
echo 📚 학습 완료된 부분:
echo   - DTO 패턴 적용
echo   - RESTful API 설계
echo   - 예외 처리 패턴
echo   - 외부 API 연동 방법
echo.

pause
