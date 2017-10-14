# -*- coding: utf-8 -*-
import os
from . import app
from flask import (
    render_template,
    request)
from flask.views import View
from .forms import UploadForm, FeatureForm
from werkzeug.utils import secure_filename

import json
import requests
import kafka
from kafka import KafkaClient, SimpleProducer

@app.route('/')
def index():
    # app.logger.debug('Debug test')
    return render_template('welcome.html')


# handler for upload data file
@app.route('/upload/',methods=('GET', 'POST'))
def upload():
    form = UploadForm()
    app.logger.debug('upload form')

    if form.is_submitted():
        filename = secure_filename(form.DataFile.data.filename)
        strDir = os.getcwd()
        filepath = os.path.join(strDir, 'uploads', filename)
        app.logger.debug(filepath)
        form.DataFile.data.save(filepath)
        # take the data file, call model to process data
        #fin = Fintech()
        # res = 0.88
        #res = fin.run(filepath)

        # if it take a long time, here show the calc progress window...
        # in progress page, js continous checking the calculate progress, if it's completed, redirect to result.html
        return render_template('result.html')
    else:
        filename = None
    return render_template('upload.html', form=form, filename=filename)

# handler for form
@app.route('/form/', methods=('GET', 'POST'))
def form():
    form = FeatureForm()
    Datas = {}

    if form.is_submitted():
        Datas['member_id']=form.member_id.data
    	Datas['bc_open_to_buy']=form.bc_open_to_buy.data
     	Datas['total_il_high_credit_limit']  =form.totalCreditLimit.data
    	Datas['dti']=form.dti.data
    	Datas['annual_inc']=form.annual_inc.data
    	Datas['bc_util']=form.bc_util.data
        Datas['int_rate'] = form.int_rate.data
        Datas['installment'] = form.installment.data
    	Datas['term']=form.term.data
    	Datas['loan_amnt'] = form.loan_amnt.data
    	Datas['fund_rate']=form.fund_rate.data
     	Datas['funded_amnt']=form.funded_amnt.data
        Datas['grade']=form.grade.data

        data = {
            "member_id" : str(Datas['member_id']),
        	"bc_open_to_buy" : str(Datas['bc_open_to_buy']),
        	"total_il_high_credit_limit" : str(Datas['total_il_high_credit_limit']),
        	"dti" : str(Datas['dti']),
        	"annual_inc" : str(Datas['annual_inc']),
        	"bc_util" : str(Datas['bc_util']),
        	"int_rate" : str(Datas['int_rate']),
        	"term" : str(Datas['term']),
        	"loan_amnt" : str(Datas['loan_amnt']),
        	"fund_rate" : str(Datas['fund_rate']),
        	"funded_amnt" : str(Datas['funded_amnt']),
            "grade" : str(Datas['grade'])
    	}
    #for line in r.iter_lines():
	kafka = KafkaClient('localhost:9092')

	producer = SimpleProducer(kafka)
    	producer.send_messages('fintech-lendingclub',json.dumps(data))
    	return "success"

    return render_template('form.html', form=form)

@app.route('/pipline/')
def trigger():
    kafka = KafkaClient('localhost:9092')

    producer = SimpleProducer(kafka)

    r = requests.get("http://fintech.dataapplab.com:33334/api/v1.0/FinTech/streamingdata")
    print r
    producer.send_messages('fintech-lendingclub',r.content)
    data = {
      "bc_open_to_buy" : 0,
      "total_il_high_credit_limit" : 0,
      "dti" : 2009,
      "annual_inc" : 12000,
      "bc_util" : 0,
      "int_rate" : 10.08,
      "term" : 36,
      "loan_amnt" : 3500,
      "fund_rate" : 0,
      "funded_amnt" : 3500,

    }
    #for line in r.iter_lines():
    producer.send_messages('fintech-lendingclub',json.dumps(data))
    return "success"
        #print type(line)

    kafka.close()

# endpoint page
@app.route('/result/')
def submit():
    return render_template('result.html')

@app.route('/about')
def about():
    return 'The about page'

# below method just for test
@app.route('/user/<name>')
def show_user_profile(name):
    return 'User %s name' % name

@app.route('/post/<int:post_id>')
def show_post(post_id):
    return 'Post %d' % post_id
