
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;

public class StateMachine {

	private Vector<State> states = new Vector<State>();
	private Vector<TransitionGroup> groups = new Vector<TransitionGroup>();
	public Vector<Text> texts = new Vector<Text>();
	public HashMap<String, Vector<Transition>> map = new HashMap<String, Vector<Transition>>();
	public static int page = 0;
	String devStr = "";
	boolean devTxt = false;
	public TVFrame tv;
	private InputStream script;

	int currentState = 0;
	boolean group = false;

	public void load(){
		script = this.getClass().getResourceAsStream("stateScript.ym");
		load(script);
	}
	
	public void load(InputStream script) {
		try {
			State s = null;
			TransitionGroup tg = null;

			BufferedReader br = new BufferedReader(new InputStreamReader(script));
			String line = br.readLine();

			while (line != null) {
				line = line.trim();
				if (line.startsWith("$")) {
					line = br.readLine();
					continue;
				}
				else if(line.startsWith(".TEXT:")){
					texts.add(new Text(line.substring(6)));
				}
				else if(line.startsWith("#INCLUDE:")){
					InputStream stream = this.getClass().getResourceAsStream(line.substring(9));
					load(stream);
				}
				else if (line.startsWith("@TGROUP:")) {
					group = true;
					tg = new TransitionGroup(line.substring(8));
					groups.add(tg);
				}
				else if (line.startsWith("STATE:")) {
					group = false;
					s = new State(line.substring(6), texts, map, states, tv);
					s.setImage(br.readLine().trim());
					states.add(s);
				} else if (line.startsWith("^")) {
					for (int i = 0; i < groups.size(); i++) {
						if (groups.elementAt(i).name.equals(line.substring(1))) {
							groups.elementAt(i).put(s);
						}
					}
				} else if (!line.equals("")) {
					if (!group) {
						s.addTransition(line);
					} else {
						tg.add(line);
					}
				}
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean[][] getDefaultGeorgecloony() {
		return states.elementAt(0).getGeorgeClooney();
	}

	public boolean[][] getCurrentState() {
		return states.elementAt(currentState).getGeorgeClooney();
	}

	public void transition(int x, int y) {
		State current = states.elementAt(currentState);
		//String printMe = "From: " + current.name + "; ";
		int whatever = current.transition(x, y, states);
		if (whatever != -1) {
			currentState = whatever;
			//System.out.println(printMe + "Going to: "
			//		+ states.elementAt(currentState).name);
		}
	}

	//	public BufferedImage getImage(int x, int y){
	//		return states.elementAt(currentState).getImage(x,y);
	//	}

	public BufferedImage getImage() {
		return states.elementAt(currentState).getImage();
	}
	
	public void reset(){
		currentState = 0;
	}
	
	public String getCurrentStateName(){
		return states.elementAt(currentState).name;
	}
	
}

class State {
	Vector<Transition> transitions = new Vector<Transition>();
	String name;
	String image;
	Vector<Text> texts;
	Vector<State> states;
	HashMap<String, Vector<Transition>> map;
	TVFrame tv;
	ChannelGoer goer;
	
	State(String name, Vector<Text> texts, HashMap<String, Vector<Transition>> map, Vector<State> states, TVFrame tv) {
		this.tv = tv;
		this.name = name;
		this.texts = texts;
		this.map = map;
		this.states = states;
	}

	public void setImage(String img) {
		image = img;
	}
	
	public void addTransition(String s) {
		StringTokenizer st = new StringTokenizer(s, ":");
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		String name = st.nextToken();
		//String image = st.nextToken();
		String signal;
		if (st.hasMoreTokens())
			signal = st.nextToken();
		else
			signal = null;
		//transitions.add(new Transition(x,y,name,image,signal));
		transitions.add(new Transition(x, y, name, signal));
	}

	public boolean[][] getGeorgeClooney() {
		boolean[][] clooney = new boolean[6][8];
		for (int i = 0; i < transitions.size(); i++) {
			clooney[transitions.elementAt(i).x][transitions.elementAt(i).y] = true;
		}
		return clooney;
	}

	public int transition(int x, int y, Vector<State> states) {
		Transition t = null;
		for (int i = transitions.size() - 1; i >= 0; i--) {
			if (transitions.elementAt(i).x == x
				&& transitions.elementAt(i).y == y) {
				t = transitions.elementAt(i);
				break;
			}
		}
		
		if (!(t.signal == null)){
			if(t.signal.startsWith("ADDTEXT(")){
				String target = t.signal.substring(8, t.signal.indexOf(","));
				String text = t.signal.substring(t.signal.indexOf(",")+1, t.signal.length()-1);
				System.out.println(System.currentTimeMillis() + ", User pressed the \"" + text + "\" button");
				for(int i=0; i<texts.size(); i++){
					if(texts.elementAt(i).name.equals(target)){
						texts.elementAt(i).value += text;
					}
				}
			}else if(t.signal.startsWith("BACKTEXT(")){
				String target = t.signal.substring(9, t.signal.indexOf(")"));
				for(int i=0; i<texts.size(); i++){
					if(texts.elementAt(i).name.equals(target) && texts.elementAt(i).name.length()>0){
						texts.elementAt(i).value = texts.elementAt(i).value.substring(0, texts.elementAt(i).value.length()-1);
					}
				}
				System.out.println(System.currentTimeMillis() + ", User pressed backspace button");
			}else if(t.signal.startsWith("CLEARTEXT(")){
				String target = t.signal.substring(10, t.signal.indexOf(")"));
				for(int i=0; i<texts.size(); i++){
					if(texts.elementAt(i).name.equals(target)){
						texts.elementAt(i).value = "";
					}
				}
			}else if(t.signal.startsWith("CREATEDEV(")){
				System.out.println(System.currentTimeMillis() + ", User creating device");
				String target = t.signal.substring(10, t.signal.indexOf(","));
				String type = t.signal.substring(t.signal.indexOf(",")+1, t.signal.length()-1);
				String devname = "";
				String devtype ="";
				for(int i=0; i<texts.size(); i++){
					if(texts.elementAt(i).name.equals(target)){
						devname = texts.elementAt(i).value;
						texts.elementAt(i).value = "";
						break;
					}
				}
				for(int i=0; i<texts.size(); i++){
					if(texts.elementAt(i).name.equals(type)){
						devtype = texts.elementAt(i).value;
						texts.elementAt(i).value = "";
						break;
					}
				}
				if(!devname.trim().equals("") && !devtype.trim().equals("")){
						System.out.println(System.currentTimeMillis() + ", User created device \"" + devname.trim() + "\" type " + devtype);
						Vector<Transition> v1 = map.get("Main");
						Vector<Transition> v2 = map.get("Settings");
						if(v1 == null){
							v1 = new Vector<Transition>();
							map.put("Main", v1);
						}
						if(v2 == null){
							v2 = new Vector<Transition>();
							map.put("Settings", v2);
						}
						v1.add(new Transition(devtype, null, devname));
						v2.add(new Transition(devtype, null, devname));
				}else{
					System.out.println(System.currentTimeMillis() + ", User attempt to create device \"" + devname.trim() + "\" type " + devtype + " failed");
				}
			}else if(t.signal.equals("PAGERIGHT()")){
				StateMachine.page++;
				Vector<Transition> v = map.get(name);
				if(v != null){
					int size = map.get(name).size();
					StateMachine.page %= Math.ceil(size/5.0);
				}else{
					StateMachine.page = 0;
				}
				
			}else if(t.signal.equals("PAGELEFT()")){
				StateMachine.page--;
				Vector<Transition> v = map.get(name);
				if(v != null){
					int size = map.get(name).size();
					if(StateMachine.page < 0){
						StateMachine.page = (size-1)/5;
					}else{
						StateMachine.page %= Math.ceil(size/5.0);
					}
				}else{
					StateMachine.page = 0;
				}
			}else if(t.signal.startsWith("OPENDEVICE(")){
				int dev = Integer.parseInt(t.signal.substring(11, t.signal.indexOf(")")));
				Vector<Transition> v = map.get(name);
				if(v == null){
					return -1;
				}
				if(v.size() > StateMachine.page*5+dev){
					for(int i=0; i<states.size(); i++){
						if(states.elementAt(i).name.equals(v.elementAt(StateMachine.page*5+dev).target)){
							System.out.println(System.currentTimeMillis() + ", User opened device " + states.elementAt(i).name);
							return i;
						}
					}
				}else{
					return -1;
				}
			}else if(t.signal.equals("TOGGLETV")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV power button");
				tv.toggleOn();
			}else if(t.signal.equals("VOLUP")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV volume up");
				tv.volumeUp();
			}else if(t.signal.equals("VOLDOWN")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV volume down");
				tv.volumeDown();
			}else if(t.signal.equals("CHANUP")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV channel up");
				tv.channelUp();
			}else if(t.signal.equals("CHANDOWN")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV channel down");
				tv.channelDown();
			}else if(t.signal.equals("RECALL()")){
				System.out.println(System.currentTimeMillis() + ", User pressed TV recall button");
				tv.recall();
			}else if(t.signal.startsWith("NUM(")){
				int num = Integer.parseInt(t.signal.substring(4, t.signal.indexOf(")")));
				System.out.println(System.currentTimeMillis() + ", User pressed the TV " + num + " button");
				if(goer == null || !goer.isAlive()){
					goer = new ChannelGoer(tv);
					goer.a = num;
					goer.start();
				}else{
					goer.setB(num);
				}
			}
		}else if (t.target.equals(".")){
			System.out.println(System.currentTimeMillis() + ", User pressed a nontransitional button " );
			return -1;
		}
		
		for (int i = 0; i < states.size(); i++) {
			if (states.elementAt(i).name.equals(t.target)) {
				StateMachine.page = 0;
				System.out.println(System.currentTimeMillis() + ", Transitioning to state " + states.elementAt(i).name);
				return i;
			}
		}
		return -1;
	}

	/*	
	 public BufferedImage  getImage(int x, int y){
	 Transition t = null;
	 for(int i=0; i<transitions.size(); i++){
	 if(transitions.elementAt(i).x == x && transitions.elementAt(i).y == y){
	 t = transitions.elementAt(i);
	 break;
	 }
	 }
	 File image = new File(t.imageFile);
	 try{return ImageIO.read(image);}
	 catch(Exception e){return null;}

	 }
	 */

	public BufferedImage getImage() {
		File img = new File(image);
		try {
			return ImageIO.read(this.getClass().getResourceAsStream(image));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("image unreadable: " + image);
			return null;
		}
	}
}

class Transition {
	int x, y;
	String target;
	String signal;
	String text;

	Transition(int x, int y, String target, String signal) {
		this.x = x;
		this.y = y;
		this.target = target;
		this.signal = signal;
	}
	
	Transition(String target, String signal, String text){
		this.target = target;
		this.signal = signal;
		this.text = text;
	}
	
}

class TransitionGroup {
	String name;
	Vector<String> Ts = new Vector<String>();

	TransitionGroup(String name) {
		this.name = name;
	}

	public void add(String t) {
		Ts.add(t);
	}

	public void put(State s) {
		for (int i = 0; i < Ts.size(); i++) {
			s.addTransition(Ts.elementAt(i));
		}
	}
}

class Text {
	public String name;
	public String value = "";
	
	Text(String name){
		this.name = name;
	}
}

class ChannelGoer extends Thread {

	int a = -1;
	int b = -1;
	TVFrame tv;
	
	ChannelGoer(TVFrame tv){
		this.tv = tv;
	}
	
	public void run(){
		tv.channelInput(a);
		try{
			Thread.sleep(2000);
		}catch(Exception e){
			
		}
		tv.commit();
		System.out.println(System.currentTimeMillis() + ", TV channel changed to " + ((b==-1)?a:a*10+b));
	}

	public void setB(int x){
		b = x;
		tv.channelInput(b);
		this.interrupt();
	}
	
}