import javax.imageio.ImageIO;
import javax.swing.*;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

public class Simulator extends JPanel implements MouseListener {

	private final boolean DEBUG = false;
	
	private int height;
	private int width;
	static final long serialVersionUID = 1;
	boolean georgeClooney[][];
	boolean on = false;
	StateMachine theClooninator = new StateMachine();
	BufferedImage georgeClooneysFace;
	BufferedImage test = new BufferedImage(240, 320, BufferedImage.TYPE_INT_ARGB);
	BufferedImage oldimage;
	float opacity = 1.0f;
	java.awt.Font myFont = null;
	java.awt.Font defaultFont = new Font( "", Font.PLAIN, 12);
	TVFrame tv;
	private AudioStream as;
	
	public Simulator(int w, int h, boolean b, TVFrame tv) {
		if(!DEBUG){
			try{
				PrintStream logfile = new PrintStream(new FileOutputStream("log.csv", true)); 
				System.setOut(logfile);
			}catch(Exception e){
			}
		}
		System.out.println(System.currentTimeMillis() + ", Simulation started");
		this.tv = tv;
		theClooninator.tv = tv;
		theClooninator.map.put("Main", new Vector<Transition>());
		theClooninator.map.get("Main").add(new Transition("TV", "OPENDEVICE()", "John's TV"));
		try 
		{
			InputStream fontStream = this.getClass().getResourceAsStream("Fonts/CALIBRI.TTF");
			myFont = java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, fontStream  );
			myFont = myFont.deriveFont( (float) 25 );
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (b) {
			width = h;
			height = w;
		}
		else {
			width = w;
			height = h;
		}
		this.addMouseListener(this);
		this.setPreferredSize(new Dimension(width + 40, height + 40));
		theClooninator.load();
		georgeClooney = theClooninator.getDefaultGeorgecloony();
	}

	public void paintComponent(Graphics g) {
		
		super.paintComponents(g);
		drawForm(g);
		drawSquares(g);
	
		if(DEBUG){
			drawGrid(g);
		}
	}

	private void drawForm(Graphics g) {
		
		
		g.setColor(Color.GRAY);
		g.fillRoundRect(10, 10, width + 20, height + 20, 20, 20);
		
		g.setColor(Color.black);
		g.drawRoundRect(10, 10, width+19, height+19, 20, 20);
		g.setColor(Color.WHITE);
		g.fillRect(20, 20, width, height);

		//Round Rectum
		g.setColor(Color.GRAY);
		g.fillRoundRect(width+43, 10, 23, height+20, 20, 20);
		g.setColor(Color.BLACK);
		g.drawRoundRect(width+43, 10, 22, height+19, 20, 20);
		
		//On/Off Button 
		g.setColor(Color.GRAY.darker());
		g.fillRoundRect(width+45, 50, 20, 20, 8, 8);
		g.setColor(Color.WHITE);
		g.drawString("I", width+53, 64);
		g.drawString("O", width+50, 64);
		
		//Return button
		g.setColor(Color.GRAY.darker());
		g.fillRoundRect(width+45, 100, 20, 20, 8, 8);
		g.setColor(Color.WHITE);
		g.drawString("H", width+50, 114);
		
		g.setColor(Color.BLACK);
		g.fillRoundRect(21, 13, 17, 5, 2, 2);
		
		if(on) {
			g.setColor(Color.GREEN);
		}
		else {
			g.setColor(Color.RED);
		}
		g.setFont(defaultFont);
		g.fillRoundRect(22, 14, 15, 3, 2, 2);

	}

	private void drawGrid(Graphics g) {
		g.setColor(Color.GRAY);
		//draw vert Grid Lines
		for (int i = 59; i < width - 1; i += 40) {
			g.drawLine(i, 20, i, height + 20);
		}
		//draw horz Grid Lines
		for (int i = 60; i < height - 1; i += 40) {
			g.drawLine(20, i, width + 20, i);
		}
	}

	private void drawSquares(Graphics g) {
		/*
		 g.setColor(Color.GREEN);
		 for(int i = 0; i < georgeClooney.length; i++){
		 for(int j = 0; j < georgeClooney[i].length; j++){
		 if(georgeClooney[i][j]){
		 georgeClooneysFace = theClooninator.getImage(i,j);
		 g.drawImage(georgeClooneysFace, i*40+20, j*40+20, null);
		 }
		 }
		 }
		 */

		if(on)
			georgeClooneysFace = theClooninator.getImage();
		else
			try{
				georgeClooneysFace = ImageIO.read(this.getClass().getResourceAsStream("image/off_screen.bmp"));
				
			}catch(Exception e){
				e.printStackTrace();
			}
		if(oldimage != null){
			g.drawImage(oldimage, 20, 20, null);
		}
		
		if(opacity <= 1.1f && opacity >= -0.1f){
			float[] scales = { 1f, 1f, 1f, opacity };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
			Graphics test2 = test.getGraphics();
			test2.drawImage(georgeClooneysFace, 0, 0, null);
			
			if(on){			
				if(theClooninator.getCurrentStateName().startsWith("Keypad")){
					test2.setFont(myFont);
					test2.setColor(Color.BLACK);
					for(int i=0; i<theClooninator.texts.size(); i++){
						if(theClooninator.texts.elementAt(i).name.equals("devname")){
							test2.drawString(theClooninator.texts.elementAt(i).value, 20, 50);
						}
					}
				}else{
				
					Vector<Transition> trans = theClooninator.map.get(theClooninator.getCurrentStateName());
					if(trans != null){
						int offset = StateMachine.page*5;
						
						test2.setFont(myFont);
						test2.setColor(Color.WHITE);
						for(int i=offset; i<Math.min(5+offset, trans.size()); i++){
							test2.drawString(trans.elementAt(i).text.substring(0,1).toUpperCase() + trans.elementAt(i).text.substring(1), 15, 40*(i%5+1)+67);		
						}
					}
				}
			}
			
			((Graphics2D)g).drawImage(test, rop, 20, 20);
			
			opacity += .1f;
		}else{
			opacity = -1.0f;
			oldimage = georgeClooneysFace.getSubimage(0, 0, georgeClooneysFace.getWidth(), georgeClooneysFace.getHeight());
			g.drawImage(oldimage, 20, 20, null);
			
			
			if(on){	
				if(theClooninator.getCurrentStateName().startsWith("Keypad")){
					g.setFont(myFont);
					g.setColor(Color.BLACK);
					for(int i=0; i<theClooninator.texts.size(); i++){
						if(theClooninator.texts.elementAt(i).name.equals("devname")){
							g.drawString(theClooninator.texts.elementAt(i).value, 40, 70);
						}
					}
				}else{		
					Vector<Transition> trans = theClooninator.map.get(theClooninator.getCurrentStateName());
					if(trans != null){
						int offset = StateMachine.page*5;
						
						g.setFont(myFont);
						g.setColor(Color.WHITE);
						for(int i=offset; i<Math.min(5+offset, trans.size()); i++){
							g.drawString(trans.elementAt(i).text.substring(0,1).toUpperCase() + trans.elementAt(i).text.substring(1), 35, 40*(i%5+1)+87);		
						}
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent event) {
	}

	public void mouseReleased(MouseEvent event) {
	}

	public void mousePressed(MouseEvent event) {
		//System.out.println("Enter mousePressed");
		int x = event.getX() - 20;
		int y = event.getY() - 20;
		if (x < width && x > 0 && y < height && y > 0
			&& georgeClooney[x / 40][y / 40] && on) {
			try{
				InputStream in = this.getClass().getResourceAsStream("Sounds/start.wav");
				AudioPlayer.player.stop(as);
				as = new AudioStream(in);
				AudioPlayer.player.start(as);            
				
			}catch(Exception e){
				e.printStackTrace();
			}
			theClooninator.transition(x / 40, y / 40);
			georgeClooney = theClooninator.getCurrentState();
			opacity = 0.0f;
			(new T(this)).start();
			//System.out.println("\tEnd First If");
		}
		else if(x > (265) && x < (305) && y > 30 && y < 50){
			if(on){
				try{
					InputStream in = this.getClass().getResourceAsStream("Sounds/vihs.wav");
					AudioPlayer.player.stop(as);
					as = new AudioStream(in);
					AudioPlayer.player.start(as);            
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				try{
					InputStream in = this.getClass().getResourceAsStream("Sounds/shiv.wav");
					AudioPlayer.player.stop(as);
					as = new AudioStream(in);         
					AudioPlayer.player.start(as);            
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			System.out.println(System.currentTimeMillis() + ", User pressed On/Off hard button");
			//System.out.println("\tIn Second If");
			on = !on;
			opacity = 0.0f;
			(new T(this)).start();
			//System.out.println("\tEnd Second First If");
		}
		else if(x > (265) && x < (305) && y > 80 && y < 100 && on){
			System.out.println(System.currentTimeMillis() + ", User pressed Home hard button");
			//System.out.println("\tIn Third If");
			if(theClooninator.getCurrentStateName().equals("Main")){
				return;
			}
			theClooninator.reset();
			georgeClooney = theClooninator.getCurrentState();
			opacity = 0.0f;
			(new T(this)).start();
			//System.out.println("\tEnd Third First If");
		}
		else {
			System.out.println(System.currentTimeMillis() + ", Error click");
			//System.out.println("                        _,-%/%|\n                    _,-'    \\//%\\\n                _,-'        \\%/|%\n              / / )    __,--  /%\\\n              \\__/_,-'%(%  ;  %)%\n                      %\\%,   %\\\n                        '--%'         ");
		}
		//System.out.println(x + "," + y);
		//System.out.println("Exit mousePressed");
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
	}

}

class T extends Thread {
	
	JPanel p;
	
	T(JPanel p){
		this.p = p;
	}
	
	public void run(){
		for(int i=0; i<15; i++){
			p.repaint();
			try{
				Thread.sleep(20);
			}catch(Exception e){
				
			}
		}
	}
}
