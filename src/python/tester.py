__author__ = 'pascalpoizat'

import sys

if __name__ == '__main__':
    if(len(sys.argv)<>2):
        print "usage : %s <argument>" % sys.argv[0]
    else:
        f = open("barfoo.pif",'w')
        f.write("test")
        f.close()

