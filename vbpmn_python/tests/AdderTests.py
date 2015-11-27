# class is there to demonstrate the use of unit testing in python
# to be removed later on
# for information, see:
# http://pyunit.sourceforge.net/pyunit.html
# https://confluence.jetbrains.com/display/PYH/Creating+and+running+a+Python+unit+test
# https://www.youtube.com/watch?v=_57f3HR-fF8&feature=youtu.be

import unittest
from Adder import Adder


class MyTestCase(unittest.TestCase):
    def setUp(self):
        self.object = Adder(2, 3)

    def tearDown(self):
        self.object = None

    def test_creation(self):
        self.assertEquals(self.object.x, 2)
        self.assertEquals(self.object.y, 3)

    def test_addition1(self):
        self.assertEqual(self.object(), 5)

    # def test_addition2(self):
    #     self.assertEqual(self.object(), 6)


if __name__ == '__main__':
    unittest.main()
