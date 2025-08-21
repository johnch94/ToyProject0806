@echo off
echo === 회원관리 API 테스트 시작 ===

set BASE_URL=http://localhost:8081

echo.
echo 1. 헬스 체크
curl -s "%BASE_URL%/api/auth/health"

echo.
echo.
echo 2. 회원가입 테스트
curl -s -X POST "%BASE_URL%/api/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser001\", \"password\": \"Test1234!\", \"email\": \"test001@example.com\", \"role\": \"USER\"}"

echo.
echo.
echo 3. 로그인 테스트
curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"testuser001\", \"password\": \"Test1234!\"}"

echo.
echo.
echo 4. 중복 확인 테스트
curl -s "%BASE_URL%/api/auth/check/username/testuser001"
echo.
curl -s "%BASE_URL%/api/auth/check/email/test001@example.com"

echo.
echo.
echo 5. 게시글 작성 테스트 (authorId=1 가정)
curl -s -X POST "%BASE_URL%/api/boards?authorId=1" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\": \"API 테스트 게시글\", \"content\": \"회원가입 후 자동 생성된 게시글입니다.\"}"

echo.
echo.
echo === 테스트 완료 ===
pause