# Generated by Django 2.2.1 on 2019-05-19 13:50

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('controller', '0002_courier_location'),
    ]

    operations = [
        migrations.AddField(
            model_name='courier',
            name='latitude',
            field=models.FloatField(default=0),
        ),
        migrations.AddField(
            model_name='courier',
            name='longitude',
            field=models.FloatField(default=0),
        ),
    ]
