# Generated by Django 2.2.1 on 2019-05-23 12:12

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Courier',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('email', models.CharField(default='', max_length=50)),
                ('cred_id', models.BinaryField()),
                ('cred_pub_key', models.BinaryField()),
                ('registration_challenge', models.CharField(max_length=50)),
                ('authentication_challenge', models.CharField(max_length=50)),
            ],
        ),
    ]
