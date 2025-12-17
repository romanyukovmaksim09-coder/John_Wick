import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

class Bullet {
    private int x, y;
    private int lastX, lastY; // Предыдущая позиция для проверки линии движения
    private double angle;
    private final double speed = 150.0; // Уменьшена скорость для более точной проверки
    private boolean active = true;
    private BufferedImage image;
    private int id;

    // Размер пули для коллизии
    private final int BULLET_SIZE = 8;

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
            // Сохраняем предыдущую позицию
            lastX = x;
            lastY = y;

            // Двигаем пулю
            x += (int)(Math.cos(angle) * speed);
            y += (int)(Math.sin(angle) * speed);

            // Деактивируем пулю, если она вышла за пределы экрана
            /*if (x < -100 || x > 1100 || y < -100 || y > 900) {
                active = false;
            }*/
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

            // Отладочная линия движения (можно убрать)
            g2d.setColor(Color.RED);
            g2d.drawLine(-(int)(Math.cos(angle) * 20), -(int)(Math.sin(angle) * 20),
                    (int)(Math.cos(angle) * 20), (int)(Math.sin(angle) * 20));
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

    // Улучшенный метод для проверки попадания - проверяет всю линию движения
    public boolean hitsTarget(int targetX, int targetY, int targetRadius) {
        if (!active) return false;

        // 1. Проверка текущей позиции (как было)
        double currentDistance = Math.sqrt(Math.pow(x - targetX, 2) + Math.pow(y - targetY, 2));
        if (currentDistance <= (targetRadius + BULLET_SIZE/2)) {
            return true;
        }

        // 2. Проверка предыдущей позиции (на случай если пуля уже прошла через мишень)
        double lastDistance = Math.sqrt(Math.pow(lastX - targetX, 2) + Math.pow(lastY - targetY, 2));
        if (lastDistance <= (targetRadius + BULLET_SIZE/2)) {
            return true;
        }

        // 3. Проверка пересечения линии движения с окружностью мишени
        // Вектор движения пули
        int dx = x - lastX;
        int dy = y - lastY;

        // Вектор от начала движения к центру мишени
        int fx = lastX - targetX;
        int fy = lastY - targetY;

        // Решаем квадратное уравнение для нахождения точки пересечения
        double a = dx * dx + dy * dy;
        double b = 2 * (fx * dx + fy * dy);
        double c = fx * fx + fy * fy - (targetRadius + BULLET_SIZE/2) * (targetRadius + BULLET_SIZE/2);

        double discriminant = b * b - 4 * a * c;

        if (discriminant >= 0) {
            // Находим параметры пересечения
            discriminant = Math.sqrt(discriminant);
            double t1 = (-b - discriminant) / (2 * a);
            double t2 = (-b + discriminant) / (2 * a);

            // Если пересечение происходит на отрезке движения (t от 0 до 1)
            if ((t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1)) {
                return true;
            }
        }

        return false;
    }

    // Метод для получения линии движения (для отладки)
    public Line2D getMovementLine() {
        return new Line2D.Double(lastX, lastY, x, y);
    }
}