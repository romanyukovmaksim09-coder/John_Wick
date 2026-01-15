import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class Body {
    private int x, y;
    private BufferedImage imageBody;
    private double lookAngle = 0;
    private int mouseX = 500;
    private int mouseY = 500;

    private int bodyWidth, bodyHeight;

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
        this.x = pointCX;
        this.y = pointCY;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        g2d.translate(x, y);
        g2d.rotate(lookAngle);

        if (imageBody != null) {
            int width = bodyWidth;
            int height = bodyHeight;

            int drawX = -width/2 + BODY_CENTER_OFFSET;
            int drawY = -height/2;

            g2d.drawImage(imageBody, drawX, drawY, width, height, null);
        }

        g2d.setTransform(originalTransform);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getLookAngle() { return lookAngle; }
}
