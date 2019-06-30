from . import views
from django.urls import path

app_name = 'fantasycricket'

urlpatterns = [
    path('FAQ/', views.FAQ, name='FAQ'),
    path('AboutUs', views.AboutUs, name='AboutUs'),
    
    path('', views.home, name='home'),
    path('register/', views.reg, name='register'),
    path('login/', views.login, name='login'),
    path('logout/', views.logout, name='logout'),
    path('profile/', views.profile, name='profile'),
    path('matches/', views.matches, name='matches'),
    path('contests/', views.contests, name='contests'),
    path('contests/SelectPlayers/', views.contestSelectPlayers, name='contestSelectPlayers'),
    
    path('myMatches/', views.myMatches, name='myMatches'),
    path('myContests/', views.myContests, name='myContests'),
    path('myContestStatus/', views.myContestStatus, name='myContestStatus'),
    path('leaderboard/', views.leaderboard, name='leaderboard'),
    
    ## ADMIN
    path('admin/superProfile/', views.superProfile, name='superProfile'),
    
    ##  paytm
    path('paytm/checkout/', views.checkout, name='checkout'),
    path('paytm_extra/', views.paytm_extra, name='paytm_extra'),
    path('payment_success/', views.payment_success, name='payment_success'),
]