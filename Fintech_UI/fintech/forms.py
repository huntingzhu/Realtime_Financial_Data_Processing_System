# coding: utf-8

"""
    forms.py
    ~~~~~~~~
"""
from flask_wtf import FlaskForm
from flask_wtf.file import FileField, FileAllowed, FileRequired
from wtforms import IntegerField, StringField, SubmitField, PasswordField, BooleanField
from wtforms.validators import Required, EqualTo

class FeatureForm(FlaskForm):
    """ Feature Form"""
    member_id = StringField('Member')
    loan_amnt = StringField('Loan Amount')
    int_rate = StringField('Rate')
    installment = StringField('Installment')
    totalCreditLimit = StringField('totalCreditLimit')
    bc_open_to_buy = StringField('bc_open_to_buy')
    dti=StringField('dti')
    annual_inc = StringField('annual_inc')
    bc_util = StringField('bc_util')
    int_rate = StringField('int_rate')
    installment =StringField('installment')
    term=StringField('term')
    loan_amnt=StringField('loan_amnt')
    fund_rate=StringField('fund_rate')
    funded_amnt=StringField('funded_amnt')
    grade=StringField('grade')
    go = SubmitField('Submit')

class UploadForm(FlaskForm):
    DataFile = FileField('Your data file', validators=[
        FileRequired(),
        FileAllowed(['csv', 'txt'], 'plain text file only!')
    ])
