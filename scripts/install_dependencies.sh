#!/bin/bash

set -euo pipefail

# 로그 디렉토리 구조 설정
LOG_DIR="/home/ec2-user/logs"
DEPLOY_LOG_DIR="${LOG_DIR}/deploy"
CURRENT_DATE=$(date +%Y-%m-%d)

# 로그 디렉토리 생성
mkdir -p "${DEPLOY_LOG_DIR}/${CURRENT_DATE}"

# 로그 파일 경로 설정
LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/install_dependencies.log"
ERROR_LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/install_dependencies_error.log"

# 로그 시작
echo "=== Installation Start: $(date) ===" >> $LOG_FILE

# 배포 시점에는 패키지 설치/업데이트를 하지 않는다.
# OS 패키지 작업은 인스턴스 초기 구성 단계에서만 수행한다.
if type -p java >/dev/null 2>&1; then
    echo "Java is already installed." >> $LOG_FILE
else
    echo "Java is not installed. Provision the instance before deploying." >> $ERROR_LOG_FILE
    exit 1
fi

# Redis 패키지 확인 및 기동
if rpm -q redis6 >/dev/null 2>&1; then
    echo "Redis package is installed." >> $LOG_FILE
else
    echo "Redis6 is not installed. Provision the instance before deploying." >> $ERROR_LOG_FILE
    exit 1
fi

{
    sudo systemctl enable redis6
    sudo systemctl start redis6
} >> $LOG_FILE 2>> $ERROR_LOG_FILE

if systemctl is-active --quiet redis6; then
    echo "Redis startup successful" >> $LOG_FILE
    /usr/bin/redis6-cli ping >> $LOG_FILE 2>&1
else
    echo "Redis startup failed" >> $ERROR_LOG_FILE
    exit 1
fi

echo "=== Installation End: $(date) ===" >> $LOG_FILE
