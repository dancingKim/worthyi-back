# AWS Rebuild Guide - 2026-03-26

## 범위
- Backend repo: `/Users/ho/IdeaProjects/worthyi-back`
- Client repo: `/Users/ho/WebstormProjects/worthy-i`
- 목적: AWS 계정을 새로 만든 뒤, 현재 서비스가 어떤 AWS 자원에 의존하는지 정리하고 재구축 순서를 남긴다.

## 복사용 핵심 값
```text
worthyilife.com
api-dev.worthyilife.com
api.worthyilife.com
/WorthyI/dev
/WorthyI/prod
worthyi-main-vpc
worthyi-dev-alb
worthyi-dev-tg
worthyi-dev-alb-sg
ALB security group for WorthyI dev
worthyi-dev-app-sg
App security group for WorthyI dev
worthyi-dev-rds-sg
RDS security group for WorthyI dev
worthyi-dev-app-1
worthyi-dev-postgres
worthyi-shared-db-subnet-group
worthyi-back-dev
worthyi-back-dev-group
```

## 결론 요약
- 백엔드는 AWS 의존성이 분명하다.
- 클라이언트는 AWS SDK, Amplify, Cognito를 직접 쓰지 않는다.
- 현재 레포 기준으로 가장 먼저 복구해야 하는 축은 `Secrets Manager -> DB(PostgreSQL) -> EC2 -> Redis -> S3 -> CodeDeploy -> 도메인/HTTPS` 순서다.
- 현재 운영 흐름은 `develop 브랜치 -> GitHub Actions -> S3 -> CodeDeploy -> EC2` 구조다.
- Redis는 현재 ElastiCache가 아니라 EC2 내부 로컬 Redis를 쓰는 구조로 보인다.

## 현재 AWS 사용처

### 1. Secrets Manager
- 백엔드는 AWS Secrets Manager에서 설정을 읽는다.
- dev: `/WorthyI/dev`
- prod: `/WorthyI/prod`

근거:
- `build.gradle`에서 Spring Cloud AWS Secrets Manager 의존성 사용
- `application-local.yaml`, `application-dev.yaml`, `application-prod.yaml`에서 `aws-secretsmanager:` import 사용

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/build.gradle`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-local.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`

### 2. EC2
- 배포 스크립트가 `/home/ec2-user/...` 경로를 전제로 동작한다.
- 로그, 앱, 배포 스크립트 경로도 모두 EC2 리눅스 서버 구조를 가정한다.

근거:
- `appspec.yml`
- `scripts/setup.sh`
- `scripts/install_dependencies.sh`
- `scripts/start_server.sh`

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/appspec.yml`
- `/Users/ho/IdeaProjects/worthyi-back/scripts/setup.sh`
- `/Users/ho/IdeaProjects/worthyi-back/scripts/install_dependencies.sh`
- `/Users/ho/IdeaProjects/worthyi-back/scripts/start_server.sh`

### 3. S3
- GitHub Actions가 배포 산출물 `app.zip`을 S3 버킷에 업로드한다.
- 현재 레포에 하드코딩된 버킷 이름은 `worthyi-bucket-204098849975-ap-northeast-2-an`이다.

근거:
- `.github/workflows/deploy.yml`

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/.github/workflows/deploy.yml`

### 4. CodeDeploy
- GitHub Actions가 S3에 올린 zip을 기준으로 CodeDeploy 배포를 실행한다.
- CodeDeploy application 이름과 deployment group 이름은 GitHub secrets에서 읽는다.

근거:
- `.github/workflows/deploy.yml`
- `appspec.yml`

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/.github/workflows/deploy.yml`
- `/Users/ho/IdeaProjects/worthyi-back/appspec.yml`

### 5. PostgreSQL
- 백엔드는 PostgreSQL을 사용한다.
- 레포에서는 DB가 AWS RDS인지 명시되지 않는다.
- 다만 새 AWS 계정에서 다시 만들 때는 RDS PostgreSQL로 재구성하는 것이 가장 자연스럽다.

근거:
- PostgreSQL 드라이버 사용
- datasource가 환경변수 기반 `jdbc:postgresql://...`

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/build.gradle`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-local.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`

### 6. Redis
- 앱은 인증 코드, refresh token, blacklist 저장에 Redis를 사용한다.
- 현재 배포 스크립트는 EC2 서버에 `redis6`를 직접 설치하고 실행한다.
- 따라서 현재 레포 기준으로는 ElastiCache가 아니라 EC2 로컬 Redis 사용으로 보는 것이 맞다.

근거:
- `install_dependencies.sh`가 `yum install redis6`
- `start_server.sh`가 `systemctl start redis6`
- `AuthService`, `OAuth2AuthenticationSuccessHandler`, `JwtAuthenticationFilter` 등이 `StringRedisTemplate` 사용

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/scripts/install_dependencies.sh`
- `/Users/ho/IdeaProjects/worthyi-back/scripts/start_server.sh`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/java/com/worthyi/worthyi_backend/service/AuthService.java`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/java/com/worthyi/worthyi_backend/security/OAuth2AuthenticationSuccessHandler.java`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/java/com/worthyi/worthyi_backend/security/JwtAuthenticationFilter.java`

### 7. Route53 / 도메인 / HTTPS / Load Balancer
- 레포에 도메인으로 `api-dev.worthyilife.com`, `api.worthyi.com`, `www.worthyilife.com`, `worthyilife.com`이 등장한다.
- 백엔드는 forwarded header 환경을 고려한다.
- 따라서 실제 운영은 `Route53 + 인증서 + ALB 또는 리버스 프록시` 구조였을 가능성이 높다.
- 다만 레포 안에는 ALB, Route53, ACM, Nginx 설정 파일이 없다.
- 현재 복구 목표 도메인은 `api.worthyilife.com`이고, `api.worthyi.com`은 legacy 흔적으로 보는 것이 맞다.

정리:
- `Route53 사용 가능성`: 높음
- `ACM 사용 가능성`: 높음
- `ALB 사용 가능성`: 중간 이상
- `확정 여부`: 레포만으로는 확정 불가

관련 파일:
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`
- `/Users/ho/WebstormProjects/worthy-i/app.config.js`

## 클라이언트에서 확인한 내용
- 클라이언트는 AWS를 직접 호출하지 않는다.
- AWS SDK, Amplify, Cognito 사용 흔적을 찾지 못했다.
- 클라이언트는 `BASE_URL`, `OAUTH_BASE_URL`로 백엔드 도메인만 바라본다.
- 현재 client `production` 설정도 `https://api-dev.worthyilife.com`으로 되어 있다.
- 토큰 refresh URL도 dev 도메인이 하드코딩되어 있다.

의미:
- 새 AWS 계정에서 우선 복구할 대상은 백엔드다.
- 클라이언트는 백엔드 API 도메인만 정상 복구되면 대부분 동작 가능하다.
- 다만 prod 인프라를 복구해도 클라이언트 설정이 dev 도메인을 보고 있으면 잘못 붙을 수 있다.

관련 파일:
- `/Users/ho/WebstormProjects/worthy-i/app.config.js`
- `/Users/ho/WebstormProjects/worthy-i/utils/api.ts`
- `/Users/ho/WebstormProjects/worthy-i/hooks/api/useApiGeneric.ts`

## 다시 만들어야 하는 AWS 자원

### 거의 확정
- Secrets Manager
- EC2
- S3
- CodeDeploy
- GitHub Actions deployer IAM user
- IAM user 또는 role
- 보안그룹
- PostgreSQL용 DB 자원

### 강하게 의심됨
- Route53 hosted zone
- ACM certificate
- ALB

### 현재 레포 기준으로 불필요하거나 미확인
- Amplify
- Cognito
- AppSync
- Lambda
- CloudFront
- SES
- SNS
- SQS
- ElastiCache

## AWS 밖에서 같이 다시 만들어야 하는 것

### Google OAuth Client
- Google 로그인은 AWS 리소스가 아니라 Google Cloud Console의 `Google Auth Platform`에서 다시 만들어야 한다.
- 현재 dev backend redirect URI는 `https://api-dev.worthyilife.com/login/oauth2/code/google`이다.
- 현재 prod backend 코드는 아직 `https://api.worthyi.com/login/oauth2/code/google`을 기대한다.
- prod를 `api.worthyilife.com`으로 바꿀 계획이면 backend 설정을 먼저 맞추고 prod client를 만드는 것이 안전하다.
- 자세한 따라 하기 문서는 [google-oauth-client-follow-along-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/google-oauth-client-follow-along-2026-03-27.md) 참고

## 가장 현실적인 재구축 순서

### 1. dev 환경부터 복구
- 현재 서버 시작 스크립트가 `dev` 프로필로 고정이다.
- GitHub Actions도 `develop` 브랜치 푸시 기준 배포다.
- 클라이언트도 대부분 `api-dev.worthyilife.com`을 본다.

먼저 복구할 대상:
- `api-dev.worthyilife.com`
- `/WorthyI/dev`
- dev DB
- dev EC2

### 2. Secrets Manager 생성
먼저 아래 시크릿을 만들 것.

#### `/WorthyI/dev`
- `JWT_SECRET_KEY`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `APPLE_CLIENT_ID`
- `APPLE_CLIENT_SECRET`
- `APPLE_KID`
- `APPLE_TID`

#### `/WorthyI/prod`
- `JWT_SECRET_KEY`
- `PROD_DB_URL`
- `PROD_DB_USERNAME`
- `PROD_DB_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

주의:
- prod 설정에는 Apple 항목이 현재 YAML에 없다.
- 다만 향후 prod에서도 Apple 로그인 쓸 거면 prod secret에도 Apple 관련 값을 같이 두는 편이 안전하다.

### 3. PostgreSQL 생성
권장:
- AWS RDS PostgreSQL 생성

필요한 결정:
- dev/prod를 분리할지
- 단일 인스턴스 내 다중 DB로 갈지
- 백업/스냅샷 정책

Secrets Manager에 넣어야 할 값 형태 예시:
- `DB_URL`: `host:5432/dbname`
- `DB_USERNAME`
- `DB_PASSWORD`

주의:
- 현재 코드상 DB 스키마 일부는 앱 시작 시 반영될 수 있어도, 기존 데이터는 자동 복구되지 않는다.

### 4. EC2 생성
권장 사양:
- Amazon Linux 계열 1대부터 시작
- dev 먼저 생성

필수 작업:
- EC2 인스턴스 프로파일에 Secrets Manager 읽기 권한 부여
- CodeDeploy agent 설치
- Java 실행 가능 환경 확보
- 보안그룹에서 앱 포트와 SSH 정책 정리

현재 스크립트 전제:
- 앱 배포 위치: `/home/ec2-user/app`
- 스크립트 위치: `/home/ec2-user/scripts`
- 로그 위치: `/home/ec2-user/logs`

### 5. Redis 구성
현재 레포 기준 권장:
- dev는 EC2 내부 Redis로 그대로 복구

이유:
- 스크립트가 이미 `redis6` 설치와 시작을 자동화한다.
- 코드에 Redis host/port 외부화 흔적이 거의 없다.

나중에 확장 필요 시:
- ElastiCache Redis로 옮길 수는 있지만
- 그 경우 Redis 연결 설정을 코드/설정으로 분리하는 작업이 필요하다.

### 6. S3 버킷 생성
현재 배포 파일 기준 버킷:
- `worthyi-bucket-204098849975-ap-northeast-2-an`

선택지:
- 같은 이름을 다시 쓸 수 있으면 그대로 사용
- 불가능하면 새 이름 사용 후 GitHub Actions 수정

버킷 용도:
- CodeDeploy용 배포 zip 저장

### 7. CodeDeploy 생성
필요한 리소스:
- CodeDeploy application
- Deployment group
- EC2 대상 태그 또는 ASG 연결
- 서비스 role

현재 GitHub Actions가 참조하는 값:
- `AWS_CODE_DEPLOY_APPLICATION`
- `AWS_CODE_DEPLOY_GROUP`

### 8. GitHub Actions secrets 복구
GitHub 저장소에 다시 넣어야 할 값:
- `AWS_ACCESS_KEY_ID_DEPLOYER`
- `AWS_SECRET_ACCESS_KEY_DEPLOYER`
- `AWS_CODE_DEPLOY_APPLICATION`
- `AWS_CODE_DEPLOY_GROUP`

필요 권한:
- S3 업로드
- CodeDeploy 배포 생성

즉 AWS 안에서는 GitHub Actions용 IAM user와 access key도 같이 다시 만들어야 한다.
추천 user 이름:
- `github-actions-deployer`

### 9. Route53 / ACM / ALB 또는 HTTPS 앞단 복구
우선순위:
1. `api-dev.worthyilife.com`
2. 필요 시 `api.worthyilife.com`

가능한 구성 A:
- Route53
- ACM
- ALB
- EC2

가능한 구성 B:
- Route53
- EC2 공인 IP
- Nginx + HTTPS

레포만으로 확정 불가한 점:
- 이전에 ALB를 썼는지
- EC2에 직접 SSL termination 했는지
- 과거 `worthyi.com`을 어디까지 썼는지

## 추천 복구 순서 한 줄 버전
1. Secrets Manager
2. RDS PostgreSQL
3. EC2
4. Redis on EC2
5. S3 bucket
6. CodeDeploy
7. GitHub Actions secrets
8. Route53 + ACM + ALB 또는 Nginx HTTPS
9. 클라이언트 BASE_URL/OAUTH_BASE_URL 확인

## 바로 실행 가능한 추천 순서

### 단계 1. dev만 먼저 살리기
- `/WorthyI/dev` 생성
- RDS PostgreSQL 생성
- EC2 생성
- EC2 role 연결
- Redis 설치 가능 상태 확인
- S3 bucket 생성
- CodeDeploy application/deployment group 생성
- `api-dev.worthyilife.com` 연결

### 단계 2. GitHub Actions 복구
- repo secrets 입력
- `develop` 브랜치 배포 테스트

### 단계 3. 앱/로그인 확인
- `/actuator/health`
- Google 로그인
- Apple 로그인
- `/auth/token`
- Redis 기반 refresh/logout

### 단계 4. prod 정리
- `/WorthyI/prod` 생성
- prod DB 생성
- `api.worthyilife.com` 연결
- backend prod OAuth redirect URI와 Google/Apple 콘솔 값 같이 점검
- client의 production base URL 수정 필요 여부 확인

## 시크릿 값 체크리스트

### 백엔드 기동 필수
- `JWT_SECRET_KEY`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

### Google 로그인
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

### Apple 로그인
- `APPLE_CLIENT_ID`
- `APPLE_CLIENT_SECRET`
- `APPLE_KID`
- `APPLE_TID`

### local 전용 분리 값
- `DEV_GOOGLE_CLIENT_ID`
- `DEV_GOOGLE_CLIENT_SECRET`

## 지금 바로 확인해야 할 위험 요소

### 1. 도메인이 두 계열로 섞여 있음
- `worthyilife.com`
- `worthyi.com`

확인 필요:
- 실제 서비스 메인 도메인은 `worthyilife.com`으로 확정할지
- `worthyi.com`은 legacy 유지가 필요한지
- prod API는 `api.worthyilife.com`으로 정리할지

### 2. client production이 dev API를 바라봄
- `app.config.js`에서 `production`도 `api-dev.worthyilife.com`을 사용한다.

의미:
- prod 환경을 만들어도 앱이 dev 서버에 붙을 수 있다.

### 3. Redis가 외부 관리형이 아님
- 고가용성 구성이 아니라 EC2 인스턴스 내부 Redis 전제다.
- EC2 재시작/교체 시 상태 관리 전략이 약할 수 있다.

## 레포에서 못 확정한 것
- 이전 PostgreSQL이 AWS RDS였는지
- 이전에 ALB를 썼는지
- 이전에 ACM 인증서를 썼는지
- Route53 hosted zone이 정확히 어떤 도메인 기준이었는지
- prod 인프라가 실제로 운영 중이었는지, 아니면 dev를 사실상 공용으로 썼는지

## 추천 의사결정
- 당장 살리는 목적이면 `dev 단일 환경`부터 복구
- Redis는 우선 EC2 내부 Redis 유지
- DB는 RDS PostgreSQL 사용
- 도메인은 `api-dev.worthyilife.com`부터 연결
- 배포는 기존 방식 그대로 `GitHub Actions -> S3 -> CodeDeploy -> EC2`
- 이후 안정화되면 Redis/네트워크/HTTPS 구조를 별도 개선

## 다음 작업 제안
- 이 문서를 기준으로 AWS 콘솔에서 실제 생성 순서를 더 세분화한 실행 체크리스트 작성
- 필요하면 다음 문서로 아래를 추가할 수 있다:
- `AWS 콘솔 클릭 순서`
- `IAM 정책 예시`
- `Secrets Manager JSON 예시`
- `RDS/EC2/ALB 보안그룹 예시`
- `배포 검증 체크리스트`
