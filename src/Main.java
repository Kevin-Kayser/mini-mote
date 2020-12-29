import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class Main {

	private int height = 320;
	private int width = 240;
	private boolean normOrientation = true;
	JMenuBar mb = new JMenuBar();
	JMenu things = new JMenu("Things");
	JMenu stuff = new JMenu("Stuff");
    TVFrame tv = new TVFrame();
	JCheckBoxMenuItem orient = new JCheckBoxMenuItem("Horz. Orientation");
	
	
	Simulator sim = new Simulator(height, width, normOrientation, tv);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}//end main Method

	
	public void run() {
		
        JFrame frame = new JFrame("Mini-Mote");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //menuSetup(frame);
        frame.setBackground(Color.WHITE);

        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(sim, BorderLayout.CENTER);
        //frame.getContentPane().add(new JEditorPane(), BorderLayout.EAST);


        //frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setMinimumSize(new Dimension(height, width+20));
        tv.setLocationRelativeTo(null);
       // frame.setLocationRelativeTo();
        tv.setVisible(true);
        frame.setVisible(true);
        

             
	}
	
	
	
}// end Main
