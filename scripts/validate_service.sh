#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/validate_service.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/validate_service_error.log"

# 로그 디렉토리 생성
mkdir -p /home/ec2-user/deploy

# 로그 시작
echo "=== Service Validation Start: $(date) ===" >> $LOG_FILE

# 애플리케이션이 시작될 때까지 대기
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    echo "Attempt $ATTEMPT of $MAX_ATTEMPTS: Checking service..." >> $LOG_FILE
    
    if curl -f http://localhost:8080/health > /dev/null 2>&1; then
        echo "Service is running successfully: $(date)" >> $LOG_FILE
        exit 0
    fi
    
    echo "Attempt $ATTEMPT: Service not ready yet..." >> $LOG_FILE
    sleep 10
    ATTEMPT=$((ATTEMPT+1))
done

echo "Service failed to start after $MAX_ATTEMPTS attempts: $(date)" >> $ERROR_LOG_FILE
echo "Last curl attempt result:" >> $ERROR_LOG_FILE
curl -v http://localhost:8080/health >> $ERROR_LOG_FILE 2>&1
exit 1 