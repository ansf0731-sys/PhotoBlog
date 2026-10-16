from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import BlogImages

router = DefaultRouter()
router.register("Post", BlogImages, basename="post")
urlpatterns = [path("", include(router.urls))]
