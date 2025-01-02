#!/bin/bash

# 로그 디렉토리 구조 설정
LOG_DIR="/home/ec2-user/logs"
DEPLOY_LOG_DIR="${LOG_DIR}/deploy"
APP_LOG_DIR="${LOG_DIR}/application"
CURRENT_DATE=$(date +%Y-%m-%d)

# 로그 파일 경로 설정
LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/setup.log"
ERROR_LOG_FILE="${DEPLOY_LOG_DIR}/${CURRENT_DATE}/setup_error.log"

# 로그 디렉토리 생성
sudo mkdir -p ${DEPLOY_LOG_DIR}/${CURRENT_DATE}
sudo mkdir -p ${APP_LOG_DIR}/${CURRENT_DATE}

# 로그 파일 초기화
touch $LOG_FILE $ERROR_LOG_FILE

# 로그 시작
echo "=== Setup Start: $(date) ===" >> $LOG_FILE

# 필요한 디렉토리 생성
sudo mkdir -p /home/ec2-user/app
sudo mkdir -p /home/ec2-user/deploy
sudo mkdir -p /home/ec2-user/scripts

# 디렉토리 권한 설정
sudo chown -R ec2-user:ec2-user /home/ec2-user/app
sudo chown -R ec2-user:ec2-user /home/ec2-user/deploy
sudo chown -R ec2-user:ec2-user /home/ec2-user/scripts
sudo chown -R ec2-user:ec2-user ${LOG_DIR}
sudo chmod -R 755 /home/ec2-user/app
sudo chmod -R 755 /home/ec2-user/deploy
sudo chmod -R 755 /home/ec2-user/scripts
sudo chmod -R 755 ${LOG_DIR}

# logrotate 설정 파일 생성
sudo tee /etc/logrotate.d/worthyi << EOF
${LOG_DIR}/*/*.log {
    daily
    rotate 30
    compress
    dateext
    missingok
    notifempty
    create 644 ec2-user ec2-user
}
EOF

echo "=== Setup Complete: $(date) ===" >> $LOG_FILE