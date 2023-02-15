
CLIENT_SRCS=SFSAClient.java sfsalib/SFSAFileServer.java sfsalib/SFSAFilesWnd.java \
            sfsalib/SFSAGUIClient.java sfsalib/SFSAMenu.java sfsalib/SFSAMessage.java \
            sfsalib/SFSAOptionDialogThread.java sfsalib/SFSASendRecvBtns.java \
            sfsalib/SFSAStatusBar.java

all : SFSAClient.class

run :
	java -Djava.net.preferIPv4Stack=true SFSAClient

SFSAClient.class : $(CLIENT_SRCS)
	javac $^

clean :
	- rm sfsalib/*.class
	- rm *.class


