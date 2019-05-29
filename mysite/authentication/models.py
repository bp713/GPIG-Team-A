from django.db import models

# Create your models here.


class Courier(models.Model):
    email = models.CharField(max_length = 50)
    cred_id = models.BinaryField()
    cred_pub_key = models.BinaryField()
    registration_challenge = models.CharField(max_length = 50, default = '')
    authentication_challenge = models.CharField(max_length = 50, default = '')
    session_key = models.CharField(max_length = 70, default = '')
    one_time_key = models.CharField(max_length = 70, default = '')
