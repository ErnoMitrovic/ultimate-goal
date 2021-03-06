package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Disabled
/**
 * <h1>NaveDelOlvido</h1>
 * Es la clase donde programamos la mayoría de los métodos para los autónomos, que
 * tienden a estar programados en un LinearOpMode.
 * @author Hardlus_15704
 * @version 1.0
 * @since 2020-10-04
 */
public class NaveDelOlvido {
    public DcMotor upRight = null;
    public DcMotor upLeft = null;
    public DcMotor downRight = null;
    public DcMotor downLeft = null;
    
    //Motores y servos para el mecanismo de wobble
    public DcMotor wobble = null;
    public Servo garraWobble = null;
    
    private LinearOpMode programa;
    private Orientation angles;
    private BNO055IMU imu;
    public NaveDelOlvido(){
        //Constructor del robot
    }
    /**
     * Ayuda a incluir automaticamente los componentes del robot en el programa del autónomo.
     * @param hwMap Permite agregar el HardwareMap
    */
    public void getHardware (HardwareMap hwMap){
        upRight = hwMap.get(DcMotor.class, "frente_derecha");
        upLeft = hwMap.get(DcMotor.class, "frente_izquierda");
        downRight = hwMap.get(DcMotor.class, "atras_derecha");
        downLeft = hwMap.get(DcMotor.class, "atras_izquierda");
        imu = hwMap.get(BNO055IMU.class, "imu");
        
        wobble = hwMap.get(DcMotor.class, "wobble");
        garraWobble = hwMap.get(Servo.class, "garraWobble");
        
        upRight.setDirection(DcMotorSimple.Direction.FORWARD);
        downRight.setDirection(DcMotorSimple.Direction.FORWARD);
        upLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        downLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }
    /**
     * Obtiene la desviación del giroscopio interno del IMU.
     * @return regresa el primer valor del ángulo registrado.
    */
    public double desviacion(){
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        programa.sleep(25);
        return angles.firstAngle;
    }
    /**
     * Regresa los motores al valor cero
    */
    public void frenar(){
        upLeft.setPower(0);
        upRight.setPower(0);
        downLeft.setPower(0);
        downRight.setPower(0);
    }
    //Metodos para los autonomos
    //Medida de los encoders
    final int TICKS = 0;
    final double DIAM = 10.61;
    /**
     * Valores por default de los autónomos
     * @param distancia: la distancia que se moverá el robot
     * @return conversion: La conversión utilizada para los motores del robot
    */
    private int valoresInicialesAutonomo(double distancia){
        final int conversion = (int) Math.round(distancia * TICKS / DIAM / Math.PI);
        upRight.setTargetPosition(conversion);
        upLeft.setTargetPosition(conversion);
        downRight.setTargetPosition(conversion);
        downLeft.setTargetPosition(conversion);
        upRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        upLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        downRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        downLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        return conversion;
    }
    /**
     * Corrección de trayectorias en el autonomo
     * 
    */
    public void useImu(double desiredPosition, double velocidad, double distancia){
        double desviacion = desviacion();
        double error = (desiredPosition - desviacion) / desiredPosition;
        final double PROPORCIONAL = 0.002;
        double leftPower = 0;
        double rightPower = 0;
        if (distancia > 0){
            leftPower = velocidad;
            rightPower = velocidad;
            leftPower -= leftPower * error * PROPORCIONAL;
            rightPower += rightPower * error * PROPORCIONAL;
        }
        else if (distancia < 0){
            velocidad *= -1;
            leftPower = velocidad;
            rightPower = velocidad;
            leftPower -= leftPower * error * PROPORCIONAL;
            rightPower += rightPower * error * PROPORCIONAL;
        }
        upLeft.setPower(leftPower);
        downLeft.setPower(leftPower);
        upRight.setPower(rightPower);
        downRight.setPower(rightPower);
    }
    /**
     * Mueve el robot una distancia recta hacia delante o atrás con la potencia especificada.
     * @param distancia Es la distancia que se moverá hacia delante o atrás
     * @param velocidad Es la potencia que se le asignará a los motores
    */
    public void moverDistanciaRecta(double distancia, double velocidad){
        valoresInicialesAutonomo(distancia);
        double desiredPosition = desviacion();
        while(programa.opModeIsActive() && downLeft.isBusy() && downRight.isBusy() && 
        upLeft.isBusy() && upRight.isBusy()){
            useImu(desiredPosition, velocidad, distancia);
        }
        frenar();
    }
    /**
     * Metodo para mover lateralmente al robot mecano
     * @param distancia: la distancia que se planea que se mueva el robot
     * @param velocidad: la potencia que se le asignará a los motores
    */
    public void moverDistanciaLateral(double distancia, double velocidad){
        //UpRight y DownLeft van en dirección contraria
        int conversion = valoresInicialesAutonomo(distancia);
        double desiredPosition = desviacion();
        upRight.setTargetPosition(-conversion);
        downLeft.setTargetPosition(-conversion);
        while(programa.opModeIsActive() && downLeft.isBusy() && downRight.isBusy() && 
        upLeft.isBusy() && upRight.isBusy()){
            useImu(desiredPosition, velocidad, distancia);
        }
        frenar();
    }
}
