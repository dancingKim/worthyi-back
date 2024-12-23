#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/application.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/application_error.log"

# 로그 디렉토리 생성
mkdir -p /home/ec2-user/deploy

# 로그 시작
echo "=== Application Start: $(date) ===" >> $LOG_FILE

# Redis 서비스 상태 확인 및 시작 (redis6로 변경)
if ! systemctl is-active --quiet redis6
    echo "Starting Redis server..." >> $LOG_FILE
    sudo systemctl start redis6
    sleep 5
fi

# Redis 연결 테스트 (redis-cli 경로 수정)
if ! /usr/bin/redis-cli ping > /dev/null; then
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
JAVA_OPTS="-Dspring.profiles.active=dev"
nohup java $JAVA_OPTS -jar $JAR_FILE >> $LOG_FILE 2>> $ERROR_LOG_FILE &

# 프로세스 시작 확인
sleep 10
if pgrep -f java > /dev/null && /usr/bin/redis-cli ping > /dev/null; then
    echo "Application and Redis started successfully: $(date)" >> $LOG_FILE
    exit 0
else
    echo "Failed to start application or Redis: $(date)" >> $ERROR_LOG_FILE
    cat $ERROR_LOG_FILE
    exit 1
fi