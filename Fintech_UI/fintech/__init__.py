#-*- coding: utf-8 -*-

import logging
from logging.handlers import RotatingFileHandler

from flask import Flask
app = Flask(__name__)

# load config by py file
app.config.from_pyfile('default_config.py')

# load logger
# handler = RotatingFileHandler('log/fintech.log', maxBytes=10000, backupCount=1)
# handler.setLevel(logging.DEBUG)
# app.logger.addHandler(handler)

# 
from fintech import views

