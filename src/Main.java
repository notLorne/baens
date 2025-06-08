import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                GuiWindow window = new GuiWindow();
                window.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace(); // Or show a dialog, log it, whatever
                System.exit(1); // Kill the app if you can't build the UI
            }
        });
    }
}
