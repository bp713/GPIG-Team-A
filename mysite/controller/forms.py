from django import forms

class RouteForm(forms.Form):
    start_location = forms.CharField(label='Start Location', max_length=100)
    end_location = forms.CharField(label='End Location', max_length=100)
    courier_id = forms.CharField(label='Courier ID', max_length=100)
