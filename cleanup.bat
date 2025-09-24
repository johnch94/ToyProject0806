@echo off
chcp 65001 >nul
echo ================================================
echo 🧹 ToyProject 불필요한 파일 정리
echo ================================================
echo.

echo 📋 정리할 파일들:
echo   - backup_to_delete 폴더 (백업 파일들)
echo   - simple_test.bat (단순 테스트)
echo   - test_api.bat (기본 테스트)
echo   - test_api.sh (Linux용 테스트)
echo   - test_backend_flow.bat (백엔드 플로우 테스트)
echo   - test_riot_api_unified.bat (통합 테스트)
echo.

echo 📦 유지할 파일들:
echo   - start_server.bat (서버 실행)
echo   - test-riot-api.http (IntelliJ/VSCode HTTP 테스트)
echo   - 프로젝트 핵심 파일들
echo.

set /p choice="정리를 진행하시겠습니까? (y/n): "
if /i "%choice%"=="y" (
    echo.
    echo 🗑️ 파일 정리 중...
    
    if exist "backup_to_delete" (
        rmdir /s /q "backup_to_delete"
        echo ✅ backup_to_delete 폴더 삭제 완료
    )
    
    if exist "simple_test.bat" (
        del "simple_test.bat"
        echo ✅ simple_test.bat 삭제 완료
    )
    
    if exist "test_api.bat" (
        del "test_api.bat"
        echo ✅ test_api.bat 삭제 완료
    )
    
    if exist "test_api.sh" (
        del "test_api.sh"
        echo ✅ test_api.sh 삭제 완료
    )
    
    if exist "test_backend_flow.bat" (
        del "test_backend_flow.bat"
        echo ✅ test_backend_flow.bat 삭제 완료
    )
    
    if exist "test_riot_api_unified.bat" (
        del "test_riot_api_unified.bat"
        echo ✅ test_riot_api_unified.bat 삭제 완료
    )
    
    echo.
    echo 🎉 정리 완료!
    echo.
    echo 📂 남은 주요 파일들:
    echo   - start_server.bat (서버 시작)
    echo   - test-riot-api.http (API 테스트)
    echo   - src/ (소스코드)
    echo   - build.gradle (빌드 설정)
    echo.
    
) else (
    echo 정리를 취소했습니다.
)

echo.
pause
