import java.awt.*;
import java.util.ArrayList;

class Target {
    private int x, y;
    private int radius;
    private Aim aim;
    private Color color = Color.RED;

    private boolean wasHit = false;
    private long hitTime = 0;
    private final long HIT_DURATION = 100;
    private final double HIT_THRESHOLD = 1.2;

    public Target(int x, int y, int radius, Aim aim) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.aim = aim;
    }

    public void update(ArrayList<Bullet> bullets) {
        // Сбрасываем цвет, если прошло достаточно времени
        if (wasHit && System.currentTimeMillis() - hitTime >= HIT_DURATION) {
            color = Color.RED;
            wasHit = false;
        }

        // Проверяем попадания только если мишень не в состоянии попадания
        if (!wasHit) {
            for (Bullet bullet : bullets) {
                if (bullet.isActive()) {
                    // Более строгая проверка попадания
                    if (bullet.hitsTarget(x, y, (int)(radius * HIT_THRESHOLD))) {
                        bullet.deactivate();

                        // Регистрируем попадание только если пуля была близко к центру
                        double distance = Math.sqrt(
                                Math.pow(bullet.getX() - x, 2) +
                                        Math.pow(bullet.getY() - y, 2)
                        );

                        if (distance <= radius * 1.5) { // Только если пуля действительно близко
                            if (aim.registerHit()) {
                                color = Color.GREEN;
                                wasHit = true;
                                hitTime = System.currentTimeMillis();
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        g.setColor(Color.BLACK);
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        g.setColor(Color.WHITE);
        g.fillOval(x - radius/4, y - radius/4, radius/2, radius/2);

        g.setColor(Color.BLACK);
        g.drawOval(x - radius/4, y - radius/4, radius/2, radius/2);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
}
