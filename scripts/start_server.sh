#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/application.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/application_error.log"

# 로그 디렉토리 생성
mkdir -p /home/ec2-user/deploy

# 로그 시작
echo "=== Application Start: $(date) ===" >> $LOG_FILE

# 이전 프로세스 종료
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
if pgrep -f java > /dev/null; then
    echo "Application started successfully: $(date)" >> $LOG_FILE
    exit 0
else
    echo "Failed to start application: $(date)" >> $ERROR_LOG_FILE
    cat $ERROR_LOG_FILE  # 에러 로그 출력
    exit 1
fi