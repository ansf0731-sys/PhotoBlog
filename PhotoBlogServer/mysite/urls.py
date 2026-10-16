from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from blog.views import post_list, post_new

urlpatterns = [
    path("admin/", admin.site.urls),
    path("", post_list, name="post_list"),
    path("post/new/", post_new, name="post_new"),
    path("api_root/", include("blog.urls")),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
