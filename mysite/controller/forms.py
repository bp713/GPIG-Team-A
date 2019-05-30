from django import forms

class RouteForm(forms.Form):
    start_lat = forms.CharField(label='Start Lattitude', max_length=100)   
    start_long = forms.CharField(label='Start Longitiude', max_length=100)
    end_lat = forms.CharField(label='End Lattitude', max_length=100)
    end_long = forms.CharField(label='End Longitiude', max_length=100)
    courier_id = forms.CharField(label='Courier ID', max_length=100)
