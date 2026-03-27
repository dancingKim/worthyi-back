# AWS Rebuild Checklist - 2026-03-26

## 목적
- 새 AWS 계정에서 WorthyI 백엔드 dev 환경을 다시 세울 때, 이 문서 하나만 보고 순서대로 진행한다.
- 기준은 `api-dev.worthyilife.com` 복구다.
- 순수 복사용 값은 [aws-copy-values-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-copy-values-2026-03-27.md) 에 따로 모아 둔다.

## 이 프로젝트 기준 핵심 사실
- 리전: `ap-northeast-2`
- Secret 이름: `/WorthyI/dev`
- 앱 포트: `8080`
- Health check path: `/actuator/health`
- 배포 흐름: `GitHub Actions -> S3 -> CodeDeploy -> EC2`
- Redis: 현재는 `EC2 내부 redis6`
- 네트워크 결정: `VPC 1개를 dev/prod가 같이 사용`

## 복사용 핵심 값
```text
worthyi-main-vpc
172.31.0.0/16
worthyilife.com
api-dev.worthyilife.com
/WorthyI/dev
worthyi-dev-alb-sg
ALB security group for WorthyI dev
worthyi-dev-app-sg
App security group for WorthyI dev
worthyi-dev-rds-sg
RDS security group for WorthyI dev
worthyi-dev-app-1
worthyi-shared-db-subnet-group
worthyi-dev-postgres
worthyi-dev-alb
worthyi-dev-tg
worthyi-back-dev
worthyi-back-dev-group
```

### 한 VPC로 간다는 뜻
- 지금은 dev만 먼저 만든다.
- prod는 나중에 같은 VPC 안에 추가한다.
- 하지만 아래는 dev/prod를 따로 둔다.
- EC2
- RDS
- ALB
- Target Group
- Security Group
- Secret

## 먼저 정할 것
- [ ] `worthyilife.com` 도메인 소유권 확인
- [ ] `worthyi.com`은 legacy로만 둘지 아예 안 쓸지 결정
- [ ] 우선 `dev`만 복구할지 결정
- [ ] 이번에 쓸 S3 버킷 이름 결정

기록:
- 도메인 등록처:
- dev API 도메인: `api-dev.worthyilife.com`
- 버킷 이름:

---

## 1. VPC
- [ ] `worthyi-main-vpc` 생성
- [ ] Public subnet 2개 생성
- [ ] Private subnet 2개 생성
- [ ] Internet Gateway 연결 확인
- [ ] DNS hostnames / DNS resolution 활성화 확인

추천값:
- IPv4 CIDR: `172.31.0.0/16`
- AZ: 2개
- NAT gateway: `없음`

메모:
- 이 VPC를 dev와 prod가 같이 쓴다.
- 지금 단계에서는 dev 리소스만 넣는다.

기록:
- VPC ID:
- Public subnet A:
- Public subnet B:
- Private subnet A:
- Private subnet B:

---

## 2. 보안그룹
- [ ] `worthyi-dev-alb-sg` 생성
- [ ] `worthyi-dev-app-sg` 생성
- [ ] `worthyi-dev-rds-sg` 생성
- [ ] Description도 같이 입력

복사용 값:
```text
worthyi-dev-alb-sg
ALB security group for WorthyI dev

worthyi-dev-app-sg
App security group for WorthyI dev

worthyi-dev-rds-sg
RDS security group for WorthyI dev
```

필수 규칙:
- `ALB SG`: 80/443 인터넷 허용
- `App SG`: 8080은 `ALB SG`에서만 허용
- `App SG`: 22는 내 IP만 허용
- `RDS SG`: 5432는 `App SG`에서만 허용

주의:
- Security Group 이름은 `sg-`로 시작하면 안 됨

기록:
- ALB SG:
- ALB SG Description:
- App SG:
- App SG Description:
- RDS SG:
- RDS SG Description:

---

## 3. IAM
- [ ] EC2 role `worthyi-dev-ec2-role` 생성
- [ ] `AmazonSSMManagedInstanceCore` 연결
- [ ] `AmazonS3ReadOnlyAccess` 연결
- [ ] `/WorthyI/dev` 읽기용 Secrets Manager 권한 추가
- [ ] CodeDeploy service role `CodeDeployServiceRole` 생성
- [ ] `AWSCodeDeployRole` 연결
- [ ] IAM user `github-actions-deployer` 생성
- [ ] deployer access key 생성
- [ ] deployer에 S3 업로드 + CodeDeploy 배포 권한 부여

기록:
- EC2 role:
- CodeDeploy service role:
- GitHub Actions deployer user:

---

## 4. EC2
- [ ] Amazon Linux 계열 인스턴스 생성
- [ ] 이름 `worthyi-dev-app-1`
- [ ] public subnet에 배치
- [ ] public IP 활성화
- [ ] `worthyi-dev-app-sg` 연결
- [ ] `worthyi-dev-ec2-role` 연결
- [ ] 상태가 `2/2 checks passed`

추천:
- 타입 `t3.small` 또는 `t3.medium`

기록:
- EC2 instance ID:
- Private IP:
- Public IP:

---

## 5. RDS
- [ ] DB subnet group `worthyi-shared-db-subnet-group` 생성
- [ ] private subnet 2개 포함
- [ ] PostgreSQL RDS 생성
- [ ] public access `No`
- [ ] VPC는 `worthyi-main-vpc`
- [ ] subnet group은 `worthyi-shared-db-subnet-group`
- [ ] SG는 `worthyi-dev-rds-sg`
- [ ] 상태가 `Available`

추천:
- DB identifier: `worthyi-dev-postgres`
- DB name: `worthyi_dev`

기록:
- DB endpoint:
- DB port: `5432`
- DB name:
- DB username:

---

## 6. Secrets Manager
- [ ] secret 생성
- [ ] secret 이름을 정확히 `/WorthyI/dev`로 입력
- [ ] 아래 키를 모두 입력

필수 키:
- [ ] `JWT_SECRET_KEY`
- [ ] `DB_URL`
- [ ] `DB_USERNAME`
- [ ] `DB_PASSWORD`
- [ ] `GOOGLE_CLIENT_ID`
- [ ] `GOOGLE_CLIENT_SECRET`
- [ ] `APPLE_CLIENT_ID`
- [ ] `APPLE_CLIENT_SECRET`
- [ ] `APPLE_KID`
- [ ] `APPLE_TID`

주의:
- `DB_URL` 형식은 `호스트:포트/DB명`
- 예: `xxxxx.ap-northeast-2.rds.amazonaws.com:5432/worthyi_dev`

검증:
- [ ] `Retrieve secret value`로 키 이름 다시 확인

Google OAuth:
- [ ] Google Cloud project 선택 또는 생성
- [ ] Google Auth Platform 앱 기본 등록
- [ ] `Web application` client 생성
- [ ] dev redirect URI를 정확히 `https://api-dev.worthyilife.com/login/oauth2/code/google`로 등록
- [ ] `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`를 `/WorthyI/dev`에 저장
- [ ] 자세한 절차는 [google-oauth-client-follow-along-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/google-oauth-client-follow-along-2026-03-27.md) 참고

---

## 7. S3
- [ ] 배포 버킷 생성
- [ ] 리전 `ap-northeast-2`
- [ ] 이름이 workflow와 일치하는지 확인

기록:
- Bucket name:

주의:
- 현재 workflow 기준 버킷 이름은 `worthyi-bucket-204098849975-ap-northeast-2-an`

---

## 8. CodeDeploy Agent on EC2
- [ ] Session Manager 또는 SSH로 EC2 접속
- [ ] CodeDeploy agent 설치
- [ ] `codedeploy-agent` running 확인

설치 순서:

```bash
sudo yum update -y
sudo yum install -y ruby wget
cd /home/ec2-user
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
sudo ./install auto
sudo systemctl status codedeploy-agent
```

검증:
- [ ] `codedeploy-agent` running

---

## 9. EC2 태그
- [ ] EC2에 아래 태그 추가

필수 태그:
- [ ] `Project = WorthyI`
- [ ] `Environment = dev`
- [ ] `DeployGroup = worthyi-back-dev`

---

## 10. CodeDeploy
- [ ] application `worthyi-back-dev` 생성
- [ ] compute platform `EC2/On-Premises`
- [ ] deployment group `worthyi-back-dev-group` 생성
- [ ] service role은 `CodeDeployServiceRole`
- [ ] deployment type은 `In-place`
- [ ] tag filter `DeployGroup = worthyi-back-dev`
- [ ] deployment config `CodeDeployDefault.AllAtOnce`

기록:
- CodeDeploy application:
- CodeDeploy deployment group:

---

## 11. Route53
- [ ] Public Hosted Zone `worthyilife.com` 생성
- [ ] registrar에서 nameserver를 Route53 NS 4개로 교체
- [ ] Hosted Zone 적용 완료 확인

기록:
- Route53 Hosted Zone ID:
- Nameserver 1:
- Nameserver 2:
- Nameserver 3:
- Nameserver 4:

---

## 12. ACM
- [ ] `ap-northeast-2`에서 인증서 요청
- [ ] 도메인 `api-dev.worthyilife.com`
- [ ] DNS validation 선택
- [ ] Route53 validation record 생성
- [ ] 상태가 `Issued`

기록:
- ACM certificate ARN:

주의:
- ACM과 ALB는 반드시 같은 리전

---

## 13. Target Group
- [ ] target group `worthyi-dev-tg` 생성
- [ ] target type `Instances`
- [ ] protocol `HTTP`
- [ ] port `8080`
- [ ] health check path `/actuator/health`
- [ ] EC2 인스턴스 등록
- [ ] target 상태 `healthy`

기록:
- Target Group ARN:

---

## 14. ALB
- [ ] ALB `worthyi-dev-alb` 생성
- [ ] internet-facing
- [ ] public subnet 2개 선택
- [ ] `worthyi-dev-alb-sg` 연결
- [ ] 80 listener 생성
- [ ] 443 listener 생성
- [ ] 443 listener에 ACM 인증서 연결
- [ ] 기본 대상은 `worthyi-dev-tg`

권장:
- 80 -> 443 redirect

기록:
- ALB ARN:
- ALB DNS name:

---

## 15. Route53 Alias Record
- [ ] `worthyilife.com` Hosted Zone에서 record 생성
- [ ] 이름 `api-dev`
- [ ] type `A`
- [ ] Alias `Yes`
- [ ] 대상은 ALB

최종 결과:
- `api-dev.worthyilife.com -> ALB`

---

## 16. GitHub Actions Secrets
- [ ] backend GitHub repo secrets 열기
- [ ] `AWS_ACCESS_KEY_ID_DEPLOYER` 입력
- [ ] `AWS_SECRET_ACCESS_KEY_DEPLOYER` 입력
- [ ] `AWS_CODE_DEPLOY_APPLICATION = worthyi-back-dev`
- [ ] `AWS_CODE_DEPLOY_GROUP = worthyi-back-dev-group`

실제 매핑:
- [ ] `AWS_ACCESS_KEY_ID_DEPLOYER = github-actions-deployer access key ID`
- [ ] `AWS_SECRET_ACCESS_KEY_DEPLOYER = github-actions-deployer secret access key`

현재 workflow에서 실제로 쓰는 secret:
- [ ] `AWS_ACCESS_KEY_ID_DEPLOYER`
- [ ] `AWS_SECRET_ACCESS_KEY_DEPLOYER`
- [ ] `AWS_CODE_DEPLOY_APPLICATION`
- [ ] `AWS_CODE_DEPLOY_GROUP`

주의:
- deployer IAM user 또는 role은 최소한
- S3 업로드
- CodeDeploy create-deployment
권한이 있어야 한다.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `GOOGLE_CLIENT_ID`, `JWT_SECRET_KEY`는 GitHub가 아니라 `/WorthyI/dev`에 넣는다.

---

## 17. 첫 배포
- [ ] `develop` 브랜치 push 또는 workflow_dispatch 실행
- [ ] GitHub Actions build 성공
- [ ] S3 upload 성공
- [ ] CodeDeploy deployment 생성
- [ ] deployment status `Succeeded`

확인 위치:
- GitHub Actions
- AWS CodeDeploy console

---

## 18. 배포 후 점검
- [ ] EC2에서 앱 프로세스 확인
- [ ] EC2에서 redis6 실행 확인
- [ ] Target Group health `healthy`
- [ ] `https://api-dev.worthyilife.com/actuator/health` 접속
- [ ] 응답 `200`
- [ ] Google login 확인
- [ ] Apple login 확인
- [ ] `/auth/token` 확인

---

## 19. prod를 나중에 추가할 때
- [ ] 새 VPC는 만들지 않기
- [ ] 같은 `worthyi-main-vpc` 사용
- [ ] prod용 SG 별도 생성
- [ ] prod용 EC2 별도 생성
- [ ] prod용 RDS 별도 생성
- [ ] prod용 ALB 또는 listener/target 구조 분리
- [ ] `/WorthyI/prod` secret 생성
- [ ] `api.worthyilife.com` Route53/ACM 연결
- [ ] backend prod OAuth redirect URI도 같이 점검

---

## 막히면 제일 먼저 볼 것

### 앱 부팅 실패
- [ ] `/WorthyI/dev` 이름이 정확한가
- [ ] `DB_URL` 형식이 맞는가
- [ ] EC2 role에 secret 읽기 권한이 있는가

### Target unhealthy
- [ ] 앱이 8080에서 떠 있는가
- [ ] App SG가 8080을 ALB SG에서 받는가
- [ ] health check path가 `/actuator/health`인가

### 도메인 접속 실패
- [ ] registrar NS를 Route53으로 바꿨는가
- [ ] ACM 상태가 `Issued`인가
- [ ] Alias record가 ALB를 가리키는가

### 배포 실패
- [ ] GitHub secrets가 맞는가
- [ ] S3 bucket 이름이 workflow와 같은가
- [ ] CodeDeploy application/group 이름이 secrets와 같은가
- [ ] EC2에 CodeDeploy agent가 떠 있는가
- [ ] EC2 태그가 deployment group 조건과 같은가

---

## 관련 문서
- 개요 문서: [aws-rebuild-guide-2026-03-26.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-rebuild-guide-2026-03-26.md)
- Route53/ACM/ALB 따라하기: [aws-console-follow-along-2026-03-26.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-console-follow-along-2026-03-26.md)
- VPC/EC2/RDS/Secrets/CodeDeploy 따라하기: [aws-console-follow-along-core-infra-2026-03-26.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-console-follow-along-core-infra-2026-03-26.md)
- Google OAuth client 따라하기: [google-oauth-client-follow-along-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/google-oauth-client-follow-along-2026-03-27.md)
