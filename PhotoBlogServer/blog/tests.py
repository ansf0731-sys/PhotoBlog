
from io import BytesIO

from django.contrib.auth import get_user_model
from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import TestCase
from django.urls import reverse
from PIL import Image
from rest_framework.test import APIClient

from .models import Post


class PhotoBlogAPITest(TestCase):
    def setUp(self):
        self.client = APIClient()
        User = get_user_model()
        self.user = User.objects.create_user(
            username="testuser",
            password="test-password-123"
        )

    def test_게시물_목록_조회(self):
        Post.objects.create(
            author=self.user,
            title="테스트 게시물",
            text="테스트 본문"
        )

        response = self.client.get("/api_root/Post/")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]["title"], "테스트 게시물")

    def test_인증_없이_게시물_생성_차단(self):
        response = self.client.post(
            "/api_root/Post/",
            {
                "title": "권한 없음",
                "text": "본문"
            }
        )

        self.assertEqual(response.status_code, 401)

    def test_인증_사용자_게시물_생성(self):
        self.client.force_authenticate(user=self.user)

        response = self.client.post(
            "/api_root/Post/",
            {
                "title": "새 게시물",
                "text": "새 본문"
            }
        )

        self.assertEqual(response.status_code, 201, response.data)
        self.assertEqual(Post.objects.count(), 1)

    def test_이미지_업로드(self):
        self.client.force_authenticate(user=self.user)

        이미지_버퍼 = BytesIO()
        Image.new("RGB", (1, 1), color="white").save(
            이미지_버퍼,
            format="PNG"
        )
        이미지_버퍼.seek(0)

        이미지 = SimpleUploadedFile(
            "test.png",
            이미지_버퍼.read(),
            content_type="image/png"
        )

        response = self.client.post(
            "/api_root/Post/",
            {
                "title": "사진 게시물",
                "text": "사진 업로드 테스트",
                "image": 이미지,
            },
            format="multipart"
        )

        self.assertEqual(response.status_code, 201, response.data)
        self.assertTrue(response.data["image"])
