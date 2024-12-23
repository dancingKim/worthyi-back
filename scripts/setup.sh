#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/setup.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/setup_error.log"

# 로그 시작
echo "=== Setup Start: $(date) ===" >> $LOG_FILE

# 이전 애플리케이션 중지
echo "Stopping any existing java applications..." >> $LOG_FILE
pkill -f java || true

# 이전 배포 파일 정리 (jar 파일만)
echo "Cleaning previous deployment files..." >> $LOG_FILE
rm -f /home/ec2-user/app/*.jar

# 필요한 디렉토리 생성
mkdir -p /home/ec2-user/app
mkdir -p /home/ec2-user/deploy
mkdir -p /home/ec2-user/app/scripts

# 초기 권한 설정
chown -R ec2-user:ec2-user /home/ec2-user/app
chown -R ec2-user:ec2-user /home/ec2-user/deploy
chmod -R 755 /home/ec2-user/app
chmod -R 755 /home/ec2-user/deploy

# 스크립트 파일 권한 설정
if [ -d "/home/ec2-user/app/scripts" ]; then
    echo "Setting execute permissions for scripts..." >> $LOG_FILE
    chmod 755 /home/ec2-user/app/scripts/*.sh
fi

echo "=== Setup Complete: $(date) ===" >> $LOG_FILE