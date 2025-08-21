@echo off
chcp 65001 > nul
echo === API Test Start ===

set BASE_URL=http://localhost:8081

echo.
echo 1. Health Check
curl -s "%BASE_URL%/api/auth/health"

echo.
echo.
echo 2. User Signup Test
curl -s -X POST "%BASE_URL%/api/auth/signup" -H "Content-Type: application/json" -d "{\"username\": \"testuser001\", \"password\": \"Test1234!\", \"email\": \"test001@example.com\", \"role\": \"USER\"}"

echo.
echo.
echo 3. User Login Test  
curl -s -X POST "%BASE_URL%/api/auth/login" -H "Content-Type: application/json" -d "{\"username\": \"testuser001\", \"password\": \"Test1234!\"}"

echo.
echo.
echo 4. Check Username
curl -s "%BASE_URL%/api/auth/check/username/testuser001"

echo.
echo.
echo 5. Check Email
curl -s "%BASE_URL%/api/auth/check/email/test001@example.com"

echo.
echo.
echo 6. Create Board Test
curl -s -X POST "%BASE_URL%/api/boards?authorId=1" -H "Content-Type: application/json" -d "{\"title\": \"Test Board\", \"content\": \"This is a test board created by API.\"}"

echo.
echo.
echo === Test Complete ===
pause