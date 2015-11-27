# class is there to demonstrate the use of unit testing in python
# to be removed later on
# for information, see:
# http://pyunit.sourceforge.net/pyunit.html
# https://confluence.jetbrains.com/display/PYH/Creating+and+running+a+Python+unit+test
# https://www.youtube.com/watch?v=_57f3HR-fF8&feature=youtu.be


class Adder:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __call__(self):
        return self.x + self.y
