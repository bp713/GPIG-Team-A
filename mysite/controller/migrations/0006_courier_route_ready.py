# Generated by Django 2.2.1 on 2019-05-30 13:01

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('controller', '0005_auto_20190529_1815'),
    ]

    operations = [
        migrations.AddField(
            model_name='courier',
            name='route_ready',
            field=models.BooleanField(default=False),
        ),
    ]
