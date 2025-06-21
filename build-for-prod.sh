#!/bin/bash
set -e

# ===== 프로덕션 배포용 빌드 스크립트 =====
# 데이터베이스 초기화 방지 및 안전한 배포를 위한 스크립트

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 프로덕션 배포용 빌드 시작...${NC}"
echo "=================================================="

# 1. 필수 파일 체크
echo -e "\n${YELLOW}📋 필수 파일 확인 중...${NC}"

if [ ! -f src/main/resources/application-prod.properties ]; then
    echo -e "${RED}❌ application-prod.properties 파일이 없습니다.${NC}"
    echo "📝 src/main/resources/application-prod.properties를 생성하고 프로덕션 설정을 입력하세요."
    exit 1
fi

# 2. 위험한 설정 검사
echo -e "\n${YELLOW}🔍 설정 파일 검증 중...${NC}"

# DDL 설정 검사
DDL_SETTING=$(grep -E "spring\.jpa\.hibernate\.ddl-auto" src/main/resources/application-prod.properties | cut -d'=' -f2 | tr -d ' ')
if [[ "$DDL_SETTING" == "create" ]] || [[ "$DDL_SETTING" == "create-drop" ]]; then
    echo -e "${RED}❌ 위험: application-prod.properties에 ddl-auto=$DDL_SETTING 설정이 있습니다!${NC}"
    echo "데이터베이스가 초기화될 수 있습니다. 'none' 또는 'validate'로 변경하세요."
    exit 1
fi

# SQL 로깅 비활성화 확인
SQL_SHOW=$(grep -E "spring\.jpa\.show-sql" src/main/resources/application-prod.properties | cut -d'=' -f2 | tr -d ' ')
if [[ "$SQL_SHOW" == "true" ]]; then
    echo -e "${YELLOW}⚠️  경고: SQL 로깅이 활성화되어 있습니다. 성능에 영향을 줄 수 있습니다.${NC}"
fi

# 3. 환경 변수 파일 체크 (.env는 포함하지 않음)
if [ -f .env ]; then
    echo -e "${YELLOW}⚠️  주의: .env 파일이 감지되었습니다.${NC}"
    echo "   프로덕션 환경에서는 환경 변수를 시스템 레벨에서 설정하세요."
fi

# 4. 프로덕션 설정 확인
echo -e "\n${YELLOW}⚠️  프로덕션 체크리스트:${NC}"
echo "✓ 1. 데이터베이스 접속 정보가 프로덕션 DB를 가리키는지 확인"
echo "✓ 2. JWT 시크릿 키가 안전한 값으로 변경되었는지 확인"
echo "✓ 3. CORS 도메인이 실제 프론트엔드 도메인으로 설정되었는지 확인"
echo "✓ 4. 파일 업로드 경로가 적절한지 확인"
echo "✓ 5. 로그 레벨이 적절한지 확인 (WARN 이상 권장)"
echo ""
read -p "위 사항을 모두 확인했습니까? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}빌드가 취소되었습니다.${NC}"
    exit 1
fi

# 5. 클린 빌드
echo -e "\n${BLUE}🧹 기존 빌드 파일 정리...${NC}"
./gradlew clean

# 6. 프로덕션 빌드 실행 (표준 Gradle 명령 사용)
echo -e "\n${BLUE}📦 프로덕션 빌드 중 (테스트 제외)...${NC}"
./gradlew bootJar -x test -x testClasses

# 7. 빌드 결과 확인
echo -e "\n${BLUE}📊 빌드 결과 확인...${NC}"
JAR_FILE=$(ls build/libs/*.jar 2>/dev/null | grep -v plain | grep -v sources | head -1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}❌ JAR 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi

# 8. JAR 파일 정보 출력
echo -e "\n${GREEN}✅ 빌드 성공!${NC}"
echo "=================================================="
echo -e "📦 JAR 파일: ${GREEN}$JAR_FILE${NC}"
SIZE=$(du -h "$JAR_FILE" | cut -f1)
echo -e "📏 파일 크기: ${GREEN}$SIZE${NC}"
echo -e "📅 빌드 시간: ${GREEN}$(date)${NC}"

echo -e "${GREEN}🎉 빌드 프로세스가 완료되었습니다!${NC}"