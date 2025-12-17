import java.awt.*;
import java.util.ArrayList;

class Target {
    private int x, y;
    private int radius;
    private Aim aim;
    private Color color = Color.RED;

    public Target(int x, int y, int radius, Aim aim) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.aim = aim;
    }

    public void update(ArrayList<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                // Используем улучшенную проверку попадания
                if (bullet.hitsTarget(x, y, radius)) {
                    bullet.deactivate();
                    aim.registerHit();
                    color = Color.GREEN;

                    // Сбрасываем цвет через 100 мс
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                public void run() {
                                    color = Color.RED;
                                }
                            },
                            100
                    );
                    break; // Обрабатываем только одно попадание за кадр
                }
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setColor(Color.BLACK);
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        //g.drawString("Мишень", x - 20, y - radius - 5);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
}