# AWS Console Follow-Along - Core Infra - 2026-03-26

## 이 문서의 목적
- AWS 새 계정에서 `VPC`, `EC2`, `RDS`, `Secrets Manager`, `CodeDeploy`를 다시 만들 때 그대로 따라할 수 있게 정리한다.
- 이 문서는 [aws-console-follow-along-2026-03-26.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-console-follow-along-2026-03-26.md) 의 앞단 인프라 문서다.
- 기준은 `dev` 환경을 먼저 살리는 것이다.
- 네트워크 결정은 `VPC 1개를 dev/prod가 같이 쓰는 방식`으로 잡는다.
- 순수 복사용 값은 [aws-copy-values-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-copy-values-2026-03-27.md) 에 따로 모아 둔다.

## 복사용 핵심 값
```text
worthyi-main-vpc
172.31.0.0/16
worthyi-dev-alb-sg
ALB security group for WorthyI dev
worthyi-dev-app-sg
App security group for WorthyI dev
worthyi-dev-rds-sg
RDS security group for WorthyI dev
worthyi-dev-ec2-role
CodeDeployServiceRole
github-actions-deployer
worthyi-dev-app-1
worthyi-shared-db-subnet-group
worthyi-dev-postgres
/WorthyI/dev
worthyi-bucket-204098849975-ap-northeast-2-an
worthyi-back-dev
worthyi-back-dev-group
```

## 이 문서에서 만들 구조

### 현재 결정된 목표 구조
- VPC 1개
- Public subnet 2개
- Private DB subnet 2개
- EC2 1대
- RDS PostgreSQL 1개
- Secrets Manager secret 1개
- S3 bucket 1개
- CodeDeploy application 1개
- CodeDeploy deployment group 1개

### prod는 어디에 만드나
- prod도 같은 VPC 안에 만든다.
- 다만 아래 리소스는 dev와 분리한다.
- prod용 EC2
- prod용 RDS
- prod용 ALB
- prod용 보안그룹
- prod용 secret `/WorthyI/prod`

즉 구조는 아래처럼 간다.
- VPC는 공용 1개
- public subnet 2개는 dev/prod ALB와 EC2가 같이 사용 가능
- private DB subnet 2개는 dev/prod RDS가 같이 사용 가능
- 애플리케이션 리소스와 보안그룹만 env별로 분리

### 왜 이 구성을 추천하나
- ALB는 public subnet 2개가 필요하다.
- RDS는 private subnet에 두는 것이 안전하다.
- EC2는 우선 public subnet에 두면 NAT 없이도 설치와 점검이 쉽다.
- 현재 레포의 배포 스크립트는 EC2 단일 서버 구조를 전제로 한다.

## 현재 코드 기준 중요한 사실

### 1. 서버는 `dev` 프로필로 시작한다
- 현재 배포 스크립트가 `-Dspring.profiles.active=dev`를 사용한다.
- 따라서 첫 복구 대상은 `/WorthyI/dev`, dev DB, dev EC2다.

### 2. Secrets Manager 이름은 앞에 `/`가 붙는다
- dev secret 이름: `/WorthyI/dev`
- prod secret 이름: `/WorthyI/prod`

### 3. Redis는 현재 ElastiCache가 아니다
- 현재 스크립트는 EC2 안에 `redis6`를 직접 설치한다.
- 따라서 이번 문서에서는 Redis를 별도 AWS 서비스로 만들지 않는다.

### 4. 배포는 GitHub Actions -> S3 -> CodeDeploy -> EC2다
- workflow가 `worthyi-bucket-204098849975-ap-northeast-2-an`에 zip을 올린다.
- 그 뒤 CodeDeploy deployment를 만든다.

## 먼저 정해둘 추천 이름

### 리전
- `ap-northeast-2`

### 태그
- `Project = WorthyI`
- `Environment = dev`

### 예시 리소스 이름
- VPC: `worthyi-main-vpc`
- EC2 role: `worthyi-dev-ec2-role`
- EC2 instance: `worthyi-dev-app-1`
- RDS subnet group: `worthyi-shared-db-subnet-group`
- RDS instance: `worthyi-dev-postgres`
- Secret: `/WorthyI/dev`
- S3 bucket: `worthyi-bucket-204098849975-ap-northeast-2-an`
- CodeDeploy application: `worthyi-back-dev`
- CodeDeploy deployment group: `worthyi-back-dev-group`

## 지금 필요한 값

### 나중에 Secrets Manager에 넣을 값
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

---

## 1. VPC 만들기

이 문서는 복잡한 수동 네트워크 작업을 줄이기 위해 `VPC and more` 방식으로 간다.

### 추천 구성
- IPv4 CIDR: `172.31.0.0/16`
- AZ: 2개
- Public subnet: 2개
- Private subnet: 2개
- NAT gateway: `None`
- VPC endpoints: `None`

### 왜 NAT를 끄나
- EC2를 public subnet에 둘 것이므로 인터넷 설치가 가능하다.
- RDS는 인터넷에 직접 나갈 필요가 없다.
- NAT gateway는 비용이 든다.
- dev/prod가 같은 VPC를 쓰더라도 이 판단은 그대로 유효하다.

### 콘솔 클릭 순서
1. AWS Console에서 리전을 `ap-northeast-2`로 맞춘다.
2. `VPC` 콘솔로 이동한다.
3. 왼쪽 메뉴에서 `Your VPCs` 또는 상단 `Create VPC`를 누른다.
4. `VPC and more`를 선택한다.
5. 아래 값을 넣는다.

#### 입력값
- Name tag auto-generation: `worthyi-main`
- IPv4 CIDR block: `172.31.0.0/16`
- Number of Availability Zones: `2`
- Number of public subnets: `2`
- Number of private subnets: `2`
- NAT gateways: `None`
- VPC endpoints: `None`
- Enable DNS hostnames: `Yes`
- Enable DNS resolution: `Yes`

6. `Create VPC`를 누른다.

### 만들고 나면 확인할 것
- VPC 1개
- Internet Gateway 1개
- Public subnet 2개
- Private subnet 2개
- Public route table
- Private route table

### 성공 기준
- VPC Resource map에서 public/private subnet 구조가 보인다.

### 이 VPC를 나중에 어떻게 쓰나
- 지금은 dev만 올린다.
- prod를 나중에 올릴 때 새 VPC를 만들지 않는다.
- 같은 `worthyi-main-vpc` 안에 prod용 ALB, EC2, RDS를 추가한다.
- 대신 SG, target group, ALB, DB, secret은 prod용으로 따로 만든다.

---

## 2. 보안그룹 만들기

ALB 보안그룹은 이전 문서에서 만들었어도 된다.
여기서는 app, DB 기준으로 다시 정리한다.

### 2-1. ALB 보안그룹
이전 문서 기준:
```text
worthyi-dev-alb-sg
ALB security group for WorthyI dev
```
- Inbound: `80`, `443` from internet

### 2-2. EC2 앱 보안그룹
이름:
```text
worthyi-dev-app-sg
App security group for WorthyI dev
```

#### Inbound
- `TCP 8080` from `worthyi-dev-alb-sg`
- `SSH 22` from 내 IP만

#### Outbound
- All traffic allowed

### 2-3. RDS 보안그룹
이름:
```text
worthyi-dev-rds-sg
RDS security group for WorthyI dev
```

#### Inbound
- `PostgreSQL 5432` from `worthyi-dev-app-sg`

#### Outbound
- 기본값 유지

### 성공 기준
- app SG는 8080을 ALB SG에서만 받는다.
- DB SG는 5432를 app SG에서만 받는다.

### 이름 규칙
- Security Group 이름은 `sg-`로 시작하지 않는다.
- `sg-`는 AWS가 자동으로 생성하는 보안그룹 ID prefix다.

---

## 3. IAM 역할 만들기

이번 문서에서는 역할을 2개 만든다.

그리고 별도로 GitHub Actions용 IAM user 1개를 만든다.

### 역할 1. EC2 인스턴스 역할
목적:
- EC2에서 Secrets Manager 읽기
- EC2에서 S3 읽기
- Session Manager 접속을 쉽게 하기
- CodeDeploy agent가 S3 revision과 리소스킷 접근하기

### 추천 이름
- `worthyi-dev-ec2-role`

### 콘솔 클릭 순서
1. `IAM` 콘솔로 이동한다.
2. `Roles`
3. `Create role`
4. Trusted entity type: `AWS service`
5. Use case: `EC2`
6. `Next`

### 우선 붙일 정책
- `AmazonSSMManagedInstanceCore`
- `AmazonS3ReadOnlyAccess`

주의:
- `AmazonS3ReadOnlyAccess`는 시작은 쉽지만 권한이 넓다.
- 나중에 안정화되면 배포 버킷과 CodeDeploy 리소스킷 버킷만 읽게 줄이는 게 좋다.

### Secrets Manager 읽기 권한
가장 쉬운 방법:
- custom inline policy를 추가한다.

예시 권한 범위:
- `secretsmanager:GetSecretValue`
- `secretsmanager:DescribeSecret`

대상:
- `/WorthyI/dev`

만약 customer managed KMS key를 쓸 경우 추가:
- `kms:Decrypt`

### 역할 생성 후 이름
- `worthyi-dev-ec2-role`

### 역할 2. CodeDeploy 서비스 역할
목적:
- CodeDeploy가 EC2 인스턴스, 태그, 로드밸런서 등을 다룰 수 있게 한다.

### 추천 이름
- `CodeDeployServiceRole`

### 콘솔 클릭 순서
1. `IAM -> Roles -> Create role`
2. Trusted entity type: `AWS service`
3. Use case: `CodeDeploy`
4. compute platform은 `CodeDeploy`
5. `Next`
6. 정책으로 보이는 `AWSCodeDeployRole`을 선택
7. 이름을 `CodeDeployServiceRole`로 입력
8. 생성

### 성공 기준
- EC2 role과 CodeDeploy service role이 둘 다 보인다.

### 추가로 필요한 IAM user
현재 backend GitHub Actions workflow는 OIDC role assume 방식이 아니라 `access key` 방식이다.
그래서 role 2개와 별도로 GitHub Actions가 쓸 IAM user가 1개 더 필요하다.

### 추천 이름
- `github-actions-deployer`

### 목적
- GitHub Actions가 build 결과물을 S3에 업로드
- GitHub Actions가 CodeDeploy deployment 생성

### 콘솔 클릭 순서
1. `IAM` 콘솔로 이동한다.
2. `Users`
3. `Create user`
4. User name: `github-actions-deployer`
5. Management console access는 주지 않는다.
6. 생성

### 권한 붙이기
가장 단순한 방법:
- 생성한 user 상세 화면으로 들어간다.
- `Permissions`
- `Add permissions`
- `Attach policies directly` 대신 `Create inline policy`를 선택한다.

### 추천 inline policy
아래 예시는 현재 workflow에 맞춘 최소 시작점이다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::worthyi-bucket-204098849975-ap-northeast-2-an"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::worthyi-bucket-204098849975-ap-northeast-2-an/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment",
        "codedeploy:GetApplication",
        "codedeploy:GetDeploymentGroup"
      ],
      "Resource": "*"
    }
  ]
}
```

### 추천 policy 이름
- `AllowGitHubActionsDeployToWorthyIDev`

### Access key 만들기
1. 생성한 user 상세 화면으로 들어간다.
2. `Security credentials`
3. `Create access key`
4. CLI 또는 일반 programmatic access에 해당하는 선택지로 진행한다.
5. 생성 후 아래 두 값을 즉시 복사한다.

```text
Access key ID
Secret access key
```

주의:
- secret access key는 생성 직후에만 다시 볼 수 있다.
- 잃어버리면 새 access key를 다시 만들어 GitHub secrets를 교체해야 한다.

### 이 값들을 어디에 넣나
- `AWS_ACCESS_KEY_ID_DEPLOYER = Access key ID`
- `AWS_SECRET_ACCESS_KEY_DEPLOYER = Secret access key`

### 중요
현재 repo의 workflow가 실제로 읽는 GitHub secrets는 아래 4개뿐이다.

```text
AWS_ACCESS_KEY_ID_DEPLOYER
AWS_SECRET_ACCESS_KEY_DEPLOYER
AWS_CODE_DEPLOY_APPLICATION
AWS_CODE_DEPLOY_GROUP
```

즉 아래 값들은 backend repo의 현재 workflow 기준으로는 GitHub에 안 넣어도 된다.
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `JWT_SECRET_KEY`

이 값들은 GitHub가 아니라 `/WorthyI/dev` Secrets Manager에 들어가야 한다.

---

## 4. EC2 인스턴스 만들기

현재 레포 스크립트는 `yum`, `redis6`, `/home/ec2-user` 경로를 전제로 한다.
따라서 Amazon Linux 계열이 가장 안전하다.

이건 AWS 공식 문서가 아니라 현재 레포를 보고 내린 추천이다.

### 추천 스펙
- Instance type: `t3.small` 또는 `t3.medium`
- OS: Amazon Linux 계열
- Subnet: public subnet 1개
- Auto-assign public IP: `Enable`
- Security group: `worthyi-dev-app-sg`
- IAM role: `worthyi-dev-ec2-role`

### 콘솔 클릭 순서
1. `EC2` 콘솔로 이동한다.
2. `Instances`
3. `Launch instances`
4. Name: `worthyi-dev-app-1`
5. AMI는 Amazon Linux 계열 선택
6. Instance type 선택
7. Key pair 선택 또는 생성
8. Network settings에서 아래를 설정

#### Network 설정
- VPC: `worthyi-main-vpc`
- Subnet: public subnet 하나 선택
- Auto-assign public IP: `Enable`
- Firewall: existing SG 선택
- Security group: `worthyi-dev-app-sg`

#### Advanced details
- IAM instance profile: `worthyi-dev-ec2-role`

9. `Launch instance`

### launch 후 확인
- instance state: `running`
- status checks: `2/2 checks passed`
- public IPv4 주소 존재
- IAM role attached

### role을 launch 후 붙여야 하는 경우
1. 인스턴스를 선택
2. `Actions -> Security -> Modify IAM role`
3. `worthyi-dev-ec2-role` 선택
4. 저장

---

## 5. RDS용 DB subnet group 만들기

RDS는 private subnet 2개 이상이 들어 있는 subnet group이 필요하다.

### 추천 이름
- `worthyi-shared-db-subnet-group`

### 콘솔 클릭 순서
1. `RDS` 콘솔로 이동한다.
2. 왼쪽 메뉴에서 `Subnet groups`
3. `Create DB subnet group`

### 입력값
- Name: `worthyi-shared-db-subnet-group`
- Description: `WorthyI shared DB subnet group`
- VPC: `worthyi-main-vpc`
- Availability Zones: 2개 모두 포함
- Subnets: private subnet 2개 선택

4. 생성

### 성공 기준
- subnet group 상태가 정상
- 서로 다른 AZ의 subnet 2개가 들어 있다

---

## 6. RDS PostgreSQL 만들기

dev는 일단 가장 단순하게 간다.

### 추천값
- Engine: PostgreSQL
- Template: dev/Free tier/Sandbox 중 비용 맞는 것
- DB identifier: `worthyi-dev-postgres`
- Initial DB name: `worthyi_dev`
- Public access: `No`
- VPC: `worthyi-main-vpc`
- DB subnet group: `worthyi-shared-db-subnet-group`
- Security group: `worthyi-dev-rds-sg`
- Multi-AZ: dev에서는 끄는 편이 보통 낫다

### 콘솔 클릭 순서
1. `RDS -> Databases`
2. `Create database`
3. `Standard create`
4. Engine options: `PostgreSQL`
5. Template는 free tier 또는 적당한 소형 구성

### Settings
- DB instance identifier: `worthyi-dev-postgres`
- Master username: 예: `worthyi`
- Master password: 직접 입력

### Connectivity
- Compute resource: `Don’t connect to an EC2 compute resource`
- VPC: `worthyi-main-vpc`
- DB subnet group: `worthyi-shared-db-subnet-group`
- Public access: `No`
- VPC security group: `worthyi-dev-rds-sg`
- Availability Zone은 자동 또는 원하는 AZ

### Additional configuration
- Initial database name: `worthyi_dev`

6. 생성

### 생성 후 꼭 기록할 값
- Endpoint
- Port
- DB name
- Master username

### 성공 기준
- DB status가 `Available`

---

## 7. Secrets Manager 만들기

### 가장 중요한 규칙
이 프로젝트의 dev secret 이름은 정확히 아래와 같아야 한다.

- `/WorthyI/dev`

앞의 `/`까지 포함이다.

### 콘솔 클릭 순서
1. `Secrets Manager` 콘솔로 이동한다.
2. `Store a new secret`
3. Secret type: `Other type of secret`
4. Plaintext 또는 key/value 입력 화면에서 secret 값을 넣는다.

### 추천 형식
JSON Plaintext로 넣는 것이 편하다.

예시 형태:

```json
{
  "JWT_SECRET_KEY": "replace-with-long-random-secret",
  "DB_URL": "your-rds-endpoint.ap-northeast-2.rds.amazonaws.com:5432/worthyi_dev",
  "DB_USERNAME": "worthyi",
  "DB_PASSWORD": "replace-with-db-password",
  "GOOGLE_CLIENT_ID": "replace",
  "GOOGLE_CLIENT_SECRET": "replace",
  "APPLE_CLIENT_ID": "replace",
  "APPLE_CLIENT_SECRET": "replace",
  "APPLE_KID": "replace",
  "APPLE_TID": "replace"
}
```

### secret 이름
- `/WorthyI/dev`

### rotation
- 지금은 `Off`

### 생성 후 바로 확인할 것
1. secret 상세 화면으로 들어간다.
2. `Retrieve secret value`
3. 키 이름이 정확한지 확인한다.

### DB_URL 형식 주의
현재 Spring 설정은 아래처럼 붙는다.

```text
jdbc:postgresql://${DB_URL}
```

즉 `DB_URL`에는 보통 아래처럼 넣어야 한다.

```text
<rds-endpoint>:5432/<database-name>
```

예:

```text
worthyi-dev-postgres.xxxxx.ap-northeast-2.rds.amazonaws.com:5432/worthyi_dev
```

### prod도 나중에 만들 경우
- `/WorthyI/prod`

prod 키 이름은 현재 코드 기준으로 아래가 다르다.
- `PROD_DB_URL`
- `PROD_DB_USERNAME`
- `PROD_DB_PASSWORD`

---

## 8. S3 버킷 만들기

현재 GitHub Actions workflow는 아래 버킷 이름을 사용한다.

- `worthyi-bucket-204098849975-ap-northeast-2-an`

### 선택지
1. 같은 이름을 쓸 수 있으면 그대로 만든다.
2. 이미 사용 중이거나 불가능하면 새 이름을 만든 뒤 workflow를 수정한다.

### 콘솔 클릭 순서
1. `S3` 콘솔로 이동한다.
2. `Create bucket`
3. Bucket name 입력
4. Region은 `ap-northeast-2`
5. 기본값으로 생성해도 된다

### 주의
- 버킷 이름을 바꾸면 `.github/workflows/deploy.yml`도 바꿔야 한다.

---

## 9. EC2에 CodeDeploy agent 설치

방법은 두 가지다.

1. Session Manager로 접속
2. SSH로 접속

처음엔 Session Manager가 더 편하다.
이를 위해 EC2 role에 `AmazonSSMManagedInstanceCore`를 붙여 두었다.

### 접속이 되는지 먼저 확인
1. `Systems Manager -> Session Manager`
2. 인스턴스가 보이면 세션 시작

### 설치 명령
리전이 서울이므로 CodeDeploy 리소스킷 버킷은 `aws-codedeploy-ap-northeast-2`다.

세션 안에서 아래 순서로 실행한다.

```bash
sudo yum update -y
sudo yum install -y ruby wget
cd /tmp
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install -O install
chmod +x install
sudo ./install auto
sudo systemctl start codedeploy-agent
sudo systemctl status codedeploy-agent --no-pager
```

주의:
- Session Manager로 접속하면 보통 `ssm-user`라서 `/home/ec2-user`보다 `/tmp`가 안전하다.
- 문서의 ```` ```bash ```` 줄은 터미널에 넣지 말고, 코드블록 안 명령어만 실행한다.
- `wget ... -O install` 은 한 줄 전체를 끊지 않고 실행해야 한다.

### 성공 기준
- `codedeploy-agent`가 running 상태

### 만약 안 뜨면
아래도 실행해 본다.

```bash
sudo service codedeploy-agent start
sudo service codedeploy-agent status
rpm -qa | grep codedeploy
sudo tail -n 50 /var/log/aws/codedeploy-agent/codedeploy-agent.log
```

---

## 10. EC2 태그 달기

CodeDeploy deployment group이 EC2를 찾게 하려면 태그가 필요하다.

### 추천 태그
- `Project = WorthyI`
- `Environment = dev`
- `DeployGroup = worthyi-back-dev`

### 콘솔 클릭 순서
1. `EC2 -> Instances`
2. 인스턴스 선택
3. `Tags` 탭
4. 위 태그 추가

### 성공 기준
- 태그가 인스턴스에 저장됨

---

## 11. CodeDeploy application 만들기

### 추천 이름
- `worthyi-back-dev`

### 콘솔 클릭 순서
1. `CodeDeploy` 콘솔로 이동한다.
2. `Applications`
3. `Create application`

### 입력값
- Application name: `worthyi-back-dev`
- Compute platform: `EC2/On-Premises`

4. 생성

---

## 12. CodeDeploy deployment group 만들기

### 추천 이름
- `worthyi-back-dev-group`

### 콘솔 클릭 순서
1. 방금 만든 application으로 들어간다.
2. `Create deployment group`

### 입력값
- Deployment group name: `worthyi-back-dev-group`
- Service role: `CodeDeployServiceRole`
- Deployment type: `In-place`
- Environment configuration: `Amazon EC2 instances`
- Key: `DeployGroup`
- Value: `worthyi-back-dev`

### 배포 설정
- Deployment configuration: `CodeDeployDefault.AllAtOnce`

이 값은 현재 GitHub Actions workflow와 맞는다.

### Load balancer 연결
처음엔 비워 두어도 된다.

이유:
- 현재 구조는 단일 인스턴스 배포고
- ALB는 이미 target group으로 이 인스턴스를 보고 있기 때문이다.
- 나중에 필요하면 배포 그룹에 로드밸런서를 연결해도 된다.

### 생성 후 확인
- deployment group이 만들어짐
- EC2 인스턴스가 태그 조건으로 잡힘

---

## 13. GitHub Actions secrets 넣기

백엔드 저장소에 아래 secrets를 넣는다.

### 필수 secrets
- `AWS_ACCESS_KEY_ID_DEPLOYER`
- `AWS_SECRET_ACCESS_KEY_DEPLOYER`
- `AWS_CODE_DEPLOY_APPLICATION`
- `AWS_CODE_DEPLOY_GROUP`

### 넣어야 할 값
- `AWS_ACCESS_KEY_ID_DEPLOYER = github-actions-deployer access key ID`
- `AWS_SECRET_ACCESS_KEY_DEPLOYER = github-actions-deployer secret access key`
- `AWS_CODE_DEPLOY_APPLICATION = worthyi-back-dev`
- `AWS_CODE_DEPLOY_GROUP = worthyi-back-dev-group`

### 중요
- 이 Access Key는 GitHub Actions가
- S3 업로드
- CodeDeploy create-deployment
를 할 수 있어야 한다.

### 현재 workflow가 안 쓰는 값
GitHub secrets 화면에 예전 값들이 남아 있을 수 있다.
하지만 현재 `.github/workflows/deploy.yml` 기준으로는 아래 4개만 실제로 참조한다.

```text
AWS_ACCESS_KEY_ID_DEPLOYER
AWS_SECRET_ACCESS_KEY_DEPLOYER
AWS_CODE_DEPLOY_APPLICATION
AWS_CODE_DEPLOY_GROUP
```

즉 아래 값들은 backend workflow 기준으로는 필수 아님:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `JWT_SECRET_KEY`
- `EC2_HOST`
- `EC2_USER`
- `EC2_KEY`

### 접근 권한 최소 필요
- S3 put object on deployment bucket
- CodeDeploy create deployment

---

## 14. 첫 배포 전 최종 점검

### 네트워크
- [ ] EC2는 public subnet에 있다
- [ ] EC2 role이 붙어 있다
- [ ] EC2 SG는 8080을 ALB SG에서만 받는다
- [ ] RDS SG는 5432를 app SG에서만 받는다
- [ ] RDS는 public access가 꺼져 있다

### 앱
- [ ] CodeDeploy agent가 running
- [ ] `/WorthyI/dev` secret이 존재한다
- [ ] secret 키 이름이 코드와 정확히 일치한다
- [ ] DB endpoint/username/password가 맞다

### 배포
- [ ] S3 bucket이 존재한다
- [ ] CodeDeploy application이 존재한다
- [ ] CodeDeploy deployment group이 존재한다
- [ ] EC2 태그가 deployment group 조건과 맞다
- [ ] GitHub Actions secrets가 등록되었다

---

## 15. 첫 배포 후 확인할 것

### 1. GitHub Actions
- workflow 성공 여부
- S3 업로드 성공 여부
- CodeDeploy create-deployment 성공 여부

### 2. CodeDeploy 콘솔
- deployment status가 `Succeeded`

### 3. EC2 내부
- 앱 프로세스가 떠 있는지
- Redis가 떠 있는지

### 4. ALB / Target Group
- target health가 `healthy`

### 5. 외부 테스트
- `https://api-dev.worthyilife.com/actuator/health`

성공 기준:
- HTTP 200

---

## 자주 막히는 지점

### 1. secret 이름을 `WorthyI/dev`로 만듦
틀린 예:
- `WorthyI/dev`

맞는 예:
- `/WorthyI/dev`

### 2. DB_URL 형식을 endpoint만 넣음
틀린 예:
- `db-host.rds.amazonaws.com`

맞는 예:
- `db-host.rds.amazonaws.com:5432/worthyi_dev`

### 3. EC2 role이 없어서 secret 읽기 실패
증상:
- 앱 부팅 중 설정 로딩 실패

### 4. EC2 role은 있는데 S3 읽기 권한이 없음
증상:
- CodeDeploy agent가 bundle 또는 리소스킷 접근 실패

### 5. RDS를 public으로 열어 버림
증상:
- 외부 노출 위험

dev라도 public access는 끄는 게 낫다.

### 6. ALB는 되는데 target이 unhealthy
증상:
- EC2 앱이 8080에서 안 떴거나
- SG가 틀렸거나
- health check path가 틀림

### 7. Redis를 별도 AWS 서비스로 만들려 함
현재 레포 기준으로는 우선 필요 없다.
현재 스크립트는 EC2 내부 Redis를 전제로 한다.

---

## 추천 진행 순서 요약
1. VPC 생성
2. 보안그룹 생성
3. IAM 역할 생성
4. EC2 생성
5. RDS subnet group 생성
6. RDS 생성
7. Secrets Manager 생성
8. S3 bucket 생성
9. CodeDeploy agent 설치
10. EC2 태그 추가
11. CodeDeploy application 생성
12. CodeDeploy deployment group 생성
13. GitHub Actions secrets 등록
14. deploy 실행
15. ALB와 Route53 테스트

---

## 공식 AWS 문서
- VPC 생성: https://docs.aws.amazon.com/vpc/latest/userguide/create-vpc.html
- Internet gateway와 인터넷 연결: https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html
- Route table 개념: https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
- EC2 인스턴스 launch: https://docs.aws.amazon.com/codedeploy/latest/userguide/instances-ec2-create.html
- EC2에 IAM role 붙이기: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/attach-iam-role.html
- Instance profile 개념: https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_use_switch-role-ec2_instance-profiles.html
- PostgreSQL DB 생성: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.PostgreSQL.html
- DB instance 일반 생성: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html
- DB subnet group 참고: https://docs.aws.amazon.com/AmazonRDS/latest/APIReference/API_CreateDBSubnetGroup.html
- Secrets Manager secret 생성: https://docs.aws.amazon.com/secretsmanager/latest/userguide/create_secret.html
- Secrets Manager 값 조회: https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets-console.html
- CodeDeploy service role 생성: https://docs.aws.amazon.com/codedeploy/latest/userguide/getting-started-create-service-role.html
- AWSCodeDeployRole 정책: https://docs.aws.amazon.com/aws-managed-policy/latest/reference/AWSCodeDeployRole.html
- EC2를 CodeDeploy용으로 설정: https://docs.aws.amazon.com/codedeploy/latest/userguide/instances-ec2-configure.html
- CodeDeploy agent 설치: https://docs.aws.amazon.com/codedeploy/latest/userguide/codedeploy-agent-operations-install-linux.html
- CodeDeploy resource kit bucket 참고: https://docs.aws.amazon.com/codedeploy/latest/userguide/resource-kit.html
- CodeDeploy application 생성: https://docs.aws.amazon.com/codedeploy/latest/userguide/applications-create-in-place.html
- CodeDeploy deployment group 생성: https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-groups-create.html
- CodeDeploy에서 EC2 태그 사용: https://docs.aws.amazon.com/codedeploy/latest/userguide/instances-tagging.html

## 이 프로젝트에서 참고할 로컬 파일
- backend AWS 의존성: `/Users/ho/IdeaProjects/worthyi-back/build.gradle`
- backend dev secret import: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- backend prod secret import: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`
- EC2 배포 스크립트: `/Users/ho/IdeaProjects/worthyi-back/scripts/setup.sh`
- EC2 의존성 설치 스크립트: `/Users/ho/IdeaProjects/worthyi-back/scripts/install_dependencies.sh`
- 서버 시작 스크립트: `/Users/ho/IdeaProjects/worthyi-back/scripts/start_server.sh`
- CodeDeploy spec: `/Users/ho/IdeaProjects/worthyi-back/appspec.yml`
- GitHub Actions 배포 workflow: `/Users/ho/IdeaProjects/worthyi-back/.github/workflows/deploy.yml`
