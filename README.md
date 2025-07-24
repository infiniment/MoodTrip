# MoodTrip 🌍  

감정 기반 여행지 추천 및 동행자 매칭 서비스

---

## 📌 Project Overview

MoodTrip은 감정 태그 기반으로 여행지를 추천하고, 유사한 감정을 가진 여행 동행자를 매칭하는 서비스입니다.  

- 사용자 감정 분석을 통한 **맞춤형 관광지 추천**
- **동행자 매칭**, **채팅**, **후기 작성** 등 다양한 기능
- 관리자와 고객센터를 통한 **안전한 서비스 운영**

---

## 🛠️ Tech Stack

### Backend

- **Java 17**, **Spring Boot 3.x**
- **JPA (Hibernate)**, **QueryDSL**
- **MariaDB**
- **Redis** (채팅/세션 관리)
- **Swagger** (API 문서화)
- **Kakao/Google API** (지도 및 외부 서비스 연동)

### Frontend (퍼블리싱)

- **HTML5, CSS3, JavaScript (Vanilla)**
- Spring Boot의 **Thymeleaf 템플릿** 기반

### Build & Deploy

- **Gradle**
- GitHub Flow 기반 협업

---

## 🌳 Project Structure

### Java Packages

```shell
src/main/java/com/moodTrip/spring
├── domain # 도메인별 주요 기능
│ ├── attraction
│ ├── chatting # controller, dto, entity, repository, service
│ ├── companion
│ ├── emotion
│ ├── member
│ ├── review
│ ├── room
│ └── support
├── global # 전역 설정 및 공통 유틸
│ ├── common
│ │ ├── dto
│ │ ├── entity
│ │ └── util
│ └── config # 전역 설정 (JPA, Security, Swagger 등)
├── infrastructure # 외부 연동
│ ├── external
│ │ ├── google
│ │ └── kakao
│ ├── tourism, transport, weather
│ └── scheduler
└── Application.java

```



### Resources

```shell
src/main/resources
├── static
│ ├── css
│ │ ├── customer-center, creatingRoom, ...
│ ├── js
│ └── image
└── templates
├── customer-center
├── creatingRoom
├── enteringRoom
├── ...
application.yml
application-local.yml (개인 설정, .gitignore에 포함)
```



---

## 🔀 Git Convention

### Branch Strategy (GitHub Flow)

- **main**: 배포 가능한 안정화 브랜치  
- **develop**: 기능 통합 브랜치 (MVP 이후 활성화 예정)  
- **feature**: `feature/{이슈번호}-{기능명}`  
  - 예) `feature/1-user-authentication`

---

## 📝 Commit Convention

커밋 메시지는 **제목 / 본문 / 꼬리말**로 나눕니다.

### 1) 제목

- **`Tag: 제목`** 형식 사용
- 첫 글자는 대문자, 콜론 뒤 한 칸 띄우기  
  예: `Feat: 로그인 기능 추가`

### 2) Tag 종류

| Tag      | 설명                  |
| -------- | --------------------- |
| Feat     | 새로운 기능 추가      |
| Fix      | 버그 수정             |
| Docs     | 문서 수정             |
| Style    | 코드 포맷/스타일 변경 |
| Refactor | 코드 리팩토링         |
| Test     | 테스트 코드 추가/수정 |
| Chore    | 빌드/설정 관련 작업   |
| Merge    | 브랜치 병합           |

### 3) 본문 (선택)

- **72자 내로 줄바꿈**
- 무엇을, 왜 변경했는지 상세히 작성

### 4) 꼬리말 (선택)

- `Type: #이슈번호` 형식  
  예: `Fixes: #12`

---

## 💻 Code Convention

### Java

- **클래스명**: PascalCase (`UserService`, `RoomController`)  
- **메서드/변수명**: camelCase (`getUserById`, `roomList`)  
- **상수**: UPPER_SNAKE_CASE (`MAX_LOGIN_ATTEMPTS`)  
- **패키지명**: 소문자 (`com.moodTrip.spring.chatting`)  
- **예외 클래스**: PascalCase + `Exception` (`UserNotFoundException`)

### Database

- **테이블/컬럼명**: snake_case  
- **PK 컬럼명**: `{table명}_id` (`user_id`, `room_id`)  
- **공통 컬럼**: `created_at`, `updated_at`  
- 파일(이미지)은 DB에 직접 저장하지 않고 **URL 또는 UUID**로 관리

---

## 🚀 Getting Started

### 1) Clone

```bash
git clone https://github.com/{your-repo}/moodTrip.git
cd moodTrip
```
