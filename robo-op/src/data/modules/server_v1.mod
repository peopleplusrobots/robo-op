MODULE Server_v1

    !***********************************************************
    !
    ! RAPID module to remotely send commands to an industrial robot from an external
    ! application.
    !
    ! Code adapted from open-abb-driver {https://github.com/robotics/open-abb-driver}
    !  
    !
    ! @authors mad & zack
    ! (mad) www.madlab.cc
    ! (zack)enartdezark.blogspot.com
    !
    !***********************************************************

    ! Setup default tool, work object, speed, and zone data
    PERS tooldata currTool:=[TRUE,[[0,0,0],[1,0,0,0]],[0.001,[0,0,0.001],[1,0,0,0],0,0,0]];
    PERS wobjdata currWobj:=[FALSE,TRUE,"",[[0,0,0],[1,0,0,0]],[[0,0,0],[1,0,0,0]]];
    PERS speeddata currSpeed:=[100,50,0,0];
    PERS zonedata currZone:=[FALSE,0.3,0.3,0.3,0.03,0.3,0.03];

    ! Setup start pose and position
    CONST jointtarget homePose:=[[0,0,0,0,0,0],[0,0,0,0,0,0]];
    CONST robtarget StartTarget:=[[0,0,0],[1,0,0,0],[-2,-1,0,0],[84.5,9E9,9E9,9E9,9E9,9E9]];

    ! Setup program variables
    VAR pos currPos;
    VAR robtarget currTarg;

    ! Setup communication variables
    VAR socketdev temp_socket;
    VAR socketdev client_socket;
    VAR bool connected:=FALSE;

    VAR string msg_received;
    VAR bool listen:=TRUE;
    

    PROC Main()

        ConfL\Off;
        ConfJ\Off;

        currPos:=CPos();
        currTarg:=CRobT();

        ! Connect Server to Client
        ConnectToClient;
        connected:=TRUE;

        WHILE listen DO

            !SocketSend client_socket\Str:="ready for msg, Computer!\0D\0A";
            ! SocketReceive blocks until it gets a msg from the client
            SocketReceive client_socket\Str:=msg_received;
            TPWrite "Incoming Msg:  "+msg_received;
            ! Verify to client that we received the message
            SocketSend client_socket\Str:="received msg\0D\0A";

            ParseMsg msg_received;

            ! clear the received message
            msg_received:="";

        ENDWHILE

        SocketSend client_socket\Str:="Closing Server Socket\0D\0A";
        TPWrite "Closing Server Socket";
        SocketClose temp_socket;

    ENDPROC

    PROC ConnectToClient()
        VAR string clientIP;

        SocketCreate temp_socket;
        SocketBind temp_socket,"128.2.109.111",1025;
        SocketListen temp_socket;
        SocketAccept temp_socket,client_socket\ClientAddress:=clientIP;

        TPWrite "SERVER: Connected to IP "+clientIP;
    ENDPROC

    PROC ParseMsg(string msg)
        VAR num msgLength;
        VAR bool badMsg:=FALSE;
        VAR bool done:=FALSE;
        VAR string key;
        VAR string val;
        VAR num split;
        VAR num dataType;

        ! Check that we received a full message
        msgLength:=StrMatch(msg,1,";");
        IF msgLength>StrLen(msg) THEN
            badMsg:=TRUE;
        ENDIF

        IF badMsg=TRUE THEN
            TPWrite "corrupt or incomplete message";
            TPWrite msg;
        ELSE
            split:=StrMatch(msg,1,"/");
            key:=StrPart(msg,1,split-1);
            TPWrite "key: "+key;
            val:=StrPart(msg,split+1,msgLength-split-1);
            TPWrite "val: "+val;

            WHILE done=FALSE DO

                ! Check which data type we are trying to parse

                ! POINTS modify the current robtarget
                dataType:=StrMatch(key,1,"point");
                IF dataType<StrLen(key) THEN
                    TPWrite "WE HAVE A POINT!";
                    moveRobTarget(val);
                    done:=TRUE;
                ENDIF

                ! FLAGS change a program state
                dataType:=StrMatch(key,1,"flag");
                IF dataType<StrLen(key) THEN

                    dataType:=StrMatch(val,1,"exit");
                    IF dataType<StrLen(key) THEN
                        listen:=FALSE;
                    ENDIF

                    done:=TRUE;
                ENDIF

                ! OFFSETS move the robot relative to a tool position
                dataType:=StrMatch(key,1,"offset");
                IF dataType<StrLen(key) THEN
                    moveRelTool(val);
                    done:=TRUE;
                ENDIF

            ENDWHILE

        ENDIF
        
    ENDPROC

    PROC moveRelTool(String val)
        VAR num coords{6};

        IF StrToVal(val,coords) THEN
            MoveJ RelTool(currTarg,coords{1},coords{2},coords{3}\Rx:=coords{4}\Ry:=coords{5}\Rz:=coords{6}),currSpeed,currZone,currTool;
            currPos:=CPos();
            currTarg:=CRobT();

        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC

    PROC moveRobTarget(String val)
        VAR num coords{6};
        VAR robtarget target:=[[0,0,0],[1,0,0,0],[0,0,0,0],[9E9,9E9,9E9,9E9,9E9,9E9]];
        VAR jointtarget testJoint;
        VAR robtarget testTarget;

        IF StrToVal(val,coords) THEN
            target.trans:=[coords{1},coords{2},coords{3}];
            target.rot:=OrientZYX(coords{6},coords{5},coords{4});
            testJoint:=CJointT();
            testTarget:=CalcRobT(testJoint,currTool);
            target.robconf:=testTarget.robconf;

            MoveJ target,currSpeed,currZone,currTool;

            currPos:=CPos();
            currTarg:=CRobT();
        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC



ENDMODULE