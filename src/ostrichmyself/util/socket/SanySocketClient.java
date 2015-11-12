package ostrichmyself.util.socket;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * �����Ǻ�ҵ���޹ص�Socket����
 * �ŵ�:
 * 1. ֱ��ʹ�ö�����Byte��Ϊͨ�Žӿ�, Ч�ʸ�
 * 2. ���еĽӿڲ���װ����ģʽ, ��������ֻ��Ҫ�޸���ײ�Ĵ���, ����ά��
 * @author tiant5
 *
 */
public class SanySocketClient {
	
	
	private Socket clientSocket;
	
	private InetSocketAddress tcpAddress;
	
	private int timeOut = 1000;  //��ʱ����, Ĭ��һ����
	
	private OutputStream out;
	
	private InputStream in;
	
	private Date sendTime;
	
	private final int receiveMaxSize = 1024 * 1024; // ����һ�ν������ݵĴ�С, ���������,Ĭ��Ϊ1M
	
	//�ͻ���Ψһ��ʶ����, ������HTTP��Session
	private long clientID = -1;
	
	public SanySocketClient(String ip, int port)
	{
		tcpAddress = new InetSocketAddress(ip,port);
	}
	
	/**
	 * ���ó�ʱʱ��, �ͻ��˱ر���ҩ, ����, ����˲�����, ����Ҫ�ȵ���ĵ�����, ��Ϊ�����������ģʽ
	 * @param tm
	 */
	public void setTimeOut(int tm)
	{
		timeOut = tm;
	}
	
	
	/**
	 * �Է���˿ڵ�����
	 * @return true�ɹ�, false��ʾʧ��
	 */
	public boolean connect()
	{
		try
		{
			if(clientSocket != null && clientSocket.isConnected()) 
			{
				out.close();
				in.close();
				clientSocket.close();
				clientSocket = null;
			}
			clientSocket = new Socket();
			clientSocket.connect(tcpAddress);
			
			if( clientSocket.isConnected() )
			{
//				clientSocket.setKeepAlive(true);
//				clientSocket.setTcpNoDelay(true);
				clientSocket.setSoTimeout(timeOut);
				out = clientSocket.getOutputStream();
				in = clientSocket.getInputStream();
				return true;
			}
		}
		catch(IOException ex)
		{
		}
		clientSocket = null;
		out = null;
		in = null;
		return false;
	}
	
	/**
	 * �����ض������String�������
	 * @param sendString
	 * @param charset ָ���ı���
	 * @throws Exception 
	 */
	public void send(String sendString, String charset) throws Exception
	{
		
		byte[] datas = sendString.getBytes(charset);
		send(datas);
	}
	
	
	/**
	 * ���Ͷ����������������, ������ܺ�����˵, �����Ҷ����ֽ���
	 * ����Ӧ������Ϊ���ֽ����Ľӿ�
	 * @param datas
	 * @throws Exception 
	 */
	public void send(byte[] datas) throws Exception
	{
		if( null == clientSocket || clientSocket.isClosed()) 
		{
			throw new Exception("socket closed!");
		}
		out.write(datas);
		out.flush();
		sendTime = new Date(System.currentTimeMillis()); //��ȡ��ǰʱ��);
	}
	
	/**
	 * ����Server�˵���Ϣ, �������ض������String ����
	 * ��ѵķ�ʽ, �ǽ�����һ��receive������װ, ����ά������Ľӿ�.
	 * @param charset
	 * @return
	 * @throws Exception 
	 */
	public String receive(String charset) throws Exception
	{
		byte[] receiveData = receive();
		if (receiveData == null)
		{
			return null;
		}
		
		String sData = new String(receiveData, charset);
		return sData.trim();
	}
	
	
	
	/**
	 * ����ָ��λ�õ���Ϣ, ����byte[]�ķ�ʽ�洢.
	 * ʵ����ʲôʱ�����һ����Ϣ, �Ǹ�ҵ�����.
	 * usage/com/ostrichmyself/socket/server/SimpleTask.java ������ѭ����������ȡ
	 * ���Ǹ��õķ�ʽ
	 * @return
	 * @throws Exception 
	 */
	public byte[] receive() throws Exception
	{
		if( null == clientSocket || clientSocket.isClosed()) 
		{
			throw new Exception("socket closed!");
		}
		byte[] bufIn = new byte[receiveMaxSize];
		Thread.sleep(1000);
		int bytesLen = in.available();
		int totalCount = 0;
		
		while( bytesLen > 0)
		{
			
			try
			{
				bytesLen = in.read(bufIn, totalCount, bytesLen);
				totalCount += bytesLen;
				bytesLen = in.available();
			}catch(SocketTimeoutException e)
			{
				//��ʱ�׳��쳣, ���������ж�read����
				break;
			}
			
		}
		
		byte[] stores = new byte[totalCount];
		System.arraycopy(bufIn, 0, stores, 0, totalCount);
		return stores;
	}

	/**
	 * �õ�����ʱ��
	 * @return
	 */
	public Date getSendTime() {
		return sendTime;
	}
	
	public void close()
	{
		try {
			clientSocket.close();
			in.close();
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long getClientID() {
		return clientID;
	}

	public void setClientID(long clientID) {
		this.clientID = clientID;
	}
	
	
	/**
	 * �Ƚ�����Ľ��շ�ʽ, ���簴��״̬���Ľ��շ�ʽ,  ��Ҫ���ýӿڱ�¶������, �����ⲿ�ϴ�����ɶ�
	 * ԭ���Ǵ󲿷����ݽ����Ƿ�����, �����н�����ʾ
	 * @return
	 */
	public InputStream getInputStream()
	{
		return in;
	}
	
	
	public OutputStream getOutputStream()
	{
		return out;
	}
	
	
	
	


}
