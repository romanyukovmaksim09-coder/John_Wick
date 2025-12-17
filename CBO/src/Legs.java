import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.HashMap;

class Legs {
    // Точки A, B, C
    private Point pointA; // Центр поворота левой ноги
    private Point pointB; // Центр поворота правой ноги
    private Point pointC; // Центр тела (середина между A и B)

    private HashMap<String, BufferedImage> images = new HashMap<>();

    // Полные последовательности (15 кадров)
    private String[] leftFullSequence = new String[15];
    private String[] rightFullSequence = new String[15];

    private BufferedImage leftImage;
    private BufferedImage rightImage;

    // ИЗМЕНЕНО: теперь double для плавной интерполяции
    private double leftFrameIndex = 0.0;
    private double rightFrameIndex = 0.0;
    private int animationCounter = 0;

    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;
    private boolean shiftPressed = false;
    private boolean isMoving = false;
    private boolean wasMoving = false;

    private double moveAngle = 0;
    private double mouseAngle = 0;
    private double perpAngle = 0;

    private final int LEG_DISTANCE = 15;
    private int legWidth, legHeight;

    // Смещения центров вращения
    private final int ROTATION_CENTER_OFFSET = 25;
    private final int RIGHT_IMAGE_OFFSET = 10;

    // Скорости
    private final int BASE_SPEED = 12;
    private final int RUN_SPEED = 22;

    // ИЗМЕНЕНО: новые настройки плавности
    private final double IDLE_ANIMATION_SPEED = 0.2; // Очень медленно для стояния
    private final double WALK_ANIMATION_SPEED = 0.5;  // Для плавной ходьбы
    private final double RUN_ANIMATION_SPEED = 1;   // Для бега

    // Флаг для отображения ног
    private boolean showLegs = false;

    // Для интерполяции между кадрами
    private BufferedImage prevLeftImage = null;
    private BufferedImage nextLeftImage = null;
    private BufferedImage prevRightImage = null;
    private BufferedImage nextRightImage = null;
    private double interpolationAlpha = 0.0; // 0.0 - предыдущий кадр, 1.0 - следующий кадр

    // Текущие координаты мыши
    private int mouseX = 500;
    private int mouseY = 500;

    public Legs(int startX, int startY) throws Exception {
        pointC = new Point(startX, startY);
        pointA = new Point();
        pointB = new Point();

        createSequences();
        loadImages();
        calculateGeometry();

        // Начинаем с противофазы
        rightFrameIndex = 7.0;
        showLegs = false;

        updateImages(); // Инициализируем изображения

        System.out.println("=== ПЛАВНАЯ АНИМАЦИЯ НОГ ===");
        System.out.println("Всего кадров для левой ноги: " + leftFullSequence.length);
        System.out.println("Всего кадров для правой ноги: " + rightFullSequence.length);
        System.out.println("Загружено изображений: " + images.size());
    }

    private void createSequences() {
        // ЛЕВАЯ НОГА (15 кадров: 1-8 вперед, затем 7-2 назад)
        for (int i = 0; i < 8; i++) {
            leftFullSequence[i] = "leftleg" + (i + 1);
        }
        for (int i = 8; i < 15; i++) {
            leftFullSequence[i] = "leftleg" + (15 - i);
        }

        // ПРАВАЯ НОГА (15 кадров: 1-8 вперед, затем 7-2 назад)
        for (int i = 0; i < 8; i++) {
            rightFullSequence[i] = "rightleg" + (i + 1);
        }
        for (int i = 8; i < 15; i++) {
            rightFullSequence[i] = "rightleg" + (15 - i);
        }
    }

    private void loadImages() throws Exception {
        String path = "data/";

        // ЛЕВАЯ НОГА: загружаем кадры leftleg1-leftleg8
        for (int i = 1; i <= 8; i++) {
            String frameName = "leftleg" + i;
            File file = new File(path + frameName + ".png");
            if (!file.exists()) file = new File(frameName + ".png");

            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(frameName, img);

                if (frameName.equals("leftleg1")) {
                    legWidth = img.getWidth() / 2;
                    legHeight = img.getHeight() / 2;
                }
                System.out.println("Загружен кадр: " + frameName);
            } else {
                throw new Exception("Не найден файл: " + frameName + ".png");
            }
        }

        // ПРАВАЯ НОГА: загружаем кадры rightleg1-rightleg8
        for (int i = 1; i <= 8; i++) {
            String frameName = "rightleg" + i;
            File file = new File(path + frameName + ".png");
            if (!file.exists()) file = new File(frameName + ".png");

            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(frameName, img);
                System.out.println("Загружен кадр: " + frameName);
            } else {
                throw new Exception("Не найден файл: " + frameName + ".png");
            }
        }

        System.out.println("Всего загружено " + images.size() + " уникальных кадров");
        System.out.println("Для полной анимации используется " + leftFullSequence.length + " кадров");
    }

    private BufferedImage getInterpolatedImage(BufferedImage img1, BufferedImage img2, double alpha) {
        if (img1 == null || img2 == null || alpha <= 0) return img1;
        if (alpha >= 1) return img2;

        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                // Интерполяция цвета
                int a1 = (rgb1 >> 24) & 0xFF;
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;

                int a2 = (rgb2 >> 24) & 0xFF;
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;

                int a = (int)(a1 * (1 - alpha) + a2 * alpha);
                int r = (int)(r1 * (1 - alpha) + r2 * alpha);
                int g = (int)(g1 * (1 - alpha) + g2 * alpha);
                int b = (int)(b1 * (1 - alpha) + b2 * alpha);

                int rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private void updateImages() {
        int prevLeftIndex = (int)Math.floor(leftFrameIndex) % leftFullSequence.length;
        int nextLeftIndex = (prevLeftIndex + 1) % leftFullSequence.length;

        int prevRightIndex = (int)Math.floor(rightFrameIndex) % rightFullSequence.length;
        int nextRightIndex = (prevRightIndex + 1) % rightFullSequence.length;

        String prevLeftName = leftFullSequence[prevLeftIndex];
        String nextLeftName = leftFullSequence[nextLeftIndex];
        String prevRightName = rightFullSequence[prevRightIndex];
        String nextRightName = rightFullSequence[nextRightIndex];

        // Проверяем, что изображения существуют
        if (!images.containsKey(prevLeftName)) {
            throw new RuntimeException("Изображение не найдено: " + prevLeftName);
        }
        if (!images.containsKey(nextLeftName)) {
            throw new RuntimeException("Изображение не найдено: " + nextLeftName);
        }
        if (!images.containsKey(prevRightName)) {
            throw new RuntimeException("Изображение не найдено: " + prevRightName);
        }
        if (!images.containsKey(nextRightName)) {
            throw new RuntimeException("Изображение не найдено: " + nextRightName);
        }

        prevLeftImage = images.get(prevLeftName);
        nextLeftImage = images.get(nextLeftName);

        prevRightImage = images.get(prevRightName);
        nextRightImage = images.get(nextRightName);

        // Интерполяционный коэффициент
        interpolationAlpha = leftFrameIndex - Math.floor(leftFrameIndex);

        // Создаем интерполированные изображения
        leftImage = getInterpolatedImage(prevLeftImage, nextLeftImage, interpolationAlpha);
        rightImage = getInterpolatedImage(prevRightImage, nextRightImage, interpolationAlpha);
    }

    public void setKeyPressed(int keyCode, boolean pressed) {
        switch(keyCode) {
            case KeyEvent.VK_W: wPressed = pressed; break;
            case KeyEvent.VK_A: aPressed = pressed; break;
            case KeyEvent.VK_S: sPressed = pressed; break;
            case KeyEvent.VK_D: dPressed = pressed; break;
            case KeyEvent.VK_SHIFT: shiftPressed = pressed; break;
        }

        boolean newIsMoving = wPressed || aPressed || sPressed || dPressed;

        if (newIsMoving != isMoving) {
            if (newIsMoving) {
                showLegs = true;
            } else {
                showLegs = false;
            }
        }

        isMoving = newIsMoving;

        if (isMoving) {
            double dx = 0, dy = 0;
            if (wPressed) dy -= 1;
            if (sPressed) dy += 1;
            if (aPressed) dx -= 1;
            if (dPressed) dx += 1;

            if (dx != 0 || dy != 0) {
                moveAngle = Math.atan2(dy, dx);
            }
        }
    }

    // ИЗМЕНЕНО: сохраняем координаты мыши всегда
    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        double dx = mouseX - pointC.x;
        double dy = mouseY - pointC.y;

        if (dx != 0 || dy != 0) {
            mouseAngle = Math.atan2(dy, dx);
            perpAngle = mouseAngle + Math.PI/2;
            // calculateGeometry(); // Вызывается в update()
        }
    }

    private void calculateGeometry() {
        // ИЗМЕНЕНО: всегда используем mouseAngle для расчета перпендикуляра
        perpAngle = mouseAngle + Math.PI/2;

        pointA.x = pointC.x + (int)(LEG_DISTANCE * Math.cos(perpAngle));
        pointA.y = pointC.y + (int)(LEG_DISTANCE * Math.sin(perpAngle));

        pointB.x = pointC.x - (int)(LEG_DISTANCE * Math.cos(perpAngle));
        pointB.y = pointC.y - (int)(LEG_DISTANCE * Math.sin(perpAngle));
    }

    public void update() {
        // ИЗМЕНЕНО: всегда обновляем угол мыши
        double dx = mouseX - pointC.x;
        double dy = mouseY - pointC.y;
        if (dx != 0 || dy != 0) {
            mouseAngle = Math.atan2(dy, dx);
        }

        // Движение центра
        if (isMoving) {
            int speed = shiftPressed ? RUN_SPEED : BASE_SPEED;
            pointC.x += (int)(Math.cos(moveAngle) * speed);
            pointC.y += (int)(Math.sin(moveAngle) * speed);
        }

        // ПЛАВНАЯ АНИМАЦИЯ
        if (showLegs) {
            double animationSpeed;

            if (isMoving) {
                animationSpeed = shiftPressed ? RUN_ANIMATION_SPEED : WALK_ANIMATION_SPEED;
            } else {
                // Медленная анимация даже при стоянии (если ноги видны)
                animationSpeed = IDLE_ANIMATION_SPEED;
            }

            // Плавное увеличение индексов
            leftFrameIndex += animationSpeed;
            rightFrameIndex += animationSpeed;

            // Зацикливание (15 кадров)
            if (leftFrameIndex >= leftFullSequence.length) {
                leftFrameIndex -= leftFullSequence.length;
            }
            if (rightFrameIndex >= rightFullSequence.length) {
                rightFrameIndex -= rightFullSequence.length;
            }

            // Обновляем изображения с интерполяцией
            updateImages();
        } else {
            // Если ноги скрыты, сбрасываем к начальным позициям
            leftFrameIndex = 0.0;
            rightFrameIndex = 7.0;
            updateImages();
        }

        // ИЗМЕНЕНО: всегда пересчитываем геометрию
        calculateGeometry();
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        if (!showLegs) {
            return;
        }

        AffineTransform original = g2d.getTransform();

        // Левая нога
        if (leftImage != null) {
            g2d.translate(pointA.x, pointA.y);
            g2d.rotate(moveAngle);

            int rotationOffset = getLegRotationOffset((int)Math.floor(leftFrameIndex), true);
            int drawX = -legWidth/2 + rotationOffset;
            int drawY = -legHeight/2;

            g2d.drawImage(leftImage, drawX, drawY, legWidth / 3 * 2, legHeight * 8 / 11, null);

            g2d.setTransform(original);
        }

        // Правая нога
        if (rightImage != null) {
            g2d.translate(pointB.x, pointB.y);
            g2d.rotate(moveAngle);

            int rotationOffset = getLegRotationOffset((int)Math.floor(rightFrameIndex), false);
            int drawX = -legWidth/2 + rotationOffset + RIGHT_IMAGE_OFFSET;
            int drawY = -legHeight/2;

            g2d.drawImage(rightImage, drawX, drawY, legWidth / 3 * 2, legHeight * 8 / 11, null);

            g2d.setTransform(original);
        }
    }

    private int getLegRotationOffset(int frameIndex, boolean isLeft) {
        String[] sequence = isLeft ? leftFullSequence : rightFullSequence;
        frameIndex = frameIndex % sequence.length;
        String frameName = sequence[frameIndex];

        // Извлекаем номер кадра из имени (например, "leftleg3" -> 3)
        int frameNumber = Integer.parseInt(frameName.substring(isLeft ? 7 : 8));

        if (frameNumber <= 4) {
            return -ROTATION_CENTER_OFFSET;
        } else {
            return ROTATION_CENTER_OFFSET;
        }
    }

    // Геттеры
    public Point getPointA() { return new Point(pointA); }
    public Point getPointB() { return new Point(pointB); }
    public Point getPointC() { return new Point(pointC); }
    public int getCenterX() { return pointC.x; }
    public int getCenterY() { return pointC.y; }
    public boolean isMoving() { return isMoving; }
    public boolean isShowingLegs() { return showLegs; }
    public int getCurrentSpeed() { return shiftPressed ? RUN_SPEED : (isMoving ? BASE_SPEED : 0); }
    public boolean isAlternatePhase() { return false; }
    public int getRightImageOffset() { return RIGHT_IMAGE_OFFSET; }

    // НОВЫЕ геттеры для отладки
    public double getLeftFrameIndex() { return leftFrameIndex; }
    public double getRightFrameIndex() { return rightFrameIndex; }
    public double getInterpolationAlpha() { return interpolationAlpha; }
    public String[] getLeftSequence() { return leftFullSequence; }
    public String[] getRightSequence() { return rightFullSequence; }

    // Новый геттер для угла между ногами
    public double getLegsAngle() {
        double dx = pointB.x - pointA.x;
        double dy = pointB.y - pointA.y;
        return Math.atan2(dy, dx);
    }
}