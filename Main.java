import mdlaf.MaterialLookAndFeel;
import javax.swing.UIManager;
import ui.MainFrame;

public class Main{

    public static void main(String args[])
    {
        // Mencoba menghubungkan ke database
        try
        {
            // Aktifkan tema material design
            UIManager.setLookAndFeel (new MaterialLookAndFeel ());
            new MainFrame();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}