#!/bin/bash

# 로그 디렉토리 구조 설정
LOG_DIR="/home/ec2-user/logs"
DEPLOY_LOG_DIR="${LOG_DIR}/deploy"
CURRENT_DATE=$(date +%Y-%m-%d)

# 로그 파일 경로 설정
LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/install_dependencies.log"
ERROR_LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/install_dependencies_error.log"

# 로그 디렉토리 생성
mkdir -p ${DEPLOY_LOG_DIR}/${CURRENT_DATE}

# 로그 시작
echo "=== Installation Start: $(date) ===" >> $LOG_FILE

# logrotate 설치
{
    echo "Installing logrotate..." >> $LOG_FILE
    sudo yum install -y logrotate
} >> $LOG_FILE 2>> $ERROR_LOG_FILE

# 시스템 패키지 업데이트
{
    echo "Updating system packages..." >> $LOG_FILE
    sudo yum update -y
} >> $LOG_FILE 2>> $ERROR_LOG_FILE

# Java 설치 확인 및 설치
if type -p java; then
    echo "Java is already installed." >> $LOG_FILE
else
    echo "Installing Amazon Corretto JDK..." >> $LOG_FILE
    {
        sudo yum install -y java-23-amazon-corretto
    } >> $LOG_FILE 2>> $ERROR_LOG_FILE
fi

# Redis 설치 확인 및 설치
if systemctl is-active --quiet redis6; then
    echo "Redis is already installed and running." >> $LOG_FILE
else
    echo "Installing Redis..." >> $LOG_FILE
    {
        # Redis6 설치
        sudo yum install -y redis6
        
        # Redis 서비스 시작 및 자동 시작 설정
        sudo systemctl start redis6
        sudo systemctl enable redis6
    } >> $LOG_FILE 2>> $ERROR_LOG_FILE
fi

# Redis 상태 확인 (redis6로 서비스 이름 변경)
if systemctl is-active --quiet redis6; then
    echo "Redis installation and startup successful" >> $LOG_FILE
    redis-cli ping >> $LOG_FILE 2>&1
else
    echo "Redis installation or startup failed" >> $ERROR_LOG_FILE
    exit 1
fi

echo "=== Installation End: $(date) ===" >> $LOG_FILE