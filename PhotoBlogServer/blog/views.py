from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from rest_framework import viewsets, permissions
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from .models import Post
from .serializers import PostSerializer

class BlogImages(viewsets.ModelViewSet):
    queryset = Post.objects.select_related("author").all()
    serializer_class = PostSerializer
    parser_classes = [MultiPartParser, FormParser, JSONParser]

    def get_permissions(self):
        # 목록과 상세 조회는 공개하고, 생성/수정/삭제는 로그인 사용자에게만 허용합니다.
        if self.action in ["list", "retrieve"]:
            return [permissions.AllowAny()]
        return [permissions.IsAuthenticated()]

    def perform_create(self, serializer):
        serializer.save(author=self.request.user)

    def perform_update(self, serializer):
        serializer.save(author=self.request.user)

def post_list(request):
    posts = Post.objects.select_related("author").all()
    return render(request, "blog/post_list.html", {"posts": posts})

def post_new(request):
    if request.method == "POST":
        if not request.user.is_authenticated:
            return redirect("/admin/login/?next=/post/new/")
        title = request.POST.get("title", "").strip()
        text = request.POST.get("text", "").strip()
        image = request.FILES.get("image")
        if title:
            Post.objects.create(author=request.user, title=title, text=text, image=image)
            return redirect("/")
    return render(request, "blog/post_new.html")
