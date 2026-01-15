import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class Wall {
    private int x, y;
    private BufferedImage imageWall;
    private int width, height;
    private Rectangle collisionBox;

    private static BufferedImage staticWallImage = null;

    static {
        try {
            File wallFile = new File("data/wall.png");
            if (!wallFile.exists()) wallFile = new File("wall.png");

            if (wallFile.exists()) {
                staticWallImage = ImageIO.read(wallFile);
            }
        } catch (Exception e) {
            System.out.println("Не удалось загрузить изображение стены: " + e.getMessage());
        }
    }

    public Wall(int x, int y, int width, int height) throws Exception {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Увеличиваем зону коллизии для надежности
        int padding = 3; // Увеличено с 1 до 3 пикселей
        collisionBox = new Rectangle(
                x - padding,
                y - padding,
                width + padding * 2,
                height + padding * 2
        );

        loadImage();
    }

    private void loadImage() throws Exception {
        if (staticWallImage != null) {
            imageWall = staticWallImage;
        } else {
            File wallFile = new File("data/wall.png");
            if (!wallFile.exists()) wallFile = new File("wall.png");

            if (wallFile.exists()) {
                imageWall = ImageIO.read(wallFile);
            } else {
                imageWall = createPlaceholderImage();
            }
        }
    }

    private BufferedImage createPlaceholderImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(139, 69, 19),
                width, height, new Color(101, 67, 33),
                true
        );

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.setStroke(new BasicStroke(2));

        int brickHeight = 16;
        for (int i = 0; i < height; i += brickHeight) {
            g2d.drawLine(0, i, width, i);
        }

        int brickWidth = 32;
        for (int row = 0; row < height; row += brickHeight) {
            boolean offset = (row / brickHeight) % 2 == 0;
            for (int i = 0; i < width; i += brickWidth) {
                int xPos = offset ? i + brickWidth/2 : i;
                if (xPos < width) {
                    g2d.drawLine(xPos, row, xPos, Math.min(row + brickHeight, height));
                }
            }
        }

        g2d.dispose();
        return img;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (imageWall != null) {
            g2d.drawImage(imageWall, x, y, width, height, null);
        }
    }

    // Проверка столкновения с прямоугольником
    public boolean collidesWith(Rectangle otherRect) {
        if (collisionBox == null || otherRect == null) return false;
        return collisionBox.intersects(otherRect);
    }

    // НОВЫЙ МЕТОД: Проверка столкновения с точкой
    public boolean collidesWithPoint(int px, int py) {
        if (collisionBox == null) return false;
        return collisionBox.contains(px, py);
    }

    // Геттеры
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getCollisionBox() { return collisionBox; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        collisionBox.setLocation(x, y);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        collisionBox.setSize(width, height);
    }
}
