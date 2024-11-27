package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Test Drive Phase1")
public class Testing_Phase1 extends LinearOpMode {
    private double frontLeftPower = 0;     // declare motor power variable
    private double backLeftPower = 0;      // declare motor power variable
    private double frontRightPower = 0;    // declare motor power variable
    private double backRightPower = 0;     // declare motor power variable
    private double denominator = 1;        // declare motor power calculation variable

    private int precision = 2;                    // chassis motor power reduction factor 1
    private boolean intakeClawClosed = false;     // claw holder variable
    private ElapsedTime stateDelay = new ElapsedTime(); // possible delay timer for state change
    private double LeftServo;                // currently intake wrist servo left
    private double RightServo;               // currently intake wrist servo right
    private double V4Bpos = 1;              // currently intake V4B
    private double Flex = .1;               // currently intake wrist angle
    private double Yaw = 0;                 // currently intake wrist twist

    private enum V4Bstate {                 // states of intake/outake machine
        START,
        INTAKE,
        TRANSFER,
        OUTTAKE                               //  TODO add OUTTAKE state  *******************************
    }

    V4Bstate state = V4Bstate.START;           // initial state of intake/outake servos

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor FrontRight = hardwareMap.dcMotor.get("Front Right");       // Chub Port 0 // Gamepad 1
        DcMotor BackRight = hardwareMap.dcMotor.get("Back Right");         // Chub Port 1 // Left Stick For Moving
        DcMotor FrontLeft = hardwareMap.dcMotor.get("Front Left");         // Chub Port 2 // Right Stick For Turning
        DcMotor BackLeft = hardwareMap.dcMotor.get("Back Left");           // Chub Port 3

    	Servo IntakeClaw = hardwareMap.servo.get("Intake Claw");              // Chub Port 0 // O Button
    	Servo RightIntakeWrist = hardwareMap.servo.get("Right Intake Wrist"); // Chub Port 1 // Flex and Yaw controlled
        Servo LeftIntakeWrist = hardwareMap.servo.get("Left Intake Wrist");   // Chub Port 2 // IFlex and Yaw controlled
        Servo RightIntakeV4B = hardwareMap.servo.get("Right Intake V4B");     // Chub Port 3 // Preset To Swing Out With A
        Servo LeftIntakeV4B = hardwareMap.servo.get("Left Intake V4B");       // Chub Port 4 // --------------------------
 
        LeftServo = Flex - (.5 * Yaw);                                      // intake wrist servo calc
        RightServo = Flex + (.5 * Yaw);                                     // intake wrist servo calc

        BackLeft.setDirection(DcMotorSimple.Direction.REVERSE);             // Reverses the direction the motor turns
        FrontRight.setDirection(DcMotorSimple.Direction.REVERSE);           // Reverses the direction the motor turns

        FrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);    // Sets the motor to be locked when stopped
        FrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);   // Sets the motor to be locked when stopped
        BackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);     // Sets the motor to be locked when stopped
        BackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);    // Sets the motor to be locked when stopped


        IntakeClaw.setPosition(0);                  // Closes Intake Claw
        LeftIntakeWrist.setPosition(LeftServo);     // Sets the intake wrist to the starting position // Left is 0 Right is 1
        RightIntakeWrist.setPosition(RightServo);   // Sets the intake wrist to the starting position // Left is 0 Right is 1
        LeftIntakeV4B.setPosition(1);               // Sets the intake virtual four bar to the starting position
        RightIntakeV4B.setPosition(1);              // Sets the intake virtual four bar to the starting position

        // Beginning of opmode

        waitForStart();
        while (opModeIsActive()) {

            //*************************** DRIVE CONTROLS **************************************************
            // check for driving input
            double y = gamepad1.left_stick_y;         // Remember, this is reversed!
            double x = -gamepad1.right_stick_x * 1.1; // Counteract imperfect strafing
            double rx = -gamepad1.left_stick_x;       // Measures turning
            if (gamepad1.right_trigger >= 0.75) {     // Checks if the Right Trigger was pressed and if so it continues the stuff in the brackets
                y = -gamepad1.left_stick_y;           // Remember, this is reversed!
                x = gamepad1.right_stick_x * 1.1;     // Counteract imperfect strafing
                rx = gamepad1.left_stick_x;           // Measures turning
            }

            denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);    // calculate motor movement math and adjust according to lift height or manual precision mode selection

            // check for Turbo or Precision Mode
            if (gamepad1.left_bumper) {         // Left bumper is being pressed
                precision = 1;                  // set speed to full power - TURBO MODE
            } else if (gamepad1.right_bumper) { // Right bumper is being pressed
                precision = 4;                  // set speed to 1/4 power - PRECISION MODE
            } else {
                precision = 2;                  // reset default speed to half power
            }

            // calculate motor power
            denominator = denominator * precision;          // this adjusts motor speed to the desired precision level
            frontLeftPower = (y + x + rx) / denominator;    // Does math to convert stick movement to motor powers
            backLeftPower = (y - x + rx) / denominator;     // Does math to convert stick movement to motor powers
            frontRightPower = (y - x - rx) / denominator;   // Does math to convert stick movement to motor powers
            backRightPower = (y + x - rx) / denominator;    // Does math to convert stick movement to motor powers

            // State machine for intake and outake servo control

            switch (state) {                 //  These controls have been switched to gamepad2
                case START:
                    /* if (gamepad2.a) {     // Transfer Position  ORIGINAL CODE
                        V4Bpos = 1;
                        Flex = 0;
                        state = V4Bstate.INTAKE;    // Switch to INTAKE state
                    }
                    break; */
                    //                 TODO     Proposed alternate code

                    // if (stateDelay < .25 ){ break; }   // TODO possible delay code for each state change

                    V4Bpos = 1;	            	// Bar fully up upon entering state
                    Flex = 0;		            // Wrist fully up upon entering state
                    if (gamepad2.a){        	// Switch to INTAKE Position with a button
                        state = V4Bstate.INTAKE;
                        // stateDelay.reset();  // used in conjuction with stateDelay checking in each state
                    }
                    break;
                case INTAKE:    
                    /* V4Bpos = .3;  // This seems redundant as line 120 does the same         ****  ORIGINAL CODE
                    Flex = .6;
                    if (gamepad2.touchpad_finger_1) {           // Allows manual yaw control if a finger is on the touchpad
                        Yaw = gamepad2.touchpad_finger_1_x;    // Taking value from touchpad and saving as our desired yaw value
                    } else {
                        Yaw = 0; //Sets yaw to 0 if no finger is detected on the touchpad
                    }
                    if (gamepad2.right_trigger > 0) {
                        V4Bpos = 0.3 * (1 - (gamepad2.right_trigger)); //Control for variable virtual four bar height when in INTAKE state
                    } else {
                        V4Bpos = .3;
                    }
                    if (gamepad2.x) {  //Transfer position
                        V4Bpos = 1;
                        Flex = 0;
                        state = V4Bstate.TRANSFER;
                    }
                    break;  */
                    //                  TODO        Proposed alternate code
                    //                              Create button controls and sequencing for TRANSFER and OUTTAKE cases

                    V4Bpos = .3 * ( 1 - (gamepad2.right_trigger));     // Factors trigger value or returns to baseline position if no input
                    Flex = .6;					                       // TODO - determine default Flex value for here
                    Yaw = gamepad2.touchpad_finger_1_x;		           // Taking value from touchpad and saving as our desired yaw value
                    if (gamepad2.b){    			        // Toggle claw with b button
                 		if (!intakeClawClosed){             // Determine claw position
		             		IntakeClaw.setPosition(0);  	// Close claw if open
    	             	}
	                  	else {  		
		                 IntakeClaw.setPosition(1);	    	 // Open claw if closed
	                	}
	            		intakeClawClosed = !intakeClawClosed; // Switch position status variable
		            }
                    if (gamepad2.x){		                  // x button Transfer position
                        state = V4Bstate.TRANSFER;
                    }
                    break;         
                case TRANSFER:                          // Currently defaulting to START positions
                    state = V4Bstate.START;             // TODO     Transfer sequencing
                    break;
            	case OUTTAKE:                           // TODO   raising and scoring button functions   *********************
                //  if ( "button pressed"){             // Conditional code here
        	    //  state = V4Bstate.TRANSFER;
            	//  }			    
                    break;
            }

            // Continuosly looping code outside of State Machine

   	        if (gamepad2.right_trigger > 0) {     // This righttrigger override is an alternate TRANSFER state selector
   	             state = V4Bstate.INTAKE;         // changes to INTAKE state to allow manual adjustment
            }
            // Intake and Wrist positioning
            LeftIntakeV4B.setPosition(V4Bpos);      // issue intake V4B servo position
            RightIntakeV4B.setPosition(V4Bpos);     // issue intake v4B servo position
            LeftServo = Flex - (.5 * Yaw); //Calculates required servo angles for combined flex and yaw motion
            RightServo = Flex + (.5 * Yaw);//^
            LeftIntakeWrist.setPosition(LeftServo); //Sets servos to calculated positions
            RightIntakeWrist.setPosition(RightServo); // ^

            // TODO OUTTAKE servo code

            // issue Drive Wheels motor power
            FrontLeft.setPower(frontLeftPower);    // Sets the front left wheel's power
            BackLeft.setPower(backLeftPower);     // Sets the back left wheel's power
            FrontRight.setPower(frontRightPower);  // Sets the front right wheel's power
            BackRight.setPower(backRightPower);   // Sets the back right wheel's power

            // Telemetry information
            telemetry.addData("Touch Pad Yaw Input", Yaw)
                     .addData("Flex Input", Flex)
                     .addData("Left Wrist Target", LeftServo)
                     .addData("Right Wrist Target", RightServo)
                     .addData("Left Wrist Actual", LeftIntakeWrist.getPosition())
                     .addData("Right Wrist Actual", RightIntakeWrist.getPosition());
            telemetry.update();
                 
       }
    }
}
