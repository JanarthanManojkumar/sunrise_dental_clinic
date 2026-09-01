package sunrise_dental_clinic_backend;

import javax.swing.SwingUtilities;
import util.UiTheme;
import view.LoginView;

public class Sunrise_dental_clinic_backend {

    public static void main(String[] args) {
        UiTheme.apply();
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }

}
