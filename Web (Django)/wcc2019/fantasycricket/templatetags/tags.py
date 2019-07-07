from django import template

register = template.Library()

@register.filter(name='filterKey')
def filterKey(myDict, key):
    return myDict[key]

@register.filter(name='filtercut')
def filtercut(myDict, key):
    if " " in myDict[key]:
        #print('arrey bc')
        arr = myDict[key].split(' ')
        print(arr)
        s=''
        for i in arr:
            s += i[0]
            if len(i)>0 and (i[1]>='A' and i[1]<='Z'):
                s+=i[1]
        return s.upper()
    return (myDict[key][0:3]).upper()