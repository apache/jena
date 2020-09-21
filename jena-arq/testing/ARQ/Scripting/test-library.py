barx = '||'
def bar(x, y):
    return barx + combine(x, y) + barx
def value(n):
    return n.value
def combine(x, y):
    try:
        return x + y
    except:
        return str(x) + str(y)
def identity(x):
    return x
def rtnBoolean():
    return True
def rtnString():
    return "foo"
def rtnInteger():
    return 57
def rtnDouble():
    return 5.7
def rtnUndef():
    return None
def rtnNull():
    return None