package MegaMachoRobocode.variante;

import robocode.*;
import java.awt.*;

/**
 * EpsilonAlpha - Versão 4 (com predição tipo AndersonSilva)
 *
 * Mantive o comportamento original do Epsilon (movimento, evasão, estética).
 * Substituí apenas a lógica de previsão do tiro pela técnica do AndersonSilva:
 * -> simulação tick-a-tick da posição futura do inimigo até a bala alcançá-lo.
 */
public class EpsilonTeste extends AdvancedRobot {
    // Multiplicador de direção para movimento (-1 ou 1)
    int moveDirection = 1;

    // Monitora o nível de energia anterior do inimigo para detectar tiros
    double previousEnemyEnergy = 100;

    // Variáveis para guardar dados do inimigo (atualizadas a cada scan)
    double enemyVelocity = 0;
    double enemyHeading = 0;    // em graus
    double enemyDistance = 0;
    double enemyBearing = 0;    // em graus

    // potência de disparo que vamos usar (atualizada por distância)
    double lastFirePower = 3;

    public void run() {
        // Configurações
        setAdjustRadarForRobotTurn(true);    // Mantém o radar estável durante movimento
        setAdjustGunForRobotTurn(true);      // Mantém o canhão estável durante movimento

        // Estética
        setBodyColor(new Color(0, 0, 0));
        setGunColor(new Color(0, 75, 67));
        setRadarColor(new Color(0, 75, 67));
        setScanColor(Color.white);
        setBulletColor(Color.red);

        // Movimento inicial do radar
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    /**
     * Calcula o ângulo (em radianos) que o canhão deve girar para mirar na posição futura
     * do inimigo. Técnica: simula o movimento do inimigo tick-a-tick até que a bala o alcance.
     *
     * @param power potência do tiro (0.1 .. 3.0)
     * @param e evento ScannedRobotEvent atual (fornece dados frescos)
     * @return ângulo (radianos) relativo para setTurnGunRightRadians()
     */
    public double calcularAnguloPreditivo(double power, ScannedRobotEvent e) {
        // velocidade da bala (pixels / tick)
        double bulletSpeed = 20 - 3 * power;
        if (bulletSpeed <= 0) bulletSpeed = 0.001; // segurança

        // nossas coordenadas
        double myX = getX();
        double myY = getY();

        // posição absoluta atual do inimigo (usando heading do nosso robô + bearing)
        double angAbsRad = getHeadingRadians() + e.getBearingRadians();
        double enemyX = myX + Math.sin(angAbsRad) * e.getDistance();
        double enemyY = myY + Math.cos(angAbsRad) * e.getDistance();

        // velocidade e direção atual do inimigo (em radianos)
        double headingRad = e.getHeadingRadians();
        double velocity = e.getVelocity();

        // inicializa a posição futura com a posição atual
        double futuroX = enemyX;
        double futuroY = enemyY;

        // percorre ticks simulados até que a bala alcance a posição simulada do inimigo
        // deltaTime = 1 tick por iteração (discreto)
        double deltaTime = 1.0;
        // condição: enquanto (distância entre nós e o futuro) > distância que a bala percorre em t ticks
        // implementamos iterando t e verificando t*bulletSpeed < distance(my, futuro)
        for (double t = 0; ; t += deltaTime) {
            double distanceToFut = Math.hypot(futuroX - myX, futuroY - myY);
            // tempo de voo estimado da bala até o ponto futuro = distanceToFut / bulletSpeed
            // se a bala, percorrendo t ticks, já teria alcançado distanceToFut, paramos.
            if (t * bulletSpeed >= distanceToFut) {
                break;
            }

            // avança a posição futura do inimigo em um tick (assumindo movimento retilíneo com velocidade atual)
            futuroX += Math.sin(headingRad) * velocity * deltaTime;
            futuroY += Math.cos(headingRad) * velocity * deltaTime;

            // evita sair do battlefield
            futuroX = Math.max(18, Math.min(futuroX, getBattleFieldWidth() - 18));
            futuroY = Math.max(18, Math.min(futuroY, getBattleFieldHeight() - 18));

            // segurança contra loops infinitos (se bulletSpeed for muito pequeno)
            if (t > 1000) break;
        }

        // calcula ângulo para a posição futura (o uso de dx,dy mantém consistência com o resto do seu código)
        double dx = futuroX - myX;
        double dy = futuroY - myY;
        double anguloParaFuturo = Math.atan2(dx, dy); // nota: atan2(x,y) usado no seu código original

        // normaliza diferença entre o ângulo e o heading do canhão (em radianos)
        double gunHeading = getGunHeadingRadians();
        double angleToTurn = robocode.util.Utils.normalRelativeAngle(anguloParaFuturo - gunHeading);

        return angleToTurn;
    }

    /**
     * Comportamento quando detecta inimigo.
     * Mantive quase tudo do seu onScannedRobot — só removi a predição anterior e chamei calcularAnguloPreditivo().
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double power = 3; // default (ajustado abaixo por distância)

        // Atualiza dados do inimigo para uso em movimento/predição (variáveis de instância)
        enemyVelocity = e.getVelocity();
        enemyHeading = Math.toDegrees(e.getHeadingRadians()); // se precisar em graus em outro lugar
        enemyDistance = e.getDistance();
        enemyBearing = Math.toDegrees(e.getBearingRadians());

        // Cálculo dos ângulos de mira e velocidade lateral
        double anguloAbsoluto = e.getBearingRadians() + getHeadingRadians();
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - anguloAbsoluto);
        double gunTurnAmt;

        // Mantém radar travado
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

        // Sistema de detecção de tiro inimigo (evitação)
        double changeInEnergy = previousEnemyEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            double evasionAngle = (Math.random() - 0.5) * Math.PI / 2;
            double moveAmount = 100 + Math.random() * 100;
            setTurnRightRadians(evasionAngle);
            setAhead(moveAmount * (Math.random() > 0.5 ? 1 : -1));
        }
        previousEnemyEnergy = e.getEnergy();

        // Velocidade aleatória para evitar previsibilidade
        if (Math.random() > .6) {
            setMaxVelocity((12 * Math.random()) + 12);
        }

        // --- Atualiza potência com base na distância (igual ao original) ---
        if (e.getDistance() > 600) {
            power = 1.0;
        } else if (e.getDistance() > 300) {
            power = 2.0;
        } else {
            power = 3.0;
        }
        lastFirePower = power;

        // --- AQUI USAMOS A PREDIÇÃO TIPO ANDERSON: ---
        double aimTurn = calcularAnguloPreditivo(power, e);
        // gira o canhão na direção predita
        setTurnGunRightRadians(aimTurn);

        // Decide quando atirar: quando alinhado o suficiente e canhão estiver frio
        if (Math.abs(getGunTurnRemainingRadians()) < Math.toRadians(10) && getGunHeat() == 0) {
            setFire(power);
        }

        // Mantive sua lógica de movimento/engajamento (apenas removi a predição antiga)
        double meuX = getX();
        double meuY = getY();

        // posição atual do robo inimigo (S0) — já calculada acima mas mantive a convenção
        double angle = Math.toRadians(getHeading() + Math.toDegrees(e.getBearing()));
        double Xdeles = getX() + Math.sin(angle) * e.getDistance();
        double Ydeles = getY() + Math.cos(angle) * e.getDistance();

        // Comportamento de combate baseado na distância (mantido)
        if (e.getDistance() > 150) {
            // Engajamento a longa distância (mantive o seu esquema de movimento)
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians());
            setTurnGunRightRadians(gunTurnAmt);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(anguloAbsoluto - getHeadingRadians() + latVel / Math.max(0.001, getVelocity())));
            setAhead((e.getDistance() - 140) * moveDirection);
            // O tiro já está sendo tratado acima (predição)
        } else {
            // Engajamento a curta distância
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians() + latVel / 15);
            setTurnGunRightRadians(gunTurnAmt);
            setTurnLeft(-90 - e.getBearing());
            setAhead((e.getDistance() - 140) * moveDirection);
            // o disparo também é tratado pela predição acima
        }
    }

    // Trata colisão com paredes invertendo a direção
    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;
    }

    // Registra dano causado ao inimigo quando um tiro acerta
    public void onBulletHit(BulletHitEvent e) {
        double energiaPerdidaInimigo = robocode.Rules.getBulletDamage(e.getBullet().getPower());
        // você pode usar isso para estatísticas ou adaptar potência depois
    }

    // Registra ganho de energia do inimigo quando somos atingidos
    public void onHitByBullet(HitByBulletEvent e) {
        double energiaGanhaInimigo = robocode.Rules.getBulletHitBonus(e.getBullet().getPower());
    }

    // Rotina de comemoração de vitória
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}
