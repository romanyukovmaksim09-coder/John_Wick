import java.awt.*;
import java.awt.image.BufferedImage;

class Bullet {
    private int x, y;
    private int lastX, lastY;
    private double angle;
    private final double speed = 100.0;
    private boolean active = true;
    private BufferedImage image;
    private int id;

    public Bullet(int startX, int startY, double angle, BufferedImage image, int id) {
        this.x = startX;
        this.y = startY;
        this.lastX = startX;
        this.lastY = startY;
        this.angle = angle;
        this.image = image;
        this.id = id;
    }

    public void update() {
        if (active) {
            lastX = x;
            lastY = y;

            x += (int)(Math.cos(angle) * speed);
            y += (int)(Math.sin(angle) * speed);
        }
    }

    public void draw(Graphics g) {
        if (active && image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(x, y);
            g2d.rotate(angle);
            int width = image.getWidth() / 4;
            int height = image.getHeight() / 4;
            g2d.drawImage(image, -width/2, -height/2, width, height, null);
            g2d.dispose();
        }
    }

    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
    public int getLastX() { return lastX; }
    public int getLastY() { return lastY; }

    // Упрощенный метод для проверки попадания в мишень
    public boolean hitsTarget(int targetX, int targetY, int targetRadius) {
        if (!active) return false;

        // Простая проверка расстояния
        double distance = Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2));
        return distance <= targetRadius + 5; // +5 для небольшого запаса
    }
}
