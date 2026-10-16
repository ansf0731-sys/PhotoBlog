# 포토 블로그 프로젝트

## 구성
- `PhotoBlogServer/`: Django REST API 서버
- `PhotoViewer/`: Android Java 클라이언트
- `모바일웹서비스 프로젝트 공통평가 01_수행 결과 보고서.docx`: 캡처 이미지 자리표시자가 포함된 보고서

## 1. 서버 실행 (Miniconda Prompt)
```bash
conda create -n photoblog python=3.11 -y
conda activate photoblog
cd PhotoBlogServer
python -m pip install -r requirements.txt
python manage.py makemigrations blog
python manage.py migrate
python manage.py seed_demo
python manage.py runserver 0.0.0.0:8000
```
브라우저: `http://127.0.0.1:8000/`  
API 목록: `http://127.0.0.1:8000/api_root/Post/`  
관리자 생성: `python manage.py createsuperuser`

## 2. 자동 테스트 및 커버리지
새 터미널에서 같은 환경을 활성화하고 `PhotoBlogServer` 폴더에서:
```bash
coverage run manage.py test
coverage html
```
보고서: `PhotoBlogServer/htmlcov/index.html`

## 3. Android 실행
Android Studio에서 `PhotoViewer` 폴더를 엽니다. Gradle 동기화 후 에뮬레이터를 실행합니다.
- 에뮬레이터 서버 주소: `http://10.0.2.2:8000`
- 실제 Android 기기: `MainActivity.java`의 `SERVER_URL`을 PC의 LAN IP로 변경하고 PC 방화벽의 8000 포트를 허용합니다.
- API 토큰은 서버의 `seed_demo` 명령이 생성하는 데모 계정 토큰을 앱에 설정합니다. 로컬 과제용 기본값은 `photoblog-demo-token`입니다.

## 4. 업로드 기능
앱에서 `새 게시물 작성`을 누르고 제목, 본문, 이미지 파일을 선택한 뒤 업로드할 수 있습니다. `동기화`를 누르면 서버 게시물 목록과 이미지가 표시됩니다.

## 5. 주의
- 로컬 테스트용 설정입니다. 공개 배포 전에는 `DEBUG=False`, 강력한 `SECRET_KEY`, HTTPS, 적절한 인증/권한 설정을 적용하세요.
- PythonAnywhere 배포는 계정 생성, WSGI 설정, 정적/미디어 파일 설정이 필요합니다. 배포 후 앱의 `SERVER_URL`을 실제 HTTPS 주소로 변경하세요.
- 보고서의 `[캡처 삽입: ...]` 영역은 실제 실행 화면으로 교체해야 합니다. 테스트하지 않은 항목을 완료로 표시하지 마세요.
