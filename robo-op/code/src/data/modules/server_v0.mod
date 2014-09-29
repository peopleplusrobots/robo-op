MODULE server_v0

! RAPID module to remotely send commands to an industrial robot from 
! an interactive java application.
! 
!
! Code based off of open-abb-driver {https://github.com/robotics/open-abb-driver}
!
!
! @authors mad & zack
! (mad) www.madlab.cc
! (zack)enartdezark.blogspot.com
  

  ! Setup tool and home data
  PERS tooldata MyTool := [TRUE,[[0,0,129.794],[1,0,0,0]],[1,[0,0,-196.3566],[1,0,0,0],0,0,0]];
  CONST jointtarget homePose := [[0,0,0,0,0,0],[0,0,0,0,0,0]];
  CONST robtarget StartTarget:=  [ [0,0,0], [1, 0, 0, 0], [-2,-1,0,0], [ 84.5, 9E9, 9E9, 9E9, 9E9, 9E9] ];

  ! Setup communication variables
  VAR socketdev temp_socket;
  VAR socketdev client_socket;
  VAR string received_string;
  VAR num msgLength;
  VAR bool listen:=TRUE;
  VAR bool initialize:=TRUE;
  
  ! Setup program variables
  VAR pos CurrPos;
  VAR robtarget DeltaZ;



PROC ServerMain()
  VAR num sensorVal;
  VAR bool touching := FALSE;
 
  SocketCreate temp_socket;
  SocketBind temp_socket,"128.2.109.111",1025;
  SocketListen temp_socket;
  SocketAccept temp_socket,client_socket;

  ! Go to station position
  ! MoveAbsJ homePose, v200, z1, MyTool;
  
  !Get the Current Position and Current RobTarget
  CurrPos := CPos();
  DeltaZ := CRobT();
  
  WHILE listen DO
  
  	IF (touching = FALSE) THEN

      ! Any MOVE COMMAND is effectively your 'refresh rate'
  	  ! Move forward
  	  DeltaZ.trans.z := DeltaZ.trans.z - 1;

  	  MoveL DeltaZ, v20, z5, MyTool;
  	  
  	  !Get the Current Position and Current RobTarget
	    CurrPos := CPos();
	    DeltaZ := CRobT();
  	  
  	ENDIF
  	
      SocketSend client_socket\Str:="ready for msg\0D\0A";
      ! Live Communication (blocks until it gets a msg)
      SocketReceive client_socket\Str:=received_string;
      TPWrite "Incoming Msg:  "+received_string;

      ! Verify to client that we received the message
      SocketSend client_socket\Str:="msg received\0D\0A";
      
	  ! Translate String to SensorValue and Print
	  ! If we get a number, it means we're touching something	 

    ! DEBUGGING: Just check if the 1st char is a number 
    msgLength := StrLen(received_string);
    IF (msgLength > 2) THEN
      TPWrite StrPart(received_string,1,1);
      IF ( StrToVal( StrPart(received_string,1,2),sensorVal) ) THEN
  	  !IF (StrToVal(received_string,sensorVal)) THEN
  	  	! Record position and target
  	  	CurrPos := CPos();
  	    DeltaZ := CRobT();
  	    
  	    ! Stop moving if we're touching something
  	  	TPWrite "TOUCH at " \Num:=CurrPos.z;	  	
  	  	! touching := TRUE;  

        ! Send the CurrPos to the Client
        SocketSend client_socket\Str:=ValToStr(CurrPos)+"\0D\0A";
  	  	   	
  	  ENDIF

    ENDIF
      
    ! Reset message to empty
    received_string:="";

    

  ENDWHILE

  TPWrite "Close Connection";
  SocketClose temp_socket;


  ENDPROC

ENDMODULE