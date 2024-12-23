#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/setup.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/setup_error.log"

# 로그 시작
echo "=== Setup Start: $(date) ===" >> $LOG_FILE

# 이전 애플리케이션 중지
echo "Stopping any existing java applications..." >> $LOG_FILE
pkill -f java || true

# 이전 배포 파일 정리
echo "Cleaning previous deployment files..." >> $LOG_FILE
rm -rf /home/ec2-user/app/*

# 필요한 디렉토리 생성
mkdir -p /home/ec2-user/app
mkdir -p /home/ec2-user/deploy

# 권한 설정
chown -R ec2-user:ec2-user /home/ec2-user/app
chown -R ec2-user:ec2-user /home/ec2-user/deploy
chmod -R 755 /home/ec2-user/app
chmod -R 755 /home/ec2-user/deploy

echo "=== Setup Complete: $(date) ===" >> $LOG_FILE