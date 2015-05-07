MODULE Server_6640

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
    VAR speeddata currSpeed:=[100,50,0,0];
    VAR zonedata currZone:=z0;

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

            ! parse the message received from the client
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
        SocketBind temp_socket,"128.2.109.20",1025;
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
        VAR num pin;
        VAR string msgSend;

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

                ! JOINT modifies the Position and Orientation (MoveJ)
                dataType:=StrMatch(key,1,"joint");
                IF dataType<StrLen(key) THEN
                    moveTo(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
                
                ! OFFSET moves the robot relative to a tool position (MoveJ)
                dataType:=StrMatch(key,1,"offset");
                IF dataType<StrLen(key) THEN
                    moveRelTool(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
                
                ! POS modifies the Position of the robot (MoveL)
                dataType:=StrMatch(key,1,"pos");
                IF dataType<StrLen(key) THEN
                    updatePosition(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
                
                ! ORIENT modifies the Orient of the robot (MoveL)
                dataType:=StrMatch(key,1,"orient");
                IF dataType<StrLen(key) THEN
                    updateOrientation(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
                
                !CONFIG modifies the robot's configuration (MoveJ)
                dataType:=StrMatch(key,1,"config");
                IF dataType<StrLen(key) THEN
                    updateConfig(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
                
                !EXTAX modifies external axes (MoveL)
                dataType:=StrMatch(key,1,"extax");
                IF dataType<StrLen(key) THEN
                    updateExternalAxes(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF
              

                ! ZONE sets a new zone for the robot's movements
                dataType:=StrMatch(key,1,"zone");
                IF dataType<StrLen(key) THEN
                    IF StrToVal(val,currZone) THEN
                    ENDIF
                    done:=TRUE;
                    GOTO end;
                ENDIF

                ! SPEED sets a new zone for the robot's movements
                dataType:=StrMatch(key,1,"speed");
                IF dataType<StrLen(key) THEN
                    IF StrToVal(val,currSpeed) THEN
                    ENDIF
                    done:=TRUE;
                    GOTO end;
                ENDIF

                ! DO sets digital out for a given pin
                dataType:=StrMatch(key,1,"DO");
                IF dataType<StrLen(key) THEN                   
                    swapIO(val);
                    done:=TRUE;
                    GOTO end;
                ENDIF

                ! WAIT pauses the program until a given signal
                dataType:=StrMatch(key,1,"wait");
                IF dataType<StrLen(key) THEN 

                    dataType:=StrMatch(val,1,"InPos");
                    IF dataType<StrLen(val) THEN                  
                        WaitRob \InPos;
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    ! Add more wait types below

                ENDIF 

                ! FLAG changes a program state
                dataType:=StrMatch(key,1,"flag");
                IF dataType<StrLen(key) THEN

                    dataType:=StrMatch(val,1,"exit");
                    IF dataType<StrLen(val) THEN
                        listen:=FALSE;
                        done:=TRUE;
                        GOTO end;
                    ENDIF
                    
                    ! Add more flags below
                    
                ENDIF

                ! QUERY asks the robot to send information back to the computer
                dataType:=StrMatch(key,1,"query");
                IF dataType<StrLen(key) THEN

                    dataType:=StrMatch(val,1,"pos");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currTarg.trans);
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    dataType:=StrMatch(val,1,"orient");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currTarg.rot);
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    dataType:=StrMatch(val,1,"config");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currTarg.robconf);
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    dataType:=StrMatch(val,1,"extax");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currTarg.extax);
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    dataType:=StrMatch(val,1,"speed");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currSpeed);
                        TPWrite "speed: "+msgSend;
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                    dataType:=StrMatch(val,1,"zone");
                    IF dataType<StrLen(val) THEN
                        msgSend:=ValToStr(currZone);
                        TPWrite "zone: "+msgSend;
                        SocketSend client_socket\Str:=msgSend+"\0D\0A";
                        done:=TRUE;
                        GOTO end;
                    ENDIF

                ENDIF

                end:

                ! if we haven't found an appropriate command, send a message to the computer
                ! that they're sending the wrong key
                IF done=FALSE THEN
                    TPWrite "key ["+key+"] could not be found";
                    SocketSend client_socket\Str:="key ["+key+"] could not be found"+"\0D\0A";
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
    
    
    
    PROC swapIO(String val)     
        VAR num io;
        IF StrToVal(val,io) THEN

             IF io = 2 THEN
                InvertDO D651_11_DO6;
                TPWrite "Sent do"+val+" to "\Num:=DOutput(D651_11_DO6);
            ENDIF

        ENDIF   
    ENDPROC
    


    PROC moveTo(String val)
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

     PROC updatePosition(String val)
        VAR pos position;

        IF StrToVal(val,position) THEN
            currTarg.trans:=position;
            MoveJ currTarg,currSpeed,currZone,currTool;
            currPos:=CPos();
            currTarg:=CRobT();
        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC
    
     PROC updateOrientation(String val)
        VAR num rots{3};

        IF StrToVal(val,rots) THEN        
            currTarg.rot:=OrientZYX(rots{3},rots{2},rots{1});
            MoveL currTarg,currSpeed,currZone,currTool;
            currPos:=CPos();
            currTarg:=CRobT();
        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC
    
    PROC updateConfig(String val)
        VAR confdata config;

        IF StrToVal(val,config) THEN
            currTarg.robconf:=config;
            MoveJ currTarg,currSpeed,currZone,currTool;
            currPos:=CPos();
            currTarg:=CRobT();
        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC

    PROC updateExternalAxes(String val)
        VAR extjoint extax;

        IF StrToVal(val,extax) THEN
            TPWrite "Moving external axis:";
            currTarg.extax:=extax;
            MoveL currTarg,currSpeed,currZone,currTool;
            currPos:=CPos();
            currTarg:=CRobT();
        ELSE
            TPWrite "Improper msg format: "+val;
        ENDIF
    ENDPROC

ENDMODULE