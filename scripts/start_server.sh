#!/bin/bash

# JAR 파일 경로 설정
BUILD_JAR=$(ls /home/ec2-user/app/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)

# 로그 파일에 빌드 파일명 기록
echo ">>> build 파일명: $JAR_NAME" >> /home/ec2-user/deploy.log

# 빌드된 JAR 파일을 배포 경로로 복사
echo ">>> build 파일 복사" >> /home/ec2-user/deploy.log
DEPLOY_PATH=/home/ec2-user/app/
cp $BUILD_JAR $DEPLOY_PATH

# 현재 실행 중인 Java 애플리케이션의 PID를 찾아 종료
echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> /home/ec2-user/deploy.log
sudo ps -ef | grep java | awk '{print $2}' | xargs kill -15

# 배포할 JAR 파일의 경로 설정
DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> DEPLOY_JAR 배포" >> /home/ec2-user/deploy.log
echo ">>> $DEPLOY_JAR의 $JAR_NAME를 실행합니다" >> /home/ec2-user/deploy.log

# JAR 파일을 백그라운드에서 실행하고 로그 파일에 출력
nohup java -jar $DEPLOY_JAR >> /home/ec2-user/deploy.log 2> /home/ec2-user/deploy_err.log & 