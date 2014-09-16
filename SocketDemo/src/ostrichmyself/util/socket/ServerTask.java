package ostrichmyself.util.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerTask extends SingleTask {
    private BufferedReader in;  
    private PrintWriter out;  
	@Override
	public void run() {
		// TODO Auto-generated method stub
		 try {
			in = new BufferedReader(new InputStreamReader(serverClientSocket.getInputStream()));  
			 out = new PrintWriter(serverClientSocket.getOutputStream(), true);  
			 String line = in.readLine();  
			   
			 System.out.println("you input is : " + line);  
			   
			 //out.println("you input is :" + line);  
			   
			 out.close();  
			 in.close();  
			 serverClientSocket.close();  
			   
			 if(line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))  
			     return;  
			System.out.println("hello");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
