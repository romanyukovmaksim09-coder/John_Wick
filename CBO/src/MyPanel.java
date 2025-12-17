import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class MyPanel extends JPanel {
    private Body body;
    private Legs legs;
    private Aim aim;
    private Target target;
    private Timer timer;

    private boolean rightMousePressed = false;
    private boolean leftMousePressed = false;
    private int mouseX = 500;
    private int mouseY = 500;

    // Для отображения FPS
    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

    public MyPanel(Body body, Legs legs, Aim aim) {
        this.body = body;
        this.legs = legs;
        this.aim = aim;

        target = new Target(700, 400, 50, aim);

        timer = new Timer(16, e -> {
            // Обновляем ноги
            legs.update();

            // Позиционируем тело в точке C ног
            Point pointC = legs.getPointC();
            body.setPositionFromLegs(pointC.x, pointC.y);
            body.update();

            // Обновляем прицел
            aim.updatePosition(
                    body.getX(),
                    body.getY(),
                    mouseX,
                    mouseY,
                    rightMousePressed,
                    leftMousePressed,
                    body.getLookAngle()
            );

            // Обновляем мишень
            target.update(aim.getBullets());

            // Считаем FPS
            updateFPS();

            repaint();
        });
        timer.start();

        setupMouseListeners();
        setupKeyListeners();

        setFocusable(true);
        requestFocusInWindow();
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                legs.setMousePosition(e.getX(), e.getY());
                body.setMousePosition(e.getX(), e.getY());
            }

            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
                if (SwingUtilities.isRightMouseButton(e)) rightMousePressed = true;
                if (SwingUtilities.isLeftMouseButton(e)) leftMousePressed = true;
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) rightMousePressed = true;
                if (SwingUtilities.isLeftMouseButton(e)) leftMousePressed = true;
                requestFocusInWindow();
            }

            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) rightMousePressed = false;
                if (SwingUtilities.isLeftMouseButton(e)) leftMousePressed = false;
                requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                legs.setKeyPressed(e.getKeyCode(), true);
            }

            public void keyReleased(KeyEvent e) {
                legs.setKeyPressed(e.getKeyCode(), false);
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон
        g2d.setColor(new Color(240, 240, 245));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Рисуем сетку фона (опционально)
        drawGrid(g2d);

        // Рисуем ноги (под телом)
        legs.draw(g2d);

        // Рисуем тело в точке C
        body.draw(g2d);

        // Рисуем мишень
        target.draw(g2d);

        // Рисуем прицел поверх всего
        aim.draw(g2d);

        // Отладочная информация
        drawDebugInfo(g2d);

        // UI информация
        drawUIInfo(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(220, 220, 230));
        int gridSize = 50;

        // Вертикальные линии
        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }

        // Горизонтальные линии
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawDebugInfo(Graphics2D g2d) {
        Point pointA = legs.getPointA();
        Point pointB = legs.getPointB();
        Point pointC = legs.getPointC();

        // Рисуем линии между точками
        g2d.setColor(new Color(255, 200, 0, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(pointA.x, pointA.y, pointC.x, pointC.y);
        g2d.drawLine(pointC.x, pointC.y, pointB.x, pointB.y);
        g2d.setStroke(new BasicStroke(1));

        // Подписи точек
        //g2d.setFont(new Font("Monospaced", Font.BOLD, 12));

        // Точка A (левая нога)
        g2d.setColor(Color.RED);
        g2d.fillOval(pointA.x - 4, pointA.y - 4, 8, 8);
        g2d.setColor(Color.BLACK);
       // g2d.drawString("A (левая)", pointA.x + 10, pointA.y - 5);

        // Точка B (правая нога)
        g2d.setColor(Color.BLUE);
        g2d.fillOval(pointB.x - 4, pointB.y - 4, 8, 8);
        g2d.setColor(Color.BLACK);
       // g2d.drawString("B (правая)", pointB.x + 10, pointB.y - 5);

        // Точка C (центр тела)
        g2d.setColor(Color.GREEN);
        g2d.fillOval(pointC.x - 6, pointC.y - 6, 12, 12);
        g2d.setColor(Color.BLACK);
       // g2d.drawString("C (центр)", pointC.x + 10, pointC.y - 5);
    }

    private void drawUIInfo(Graphics2D g2d) {
        // Полупрозрачная панель для информации
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(10, 10, 350, 250, 10, 10);
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawRoundRect(10, 10, 350, 250, 10, 10);

        // Шрифты
        //Font headerFont = new Font("Arial", Font.BOLD, 16);
        //Font normalFont = new Font("Arial", Font.PLAIN, 12);
        //Font monospacedFont = new Font("Monospaced", Font.PLAIN, 12);

        //g2d.setFont(headerFont);
        g2d.setColor(Color.BLACK);
        //g2d.drawString("ИНФОРМАЦИЯ О СИСТЕМЕ", 20, 30);

        //g2d.setFont(normalFont);
        int y = 50;

        // Координаты точек
        Point pointC = legs.getPointC();
        //g2d.drawString("Центр тела (C): " + pointC.x + ", " + pointC.y, 20, y);
        y += 20;

        // Состояние движения
        //String movementState = legs.isMoving() ?
         //       "ДВИЖЕТСЯ (" + legs.getCurrentSpeed() + "px/кадр)" : "СТОИТ";
        //g2d.setColor(legs.isMoving() ? new Color(0, 150, 0) : Color.GRAY);
        //g2d.drawString("Состояние: " + movementState, 20, y);
        y += 20;

        //g2d.setColor(Color.BLACK);

        // Информация о ногах


        // Статистика стрельбы
        int bulletsFired = aim.getBulletsFired();
        int bulletsHit = aim.getBulletsHit();
        //g2d.drawString("Выстрелы: " + bulletsFired, 20, y);
        y += 20;
        //g2d.drawString("Попадания: " + bulletsHit, 20, y);
        y += 20;

        if (bulletsFired > 0) {
            double accuracy = (double) bulletsHit / bulletsFired * 100;
            Color accuracyColor;
            if (accuracy >= 80) accuracyColor = new Color(0, 180, 0);
            else if (accuracy >= 50) accuracyColor = new Color(255, 165, 0);
            else accuracyColor = Color.RED;

            g2d.setColor(accuracyColor);
            //g2d.drawString("Точность: " + String.format("%.1f", accuracy) + "%", 20, y);
            y += 20;
        }

        // FPS
        g2d.setColor(Color.BLUE);
        //g2d.drawString("FPS: " + fps, 20, y);
        y += 20;

        // Управление
        g2d.setColor(new Color(80, 80, 80));
       // g2d.drawString("Управление: WASD - движение, Shift - бег", 20, y);
        y += 15;
        //g2d.drawString("Мышь: ЛКМ - выстрел, ПКМ - прицеливание", 20, y);
    }

    private void updateFPS() {
        frames++;
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - lastTime;

        if (elapsedTime >= 1_000_000_000) { // 1 секунда
            fps = frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    // Метод для получения FPS (может быть полезен)
    public int getFPS() {
        return fps;
    }

    // Метод для остановки таймера при закрытии окна
    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
}