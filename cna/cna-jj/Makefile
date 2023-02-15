#
#   CNA Client/Server (Java-Java) Makefile
#
#   NOTE: Use GNU make
#
#   Edward Hess
#

#
#   Variables
#
JC=javac

CLIENT_SRCS=SFSAClient.java sfsalib/SFSAMessage.java sfsalib/SFSAFileServer.java

SERVER_SRCS=SFSAServer.java sfsalib/SFSAEFile.java sfsalib/SFSAEFiles.java \
            sfsalib/SFSAClientProcessor.java sfsalib/SFSAMessage.java \
            sfsalib/SFSAEFileEnumerator.java

#
#   Targets (phony)
#
all : SFSAClient.class SFSAServer.class

clean :
	rm sfsalib/*.class
	rm *.class

SFSAClient.class : $(CLIENT_SRCS)
	$(JC) $^

SFSAServer.class : $(SERVER_SRCS)
	$(JC) $^

.PHONY: all clean
