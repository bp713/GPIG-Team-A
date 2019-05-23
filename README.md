# GPIG-Team-A
Overleaf formative report: https://www.overleaf.com/project/5cbefaba0e7ba7374661db6f

Overleaf final report: https://www.overleaf.com/project/5ccb48f3ea7fef0235b2a501

## Client
The client folder contains an android studio project for the courier client side application

## Server
The Server folder contains python code to create routes\
requires: https://github.com/graphhopper/directions-api-clients/tree/master/python

## mysite
The mysite folder contains the django server.

Run the http debug server with `python3 manage.py runserver 0:8000`

Run the https server with `./run.py`\
requires:
 - eventlet (GitHub version)
   - install via `pip3 install -U https://github.com/eventlet/eventlet/archive/master.zip --user`
 - django
   - install via `pip3 install django --user`
 - pywarp
   - install via `pip3 install -U https://github.com/pyauth/pywarp/archive/master.zip --user`
