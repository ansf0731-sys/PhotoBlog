from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model
from rest_framework.authtoken.models import Token

class Command(BaseCommand):
    help = "과제 확인용 데모 계정과 API 토큰을 생성합니다."

    def handle(self, *args, **options):
        user_model = get_user_model()
        user, created = user_model.objects.get_or_create(username="demo")
        if created:
            user.set_password("demo-pass-123")
            user.save()
        token, _ = Token.objects.get_or_create(user=user)
        self.stdout.write(self.style.SUCCESS(f"데모 계정: demo / demo-pass-123"))
        self.stdout.write(self.style.SUCCESS(f"API 토큰: {token.key}"))
        self.stdout.write("Android MainActivity.java의 API_TOKEN에 위 토큰을 복사하세요.")
