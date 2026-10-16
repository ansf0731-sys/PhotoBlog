from django.conf import settings
from django.db import models

class Post(models.Model):
    author = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, related_name="posts")
    title = models.CharField("제목", max_length=120)
    text = models.TextField("본문", blank=True)
    image = models.ImageField("이미지", upload_to="blog_image/%Y/%m/%d/", blank=True)
    created_date = models.DateTimeField("작성일", auto_now_add=True)
    published_date = models.DateTimeField("게시일", auto_now=True)

    class Meta:
        ordering = ["-published_date"]

    def __str__(self):
        return self.title
