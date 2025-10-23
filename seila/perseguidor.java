package seila;
import robocode.*;
import java.awt.*;





public class perseguidor extends AdvancedRobot {

	int moveDirection=1;//Como ele vai se movimentar

	/**

	 * run:  Função principal de movimentação

	 */

	public void run() {

		setAdjustRadarForRobotTurn(true);//Mantém o radar parado, enquanto se movimenta

		setBodyColor(new Color(128, 128, 50));		// 

		setGunColor(new Color(50, 50, 20));			// Define as cores do robô

		setRadarColor(new Color(200, 200, 70));		// 

		setScanColor(Color.white);					// Cor do scanner

		setBulletColor(Color.blue);					// Cor da bala

		setAdjustGunForRobotTurn(true); // Mantém o canhão estável no movimento

		turnRadarRightRadians(Double.POSITIVE_INFINITY);//Mantém o radar se movimentando para direita

	}



	/**

	 * onScannedRobot: O que o robô faz se localizar um inimigo no radar

	 */

	public void onScannedRobot(ScannedRobotEvent e) {

		double absBearing=e.getBearingRadians()+getHeadingRadians();//Pega o angulo/bearing do inimigo

		double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//Velocidade do inimigo

		double gunTurnAmt;//amount to turn our gun (não sei oq signifca, estava aqui antes. Não tirei pra não dar merda)

		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());// Lock no radar

		if(Math.random()>.9){