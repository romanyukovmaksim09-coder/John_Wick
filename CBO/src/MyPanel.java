import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class MyPanel extends JPanel {
    private Body body;
    private Legs legs;
    private Aim aim;
    private Target target;
    private Timer timer;
    private ArrayList<Wall> walls = new ArrayList<>();

    private boolean rightMousePressed = false;
    private boolean leftMousePressed = false;
    private int screenMouseX = 500;
    private int screenMouseY = 500;
    private int worldMouseX = 500;
    private int worldMouseY = 500;

    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

    private double cameraX = 500;
    private double cameraY = 500;
    private double targetCameraX = 500;
    private double targetCameraY = 500;
    private final double CAMERA_LERP_SPEED = 0.2;
    private final double OFFSET_PERCENTAGE = 0.2;

    public MyPanel(Body body, Legs legs, Aim aim) {
        this.body = body;
        this.legs = legs;
        this.aim = aim;

        target = new Target(500, 200, 50, aim);

        try {
            walls.add(new Wall(200, 200, 100, 100));
            walls.add(new Wall(500, 400, 100, 100));
            walls.add(new Wall(100, 600, 100, 100));
            walls.add(new Wall(400, 100, 100, 100));
            walls.add(new Wall(700, 300, 100, 100));
            walls.add(new Wall(300, 500, 100, 100));
            walls.add(new Wall(600, 100, 100, 100));
            walls.add(new Wall(800, 500, 100, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        timer = new Timer(16, e -> {
            updateWorldMouseCoordinates();

            legs.update(walls);

            Point pointC = legs.getPointC();
            body.setPositionFromLegs(pointC.x, pointC.y);
            body.update();

            aim.updatePosition(
                    body.getX(),
                    body.getY(),
                    worldMouseX,
                    worldMouseY,
                    rightMousePressed,
                    leftMousePressed,
                    body.getLookAngle(),
                    walls
            );

            target.update(aim.getBullets());

            updateCamera();

            updateFPS();

            repaint();
        });
        timer.start();

        setupMouseListeners();
        setupKeyListeners();

        setFocusable(true);
        requestFocusInWindow();
    }

    private void updateWorldMouseCoordinates() {
        worldMouseX = screenMouseX + (int)cameraX - getWidth()/2;
        worldMouseY = screenMouseY + (int)cameraY - getHeight()/2;
    }

    private Point worldToScreen(int worldX, int worldY) {
        int screenX = worldX - (int)cameraX + getWidth()/2;
        int screenY = worldY - (int)cameraY + getHeight()/2;
        return new Point(screenX, screenY);
    }

    private Point screenToWorld(int screenX, int screenY) {
        int worldX = screenX + (int)cameraX - getWidth()/2;
        int worldY = screenY + (int)cameraY - getHeight()/2;
        return new Point(worldX, worldY);
    }

    private void updateCamera() {
        int bodyX = body.getX();
        int bodyY = body.getY();

        if (rightMousePressed) {
            double dx = worldMouseX - bodyX;
            double dy = worldMouseY - bodyY;

            double offsetX = dx * OFFSET_PERCENTAGE;
            double offsetY = dy * OFFSET_PERCENTAGE;

            targetCameraX = bodyX + offsetX;
            targetCameraY = bodyY + offsetY;
        } else {
            targetCameraX = bodyX;
            targetCameraY = bodyY;
        }

        cameraX += (targetCameraX - cameraX) * CAMERA_LERP_SPEED;
        cameraY += (targetCameraY - cameraY) * CAMERA_LERP_SPEED;
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                screenMouseX = e.getX();
                screenMouseY = e.getY();

                Point worldPoint = screenToWorld(screenMouseX, screenMouseY);
                worldMouseX = worldPoint.x;
                worldMouseY = worldPoint.y;

                legs.setMousePosition(worldMouseX, worldMouseY);
                body.setMousePosition(worldMouseX, worldMouseY);
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(240, 240, 245));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2dTransformed = (Graphics2D) g2d.create();
        g2dTransformed.translate(getWidth()/2 - cameraX, getHeight()/2 - cameraY);

        drawGrid(g2dTransformed);

        for (Wall wall : walls) {
            wall.draw(g2dTransformed);
        }

        legs.draw(g2dTransformed);

        body.draw(g2dTransformed);

        target.draw(g2dTransformed);

        aim.draw(g2dTransformed);

        g2dTransformed.dispose();
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(220, 220, 230));
        int gridSize = 50;

        for (int x = -1000; x < 2000; x += gridSize) {
            g2d.drawLine(x, -1000, x, 2000);
        }

        for (int y = -1000; y < 2000; y += gridSize) {
            g2d.drawLine(-1000, y, 2000, y);
        }
    }

    private void updateFPS() {
        frames++;
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - lastTime;

        if (elapsedTime >= 1_000_000_000) {
            fps = frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    public int getFPS() {
        return fps;
    }

    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
}
