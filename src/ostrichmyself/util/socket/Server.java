package ostrichmyself.util.socket;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SanySocketServer ser = new SanySocketServer(59687, 20, ServerTask.class);
			ser.process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
