CC := javac
SRCDIR := src
BUILDDIR := build
SRCEXT := java

SOURCES := $(shell find $(SRCDIR) -name "*.$(SRCEXT)")

all:
	$(CC) $(SOURCES) -d $(BUILDDIR)
	
clean:
	rm -r $(BUILDDIR)/*
