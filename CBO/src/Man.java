/*import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Man {
    int x;
    int y;
    int x1;
    int y1;
    int vx;
    int vy;
    BufferedImage image;
    private double currentAngle = 0; // Текущий угол для плавности
    private static final double ROTATION_SPEED = 0.1; // Скорость поворота

    public Man() throws IOException {
        // Загружаем изображение при создании объекта
        this.image = ImageIO.read(new File("data\\hero.jpg"));
        // Инициализируем значения по умолчанию
        this.x = 100;
        this.y = 100;
        this.x1 = 100;
        this.y1 = 100;
        this.vx = 0;
        this.vy = 0;
    }

    public void updatePosition() {
        this.vx = (this.x1 - this.x) / 40;
        this.vy = (this.y1 - this.y) / 40;
        if (Math.abs(this.vx) * 40 >= 100 || Math.abs(this.vy) * 40 >= 100) {
            this.x += this.vx;
            this.y += this.vy;
        }

        // Плавное обновление угла
        if (vx != 0 || vy != 0) {
            double targetAngle = Math.atan2(vy, vx);
            // Плавный переход к целевому углу
            double angleDiff = targetAngle - currentAngle;
            // Корректируем разницу углов для кратчайшего пути
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            currentAngle += angleDiff * ROTATION_SPEED;
        }
    }

    public void draw(Graphics g) {
        try {
            Graphics2D g2d = (Graphics2D) g;

            // Сохраняем оригинальные настройки
            AffineTransform originalTransform = g2d.getTransform();

            // Увеличиваем размеры отрисовки в 3 раза
            int drawWidth = image.getWidth() / 2;
            int drawHeight = image.getHeight() / 2;

            // Перемещаем в позицию отрисовки
            g2d.translate(x, y);
            // Поворачиваем вокруг центра изображения
            g2d.rotate(currentAngle);

            // Устанавливаем качественный рендеринг
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Рисуем изображение по центру с увеличенным размером
            g2d.drawImage(image, -drawWidth/2, -drawHeight/2, drawWidth, drawHeight, null);

            // Восстанавливаем оригинальные настройки
            g2d.setTransform(originalTransform);

        } catch (Exception e) {
            // Если не удалось нарисовать изображение, рисуем простой круг
            g.setColor(Color.RED);
            g.fillOval(x - 25, y - 25, 50, 50);
            g.setColor(Color.BLACK);
            g.drawString("Error", x - 15, y);
        }
    }
}*/
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Man {
    int x;
    int y;
    int x1;
    int y1;
    int vx;
    int vy;
    BufferedImage imageHero;    // состояние покоя
    BufferedImage imageHero1;   // шаг 1
    BufferedImage imageHero2;   // шаг 2
    BufferedImage imageHero3;   // шаг 3
    BufferedImage imageHero4;   // шаг 4
    private double currentAngle = 0;
    private static final double ROTATION_SPEED = 0.1;
    private boolean isMoving = false;
    private int stepCounter = 0;
    // ⚡ ИЗМЕНЕНИЕ: Уменьшил задержку для более быстрых шагов
    private static final int STEP_DELAY = 8; // Было 15, стало 8 (в 2 раза быстрее)
    private int currentStep = 0;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    public Man() throws IOException {
        // Загружаем все изображения для анимации
        this.imageHero = loadImage("hero");   // покой
        this.imageHero1 = loadImage("hero1"); // шаг 1
        this.imageHero2 = loadImage("hero2"); // шаг 2
        this.imageHero3 = loadImage("hero3"); // шаг 3
        this.imageHero4 = loadImage("hero4"); // шаг 4

        // Если не нашли файлы, создаем изображения по умолчанию
        if (imageHero == null) imageHero = createDefaultImage(Color.BLUE, "Stand");
        if (imageHero1 == null) imageHero1 = createDefaultImage(Color.GREEN, "Step1");
        if (imageHero2 == null) imageHero2 = createDefaultImage(Color.RED, "Step2");
        if (imageHero3 == null) imageHero3 = createDefaultImage(Color.ORANGE, "Step3");
        if (imageHero4 == null) imageHero4 = createDefaultImage(Color.MAGENTA, "Step4");

        // Инициализируем значения по умолчанию
        this.x = 100;
        this.y = 100;
        this.x1 = 100;
        this.y1 = 100;
        this.vx = 0;
        this.vy = 0;
        this.lastMouseX = x1;
        this.lastMouseY = y1;
    }

    private BufferedImage loadImage(String baseName) {
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif"};
        String[] paths = {
                "data\\" + baseName,
                baseName
        };

        for (String path : paths) {
            for (String ext : extensions) {
                try {
                    File file = new File(path + ext);
                    if (file.exists()) {
                        System.out.println("Загружен файл: " + file.getPath());
                        return ImageIO.read(file);
                    }
                } catch (IOException e) {
                    // Продолжаем пробовать другие варианты
                }
            }
        }
        System.out.println("Файл " + baseName + " не найден");
        return null;
    }

    private BufferedImage createDefaultImage(Color color, String text) {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        // Рисуем простого персонажа
        g2d.setColor(color);
        g2d.fillOval(10, 10, 80, 80);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(30, 30, 20, 20);
        g2d.fillOval(50, 30, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 35, 70);

        g2d.dispose();
        return img;
    }

    public void updatePosition() {
        // Проверяем, двигалась ли мышь
        boolean mouseMoved = (x1 != lastMouseX || y1 != lastMouseY);

        // Сохраняем текущие координаты мыши для следующего обновления
        lastMouseX = x1;
        lastMouseY = y1;

        // Вычисляем скорость
        this.vx = (this.x1 - this.x) / 40;
        this.vy = (this.y1 - this.y) / 40;

        // Проверяем, движется ли персонаж
        double distanceToTarget = Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y));
        isMoving = distanceToTarget > 20 && mouseMoved;

        if (isMoving) {
            // Двигаемся к цели
            this.x += this.vx;
            this.y += this.vy;

            // ⚡ ИЗМЕНЕНИЕ: Более быстрая анимация шагов
            stepCounter++;
            if (stepCounter >= STEP_DELAY) {
                // Сложная последовательность: hero4, hero1, hero4, hero, hero3, hero2, hero3, hero...
                currentStep = (currentStep + 1) % 8; // 8 шагов в цикле
                stepCounter = 0;
            }
        } else {
            // Стоим на месте - сбрасываем анимацию
            stepCounter = 0;
            currentStep = 0;
        }

        // Плавное обновление угла
        if (isMoving && (vx != 0 || vy != 0)) {
            double targetAngle = Math.atan2(vy, vx);
            double angleDiff = targetAngle - currentAngle;
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            currentAngle += angleDiff * ROTATION_SPEED;
        }
    }

    public void draw(Graphics g) {
        try {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalTransform = g2d.getTransform();

            // Выбираем изображение в зависимости от состояния и шага
            BufferedImage currentImage;
            if (isMoving) {
                // ⚡ ИЗМЕНЕНИЕ: Та же последовательность, но выполняется быстрее
                // Сложная последовательность шагов:
                switch (currentStep) {
                    case 0: currentImage = imageHero4; break; // hero4
                    case 1: currentImage = imageHero1; break; // hero1
                    case 2: currentImage = imageHero4; break; // hero4
                    case 3: currentImage = imageHero; break;  // hero
                    case 4: currentImage = imageHero3; break; // hero3
                    case 5: currentImage = imageHero2; break; // hero2
                    case 6: currentImage = imageHero3; break; // hero3
                    case 7: currentImage = imageHero; break;  // hero
                    default: currentImage = imageHero; break;
                }
            } else {
                // При стоянии используем hero.jpg
                currentImage = imageHero;
            }

            // Увеличиваем размер
            int drawWidth = currentImage.getWidth() / 2;
            int drawHeight = currentImage.getHeight() / 2;

            // Перемещаем и поворачиваем
            g2d.translate(x, y);
            g2d.rotate(currentAngle);

            // Качественный рендеринг
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Рисуем изображение
            g2d.drawImage(currentImage, -drawWidth/2, -drawHeight/2, drawWidth, drawHeight, null);

            g2d.setTransform(originalTransform);

        } catch (Exception e) {
            g.setColor(Color.RED);
            g.fillOval(x - 25, y - 25, 50, 50);
            g.setColor(Color.BLACK);
            g.drawString("Error", x - 15, y);
        }
    }
}