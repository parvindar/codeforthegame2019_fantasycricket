from django import template

register = template.Library()

@register.filter(name='filterKey')
def filterKey(myDict, key):
    return myDict[key]