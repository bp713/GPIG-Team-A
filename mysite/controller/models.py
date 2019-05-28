from django.db import models

# Create your models here.
class Controller(models.Model):
    name = models.CharField(max_length = 50)

    def __str__(self):
        return self.name

class Courier(models.Model):
    controller = models.ForeignKey(Controller, on_delete=models.CASCADE)
    location = models.CharField(max_length = 50, default = '')
    longitude = models.FloatField(default=0)
    latitude = models.FloatField(default=0)


class Route(models.Model):
    courier =  models.OneToOneField(
        Courier,
        on_delete=models.CASCADE,
        primary_key=True,
    )


class RouteComponent(models.Model):
    route = models.ForeignKey(Route, on_delete=models.CASCADE)
    json = models.TextField()
    position = models.IntegerField()

