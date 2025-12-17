import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class Body {
    private int x, y; // Координаты точки C
    private BufferedImage imageBody;
    private double lookAngle = 0;
    private int mouseX = 500;
    private int mouseY = 500;

    private int bodyWidth, bodyHeight;

    // ДОБАВЛЕНО: смещение для корректировки центра тела
    private final int BODY_CENTER_OFFSET = 50;

    public Body() throws Exception {
        loadImage();
        x = 500;
        y = 500;
    }

    private void loadImage() throws Exception {
        File heroFile = new File("data/hero.png");
        if (!heroFile.exists()) heroFile = new File("hero.png");

        if (heroFile.exists()) {
            imageBody = ImageIO.read(heroFile);
            bodyWidth = imageBody.getWidth() / 3;
            bodyHeight = imageBody.getHeight() / 3;
            //System.out.println("Тело: 3x меньше (" + bodyWidth + "x" + bodyHeight + ")");
        } else {
            throw new Exception("Файл hero.png не найден!");
        }
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        double dx = mouseX - x;
        double dy = mouseY - y;
        if (Math.sqrt(dx*dx + dy*dy) > 0.1) {
            lookAngle = Math.atan2(dy, dx);
        }
    }

    public void update() {
        double dx = mouseX - x;
        double dy = mouseY - y;
        if (Math.sqrt(dx*dx + dy*dy) > 0.1) {
            lookAngle = Math.atan2(dy, dx);
        }
    }

    public void setPositionFromLegs(int pointCX, int pointCY) {
        // Тело рисуется точно в точке C
        this.x = pointCX;
        this.y = pointCY;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        // Рисуем тело в точке C
        g2d.translate(x, y);
        g2d.rotate(lookAngle);

        if (imageBody != null) {
            int width = bodyWidth;
            int height = bodyHeight;

            // ДОБАВЛЕНО: корректировка положения тела
            // Сдвигаем тело вправо на 20 пикселей, чтобы компенсировать смещение центра
            int drawX = -width/2 + BODY_CENTER_OFFSET;  // Смещение вправо
            int drawY = -height/2;

            g2d.drawImage(imageBody, drawX, drawY, width, height, null);

            // Отладочная точка центра тела (точка C)
            drawDebugCenter(g2d);
        }

        g2d.setTransform(originalTransform);
    }

    private void drawDebugCenter(Graphics2D g2d) {
        // Зеленая точка в центре тела (точка C)
        g2d.setColor(Color.GREEN);
        g2d.fillOval(-6, -6, 12, 12);

        // Текст
        g2d.setColor(Color.BLACK);
        //g2d.drawString("Точка C", 10, -10);

        // ДОБАВЛЕНО: линия, показывающая смещение
        g2d.setColor(new Color(255, 0, 0, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, 0, BODY_CENTER_OFFSET, 0);
        g2d.setStroke(new BasicStroke(1));

        // Текст смещения
        g2d.setColor(Color.RED);
        //g2d.drawString("Смещение: " + BODY_CENTER_OFFSET + "px", 25, -15);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getLookAngle() { return lookAngle; }
}