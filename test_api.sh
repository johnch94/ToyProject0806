#!/bin/bash

# 회원관리 API 테스트 스크립트
echo "=== 회원관리 API 테스트 시작 ==="

BASE_URL="http://localhost:8080"

echo ""
echo "1. 헬스 체크"
curl -s "$BASE_URL/api/auth/health" | jq '.'

echo ""
echo "2. 회원가입 테스트"
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser001",
    "password": "Test1234!",
    "email": "test001@example.com",
    "role": "USER"
  }')
echo $SIGNUP_RESPONSE | jq '.'

# 사용자 ID 추출
USER_ID=$(echo $SIGNUP_RESPONSE | jq -r '.data.userId')
echo "생성된 사용자 ID: $USER_ID"

echo ""
echo "3. 로그인 테스트"
curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser001",
    "password": "Test1234!"
  }' | jq '.'

echo ""
echo "4. 중복 확인 테스트"
curl -s "$BASE_URL/api/auth/check/username/testuser001" | jq '.'
curl -s "$BASE_URL/api/auth/check/email/test001@example.com" | jq '.'

echo ""
echo "5. 게시글 작성 테스트 (User 연동)"
if [ "$USER_ID" != "null" ]; then
  curl -s -X POST "$BASE_URL/api/boards?authorId=$USER_ID" \
    -H "Content-Type: application/json" \
    -d '{
      "title": "API 테스트 게시글",
      "content": "회원가입 후 자동 생성된 게시글입니다."
    }' | jq '.'
else
  echo "사용자 ID를 찾을 수 없어 게시글 테스트를 건너뜁니다."
fi

echo ""
echo "=== 테스트 완료 ==="