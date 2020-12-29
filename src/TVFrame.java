import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;


public class TVFrame extends JFrame {
	final static long serialVersionUID = 989329898938924l;
	private TV tv = new TV();
	private int chan1 = -1;
	private int chan2 = -1;
	private AudioStream as;
	
	// Sets up all the crap within the simple frame
	public TVFrame() {
		this.setSize(717, 707);
		this.setResizable(false);	// don't move.
		this.setTitle("TV");
		
		//this.setUndecorated(true);
		this.add(tv, BorderLayout.CENTER);
	}
	
	public void volumeUp() {
		tv.volumeUp();	
	}
	
	public void volumeDown() {
		tv.volumeDown();
	}
	
	public void channelUp() {
		tv.channelUp();	
	
	}
	public void channelDown() {
		tv.channelDown();	
	}
	
	public void channelInput(int ch) {
		if(chan1 == -1){
			chan1 = ch;
		}else{
			chan2 = ch;
		}
		tv.channelInput(ch);
	}
	
	public void turnOn() {
		tv.turnOn();	
	}
	
	public void turnOff() {
		tv.turnOff();	
	}

	public void recall(){
		tv.recall();
	}
	
	public void toggleOn(){
		if(tv.ison){
			tv.turnOff();
		}else{
			tv.turnOn();
		}
	}
	public void commit(){
		if(chan2 != -1){
			tv.setChannel(chan1*10+chan2, true);
		}else{
			tv.setChannel(chan1, true);
		}
		chan1 = -1;
		chan2 = -1;
	}
} // end of the frame class

class TV extends JPanel {
	final static long serialVersionUID = 9898928234322l;
	private int width;
	private int height;
	private int volume;
	private int channel;
	private int lastchan = 1;
	private Color backgroundColor;
	public boolean ison;
	final private int VOLUME_LIMIT = 440;
    private Image background;
    boolean firstRepaint = true;
    private String displaych = "01";
    private boolean moving = false;
    private AudioStream as;
    int numClips = 6;
    
	
	public TV (){
		width 	= 640;
		height 	= 480;
		setVolume(110);
		channel = 1;
		backgroundColor = Color.BLACK;
		this.setBackground(Color.WHITE);
		this.setPreferredSize(new Dimension(width, height));
		ison = false;
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Point pos = this.getLocationOnScreen( );
	    Point offset = new Point(-pos.x,-pos.y);
	    g.drawImage(background,offset.x,offset.y,null);
			
		//Draw Background
		drawBackground(g);
		
		// Draw Channel number
		try{
			drawChannelNumbers(g);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		// Draw Volume
		drawVolume(g);
		
		if(ison == false) {
			g.setColor(Color.black);
			try{
				BufferedImage b = ImageIO.read(this.getClass().getResourceAsStream("image/cloons/off_screen.bmp"));
				g.drawImage(b, 39, 52, null);
			}catch(Exception e){
				g.fillRect(39,52, this.getWidth(), this.getHeight());				
			}
		}
	}
	
	public void turnOn() {
		playClip();
		ison = true;
		this.repaint();
	}
	
	public void turnOff () {
		AudioPlayer.player.stop(as);
		ison = false;
		this.repaint();
	}

	public void setVolume(int v) {
		if (v <= 0)   v = 0;
		if (v >= VOLUME_LIMIT) v = VOLUME_LIMIT;

		volume = v;
		this.repaint();
		this.validate();
	}
	
	public void volumeUp(){
		if(!ison){
			System.out.println(System.currentTimeMillis() + ", TV volume not changed because TV is off");
			return;
		}
		setVolume(volume+10);
		this.validate();
	}
	
	public void volumeDown() {
		if(!ison) {
			System.out.println(System.currentTimeMillis() + ", TV volume not changed because TV is off");
			return;
		}
		setVolume(volume-10);
	}
	
	public void channelUp(){
		if(!ison){
			System.out.println(System.currentTimeMillis() + ", TV turned on from channel up button");
			turnOn();
			playClip();
		}else{
			if(channel == 99 && ison) {
				setChannel(1, !moving);
				moving = true;
				return;
			}
			setChannel(channel+1, false);
		}
	}
	
	public void recall(){
		if(ison){
			int temp = channel;
			channel = lastchan;
			lastchan = temp;
			displaych = Integer.toString(channel);
			if(displaych.length() == 1){
				displaych = "0" + displaych;
			}
			playClip();
			repaint();
			validate();
		}else{
			System.out.println(System.currentTimeMillis() + ", TV channel not changed because TV is off");
		}
	}

	private void playClip() {
		Random generator = new Random();
		int num = (int)(generator.nextInt(numClips)) + 1;
		try{
			InputStream in = this.getClass().getResourceAsStream("Sounds/george" + num + ".wav");
			AudioPlayer.player.stop(as);
			as = new AudioStream(in);
			AudioPlayer.player.start(as);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void channelDown() {
		if(!ison){
			System.out.println(System.currentTimeMillis() + ", TV turned on from channel down button");
			turnOn();
			playClip();
		}else{
			if(channel <= 1 && ison){
				setChannel(99, !moving);
				moving = true;
				return;
			}
			setChannel(channel-1, false);
		}
	}
	public void channelInput(int c){
		moving = false;
		if(c == -1 || !ison){
			return;
		}
		if(displaych.length() == 2){
			displaych = Integer.toString(c);
		}else{
			displaych += Integer.toString(c);
		}
		repaint();
		validate();
	}

	protected void setChannel(int c, boolean remember) {
		if(!ison){
			System.out.println(System.currentTimeMillis() + ", TV channel not changed because TV is off");
			return;
		}
		playClip();
		
		if(remember){
			lastchan = channel;
		}
		channel = c;
		displaych = Integer.toString(c);
		if(displaych.length() == 1){
			displaych = "0" + displaych;
		}
		this.repaint();
		this.validate();
	}
	
	/**
	 * Draws the background, not much to say about it.
	 * @param g	the Graphics for the component
	 */
	private void drawBackground(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(39,52, this.width, this.height);
		
	}
	
	private void drawVolume(Graphics g) {
		g.setColor(Color.WHITE);
		g.drawRect(100+39, 375+52+50, VOLUME_LIMIT, 8);
		
		// draw (and fill) the addition and subtraction symbols.
		g.fillRect(78+39, 379+52+50, 10, 2); // minus
		g.fillRect(552+39, 379+52+50, 10, 2); // horizontal plus
		g.fillRect(556+39, 375+52+50, 2, 10); // vertical plus
		
		//fill current Volume
		g.fillRect(100+39, 375+52+50, volume ,8);
	}

	private void drawChannelNumbers (Graphics g) throws IOException {
		int x = 39, y = 52;
		backgroundColor = Color.black;
		Image image = null;
		
		Toolkit tool = Toolkit.getDefaultToolkit();
		g.setColor(Color.WHITE); 	// set the color to white same color as volume
		
		image = ImageIO.read(this.getClass().getResourceAsStream("image/tv.gif"));
	    g.drawImage(image,0,0,this);

	    if(channel == 0){
	    	image = ImageIO.read(this.getClass().getResourceAsStream("image/cloons/off_screen.gif"));
	    	g.drawImage(image, x, y, this);
	    	backgroundColor = new Color(0,0,0);
	    }else{
			image = ImageIO.read(this.getClass().getResourceAsStream("image/cloons/gc" + ((channel%29)+1) + ".gif"));
		    g.drawImage(image, x, y, this);
			backgroundColor = new Color(0,0,0);
	    }
	    try{
	    	InputStream fontStream = this.getClass().getResourceAsStream("Fonts/mecha.ttf");
			Font myFont = java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, fontStream);
			myFont = myFont.deriveFont( (float) 35 );
			g.setFont(myFont);
	    }catch(Exception e){
	    	
	    }
	    g.drawString(displaych, 55, 80);
	}
}

