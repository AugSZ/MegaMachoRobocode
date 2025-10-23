package teste4;



import robocode.*;
import robocode.util.Utils;
import java.util.*;
import java.awt.geom.*;

public class teste4 {

    final static int OPTIONS = 12;
    static int[] optionScores = new int[OPTIONS];
    static int[] offsetCounts = new int[OPTIONS];
    static double[] offsets = new double[OPTIONS];
    static double[] dirOffsets = new double[OPTIONS];

    static double unmatchedEnemyDamage;
    static double enemyDamage;
    static double myDamage;

    AdvancedRobot bot;
    double lastEnemyEnergy = 100;
    ArrayList<Point2D.Double> myLocations = new ArrayList<>();
    ArrayList<Point2D.Double> enemyLocations = new ArrayList<>();
    ArrayList<Double> enemyHeadings = new ArrayList<>();
    ArrayList<Double> enemyVelocities = new ArrayList<>();
    ArrayList<DetectWave> enemyWaves = new ArrayList<>();

    double direction = 1;

    public teste4(AdvancedRobot bot) {
        this.bot = bot;
        if (bot.getRoundNum() > 0)
            System.out.println("Enemy Gun VG scores: \n" + Arrays.toString(optionScores));
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double latVel = bot.getVelocity() * Math.sin(e.getBearingRadians());
        direction = (latVel < 0) ? -1 : 1;

        Point2D.Double myLocation = new Point2D.Double(bot.getX(), bot.getY());
        double absBearing = e.getBearingRadians() + bot.getHeadingRadians();
        Point2D.Double enemyLocation = project(myLocation, absBearing, e.getDistance());

        myLocations.add(0, myLocation);
        enemyLocations.add(0, enemyLocation);
        enemyHeadings.add(0, e.getHeadingRadians());
        enemyVelocities.add(0, e.getVelocity());

        lastEnemyEnergy = e.getEnergy();
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

    void updateWaves() {
        long time = bot.getTime();
        enemyWaves.removeIf(dw -> {
            dw.distanceTraveled = dw.bulletVelocity * (time - dw.fireTime);
            return dw.distanceTraveled - 18 > dw.fireLocation.distance(myLocations.get(0));
        });
    }

    boolean logBullet(Bullet b) {
        double heading = b.getHeadingRadians();
        long time = bot.getTime();
        Point2D.Double bloc = new Point2D.Double(b.getX(), b.getY());

        Iterator<DetectWave> it = enemyWaves.iterator();
        while (it.hasNext()) {
            DetectWave dw = it.next();
            dw.distanceTraveled = dw.bulletVelocity * (time - dw.fireTime);
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

    static Point2D.Double project(Point2D.Double location, double angle, double distance) {
        return new Point2D.Double(location.x + distance * Math.sin(angle),
                                  location.y + distance * Math.cos(angle));
    }

    private double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    class DetectWave {
        long fireTime;
        double direction;
        double[] bulletBearings = new double[OPTIONS];
        boolean[] bearingAttempts = new boolean[OPTIONS];
        long interceptTime;
        double bulletVelocity;
        Point2D.Double fireLocation = new Point2D.Double();
        double distanceTraveled;
    }
}




