JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	SubsetRecord.java \
	helper.java \
	project2.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

# JFLAGS = -g
# JC = javac
# .SUFFIXES: .java .class
# .java.class:
# 	$(JC) $(JFLAGS) $*.java

# CLASSES = *.java
# default: classes


# classes: $(CLASSES:.java=.class)

# clean:
# 	$(RM) *.class
