package teste4;



import robocode.*;
import robocode.util.Utils;
import java.util.*;
import java.awt.geom.*;
import jk.mega.FastTrig;
import jk.precise.util.*;

public class MeuRobo {

    // ==============================
    // ======= CAMPOS ESTÁTICOS =====
    // ==============================
    final static int OPTIONS = 12;
    static int[] optionScores = new int[OPTIONS];
    static int[] offsetCounts = new int[OPTIONS];
    static double[] offsets = new double[OPTIONS];
    static double[] dirOffsets = new double[OPTIONS];

    static double unmatchedEnemyDamage;
    static double enemyDamage;
    static double myDamage;

    // ==============================
    // ======= CAMPOS NORMAIS =======
    // ==============================
    AdvancedRobot bot;
    double lastEnemyEnergy = 100;
    Point2D.Double lastEnemyLocation;
    ArrayList<Point2D.Double> myLocations = new ArrayList<>();
    ArrayList<Point2D.Double> enemyLocations = new ArrayList<>();
    ArrayList<Double> enemyHeadings = new ArrayList<>();
    ArrayList<Double> enemyVelocities = new ArrayList<>();
    ArrayList<DetectWave> enemyWaves = new ArrayList<>();

    double lastBearing;
    double firePower;
    double moveAmount;
    long nextFireTime;
    double direction = 1;

    boolean move;
    boolean aim;
    boolean turn;
    boolean fire;
    double moveVal;
    double aimAngle;
    double turnAngle;

    // ===================================
    // ======= CONSTRUTOR CORRIGIDO ======
    // ===================================
    public MeuRobo(AdvancedRobot bot) {
        this.bot = bot;
        nextFireTime = bot.getTime() + (long) Math.ceil(bot.getGunHeat() / bot.getGunCoolingRate());
        if (bot.getRoundNum() > 0)
            System.out.println("Enemy Gun VG scores: \n" + Arrays.toString(optionScores));
    }

    // ===================================
    // ======= EVENTOS DO ROBO ===========
    // ===================================
    public void onScannedRobot(ScannedRobotEvent e) {
        double latVel = bot.getVelocity() * Math.sin(e.getBearingRadians());
        if (latVel < 0) direction = -1;
        if (latVel > 0) direction = 1;

        Point2D.Double myLocation = new Point2D.Double(bot.getX(), bot.getY());
        double absBearing = e.getBearingRadians() + bot.getHeadingRadians();
        double eDistance = e.getDistance();
        double deltaE = (lastEnemyEnergy - (lastEnemyEnergy = e.getEnergy()));
        Point2D.Double enemyLocation = project(myLocation, absBearing, eDistance);

        myLocations.add(0, myLocation);
        enemyLocations.add(0, enemyLocation);
        enemyHeadings.add(0, e.getHeadingRadians());
        enemyVelocities.add(0, e.getVelocity());

        // (restante da lógica original omitida para simplificar)
        // ...
    }

    public void onHitByBullet(HitByBulletEvent e) {
        lastEnemyEnergy += 20 - e.getVelocity();
        boolean matched = logBullet(e.getBullet());
        double damage = Rules.getBulletDamage(e.getBullet().getPower());
        enemyDamage += damage;
        if (!matched) unmatchedEnemyDamage += damage;
    }

    public void onBulletHit(BulletHitEvent e) {
        lastEnemyEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
        myDamage += Rules.getBulletDamage(e.getBullet().getPower());
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        logBullet(e.getHitBullet());
    }

    // ===================================
    // ======= MÉTODOS AUXILIARES ========
    // ===================================
    static Point2D.Double project(Point2D.Double location, double angle, double distance) {
        return new Point2D.Double(location.x + distance * Math.sin(angle),
                                  location.y + distance * Math.cos(angle));
    }

    private double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    void updateWaves() {
        Iterator<DetectWave> it = enemyWaves.iterator();
        long time = bot.getTime();
        while (it.hasNext()) {
            DetectWave dw = it.next();
            dw.distanceTraveled = dw.bulletVelocity * (time - dw.fireTime);
            if (dw.distanceTraveled - 18 > dw.fireLocation.distance(myLocations.get(0)))
                it.remove();
        }
    }

    boolean logBullet(Bullet b) {
        double heading = b.getHeadingRadians();
        Iterator<DetectWave> it = enemyWaves.iterator();
        long time = bot.getTime();
        while (it.hasNext()) {
            DetectWave dw = it.next();
            dw.distanceTraveled = dw.bulletVelocity * (time - dw.fireTime);
            Point2D.Double bloc = new Point2D.Double(b.getX(), b.getY());
            if (Math.abs(dw.distanceTraveled - dw.fireLocation.distance(bloc) - dw.bulletVelocity) < 1.1 * dw.bulletVelocity) {
                boolean matched = false;
                for (int i = 0; i < OPTIONS; i++) {
                    double diff = Utils.normalRelativeAngle(heading - dw.bulletBearings[i]);
                    if (Math.abs(diff) < 0.00001) {
                        optionScores[i]++;
                        matched = true;
                    }
                }
                it.remove();
                return matched;
            }
        }
        System.out.println("No bullet detected");
        return false;
    }

    // ===================================
    // ======= CLASSES INTERNAS ==========
    // ===================================
    class DetectWave extends PreciseWave {
        long fireTime;
        double direction;
        double[] bulletBearings;
        boolean[] bearingAttempts;
        long interceptTime;
    }
}


