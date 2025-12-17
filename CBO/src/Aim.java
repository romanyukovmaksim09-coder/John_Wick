import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class Aim {
    private BufferedImage aimImage;
    private BufferedImage aimImage1;
    private int x, y;
    private int bodyX, bodyY;
    private int mouseX, mouseY;
    private boolean rightMousePressed = false;
    private boolean leftMousePressed = false;
    private final int AIM_DISTANCE = 350;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private BufferedImage bulletImage;
    private long lastShotTime = 0;
    private final long SHOT_DELAY = 50;
    private boolean usingAlternateAim = false;
    private double bodyLookAngle = 0;

    private int bulletCounter = 0;
    private int bulletsFired = 0;
    private int bulletsHit = 0;
    private int bulletsMissed = 0;

    public Aim() throws Exception {
        loadImages();
        loadBulletImage();
        x = 500;
        y = 500;
        bodyX = 500;
        bodyY = 500;
        mouseX = 500;
        mouseY = 500;
    }

    private void loadImages() throws Exception {
        String[] paths1 = {"data/aim.png", "aim.png", "data\\aim.png"};
        boolean aimLoaded = false;

        for (String path : paths1) {
            File aimFile = new File(path);
            if (aimFile.exists()) {
                aimImage = ImageIO.read(aimFile);
                System.out.println("Загружен прицел aim.png: " + path);
                aimLoaded = true;
                break;
            }
        }
        if (!aimLoaded) {
            throw new Exception("Файл aim.png не найден!");
        }

        String[] paths2 = {"data/aim1.png", "aim1.png", "data\\aim1.png"};
        aimLoaded = false;

        for (String path : paths2) {
            File aimFile = new File(path);
            if (aimFile.exists()) {
                aimImage1 = ImageIO.read(aimFile);
                System.out.println("Загружен прицел aim1.png: " + path);
                aimLoaded = true;
                break;
            }
        }
        if (!aimLoaded) {
            System.out.println("Внимание: Файл aim1.png не найден. Будет использован aim.png для обоих режимов.");
            aimImage1 = aimImage;
        }
    }

    private void loadBulletImage() {
        try {
            String[] paths = {"data/bullet.png", "bullet.png", "data\\bullet.png"};

            for (String path : paths) {
                File bulletFile = new File(path);
                if (bulletFile.exists()) {
                    bulletImage = ImageIO.read(bulletFile);
                    System.out.println("Загружена пуля: " + path);
                    return;
                }
            }
            System.out.println("Файл bullet.png не найден. Используется стандартная пуля.");
            bulletImage = createDefaultBulletImage();
        } catch (Exception e) {
            System.out.println("Ошибка загрузки bullet.png: " + e.getMessage());
            bulletImage = createDefaultBulletImage();
        }
    }

    private BufferedImage createDefaultBulletImage() {
        BufferedImage img = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(0, 0, 10, 20);
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(2, 2, 6, 16);
        g2d.dispose();
        return img;
    }

    public void updatePosition(int bodyX, int bodyY, int mouseX, int mouseY,
                               boolean rightMousePressed, boolean leftMousePressed,
                               double bodyLookAngle) {
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.bodyLookAngle = bodyLookAngle;

        if (leftMousePressed) {
            usingAlternateAim = true;
            if (!this.leftMousePressed) {
                fireSingle();
            } else {
                fireAuto();
            }
        } else {
            usingAlternateAim = false;
        }

        this.leftMousePressed = leftMousePressed;
        this.rightMousePressed = rightMousePressed;

        if (rightMousePressed) {
            this.x = mouseX;
            this.y = mouseY;
        } else {
            calculateAimPosition();
        }

        updateBullets();
    }

    private void fireSingle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOT_DELAY) {
            double spreadDegrees = (Math.random() - 0.5);
            double spreadRadians = Math.toRadians(spreadDegrees);
            double bulletAngle = bodyLookAngle + spreadRadians;

            double forwardOffsetX = 2 * Math.cos(bodyLookAngle);
            double forwardOffsetY = 2 * Math.sin(bodyLookAngle);

            double rightAngle = bodyLookAngle + Math.PI/2;
            double rightOffsetX = 10 * Math.cos(rightAngle);
            double rightOffsetY = 10 * Math.sin(rightAngle);

            double totalOffsetX = forwardOffsetX + rightOffsetX;
            double totalOffsetY = forwardOffsetY + rightOffsetY;

            int muzzleX = bodyX + (int)totalOffsetX;
            int muzzleY = bodyY + (int)totalOffsetY;

            bullets.add(new Bullet(muzzleX, muzzleY, bulletAngle, bulletImage, bulletCounter++));
            bulletsFired++;
            lastShotTime = currentTime;
        }
    }

    private void fireAuto() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOT_DELAY) {
            double spreadDegrees = (Math.random() - 0.5);
            double spreadRadians = Math.toRadians(spreadDegrees);
            double bulletAngle = bodyLookAngle + spreadRadians;

            double forwardOffsetX = 2 * Math.cos(bodyLookAngle);
            double forwardOffsetY = 2 * Math.sin(bodyLookAngle);

            double rightAngle = bodyLookAngle + Math.PI/2;
            double rightOffsetX = 10 * Math.cos(rightAngle);
            double rightOffsetY = 10 * Math.sin(rightAngle);

            double totalOffsetX = forwardOffsetX + rightOffsetX;
            double totalOffsetY = forwardOffsetY + rightOffsetY;

            int muzzleX = bodyX + (int)totalOffsetX;
            int muzzleY = bodyY + (int)totalOffsetY;

            bullets.add(new Bullet(muzzleX, muzzleY, bulletAngle, bulletImage, bulletCounter++));
            bulletsFired++;
            lastShotTime = currentTime;
        }
    }

    private void updateBullets() {
        int activeBefore = bullets.size();
        bullets.removeIf(bullet -> !bullet.isActive());
        int activeAfter = bullets.size();
        int disappeared = activeBefore - activeAfter;
        bulletsMissed += disappeared;

        for (Bullet bullet : bullets) {
            bullet.update();
        }
    }

    public void registerHit() {
        bulletsHit++;
        bulletsMissed--;
    }

    private void calculateAimPosition() {
        double dx = mouseX - bodyX;
        double dy = mouseY - bodyY;
        double distanceToMouse = Math.sqrt(dx * dx + dy * dy);

        if (distanceToMouse > 0) {
            double directionX = dx / distanceToMouse;
            double directionY = dy / distanceToMouse;
            this.x = bodyX + (int)(directionX * AIM_DISTANCE);
            this.y = bodyY + (int)(directionY * AIM_DISTANCE);
        } else {
            this.x = bodyX + AIM_DISTANCE;
            this.y = bodyY;
        }
    }

    public void draw(Graphics g) {
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        BufferedImage currentAimImage = usingAlternateAim ? aimImage1 : aimImage;

        if (currentAimImage != null) {
            int width = currentAimImage.getWidth() / 2;
            int height = currentAimImage.getHeight() / 2;
            g.drawImage(currentAimImage, x - width/2, y - height/2, width, height, null);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public int getBulletsFired() { return bulletsFired; }
    public int getBulletsHit() { return bulletsHit; }
    public int getBulletsMissed() { return bulletsMissed; }
    public int getActiveBullets() { return bullets.size(); }
    public int getTotalBulletCounter() { return bulletCounter; }
}