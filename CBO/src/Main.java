import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Тело и Ноги с Прицелом");
                Body body = new Body();
                Legs legs = new Legs(500, 300);
                Aim aim = new Aim();

                frame.setSize(1000, 800);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                MyPanel panel = new MyPanel(body, legs, aim);
                frame.add(panel);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}