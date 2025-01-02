#!/bin/bash

# 로그 디렉토리 구조 설정
LOG_DIR="/home/ec2-user/logs"
APP_LOG_DIR="${LOG_DIR}/application"
DEPLOY_LOG_DIR="${LOG_DIR}/deploy"
CURRENT_DATE=$(date +%Y-%m-%d)

# 로그 디렉토리 생성
mkdir -p "${APP_LOG_DIR}/${CURRENT_DATE}"
mkdir -p "${DEPLOY_LOG_DIR}/${CURRENT_DATE}"

# 로그 파일 경로 설정
LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/application.log"
ERROR_LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/application_error.log"

# Spring Boot 애플리케이션의 로깅 설정 추가
JAVA_OPTS="-Dspring.profiles.active=dev -Dlogging.file.path=${APP_LOG_DIR}/${CURRENT_DATE}"

# 로그 시작
echo "=== Application Start: $(date) ===" >> $LOG_FILE

# Redis 서비스 상태 확인 및 시작 (redis6로 변경)
if ! systemctl is-active --quiet redis6; then
    echo "Starting Redis server..." >> $LOG_FILE
    sudo systemctl start redis6
    sleep 5
fi

# Redis 연결 테스트 (redis6-cli 사용)
if ! /usr/bin/redis6-cli ping > /dev/null; then
    echo "Redis server is not responding" >> $ERROR_LOG_FILE
    exit 1
fi

# 이전 Java 프로세스 종료
echo "Stopping any existing java applications..." >> $LOG_FILE
pkill -f java || true

# JAR 파일 경로 설정
cd /home/ec2-user/app
JAR_FILE=$(ls *.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "No JAR file found in /home/ec2-user/app" >> $ERROR_LOG_FILE
    exit 1
fi

echo "Starting application: $JAR_FILE" >> $LOG_FILE

# JAR 파일 실행
nohup java $JAVA_OPTS -jar $JAR_FILE >> $LOG_FILE 2>> $ERROR_LOG_FILE &

# 프로세스 시작 확인
sleep 10
if pgrep -f java > /dev/null && /usr/bin/redis6-cli ping > /dev/null; then
    echo "Application and Redis started successfully: $(date)" >> $LOG_FILE
    exit 0
else
    echo "Failed to start application or Redis: $(date)" >> $ERROR_LOG_FILE
    cat $ERROR_LOG_FILE
    exit 1
fi