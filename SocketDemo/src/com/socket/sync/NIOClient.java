package com.socket.sync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.BuildRequest;

public class NIOClient {
	/*��ʶ����*/  
	private static int flag = 0;  
	/*��������С*/  
	private static int BLOCK = 4096;  
	/*�������ݻ�����*/  
	private static ByteBuffer sendBuffer = ByteBuffer.allocate(BLOCK);  
	/*�������ݻ�����*/  
	private static ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);  
	/*�������˵�ַ*/  
	private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("61.145.163.252",6688);  

	private static ArrayList<byte[]> requestQueen = new ArrayList<byte[]>();

	public static void main(String[] args) throws IOException {  
		// ��socketͨ��  
		SocketChannel clientChannel = SocketChannel.open();  
		// ����Ϊ��������ʽ  
		clientChannel.configureBlocking(false);  
		// ��ѡ����  
		Selector selector = Selector.open();  
		// ע�����ӷ����socket����  
		clientChannel.register(selector, SelectionKey.OP_CONNECT);  
		// ����  
		clientChannel.connect(SERVER_ADDRESS);  

		SocketChannel socketChannel;
		Set<SelectionKey> selectionKeys;    
		String receiveText;  
		String sendText;  
		int count=0;  
		send(BuildRequest.buildRequestByte(), selector);
		while (true) {  
			//ѡ��һ���������Ӧ��ͨ����Ϊ I/O ����׼��������  
			//�������ע��� channel ����������ע��� IO �������Խ���ʱ���ú������أ�������Ӧ�� SelectionKey ���� selected-key set 
			selector.select();  
			//���ش�ѡ��������ѡ�������  
			selectionKeys = selector.selectedKeys();  
			//System.out.println(selectionKeys.size());  
			for(SelectionKey selectionKey:selectionKeys){ 
				//�ж��Ƿ�Ϊ�������ӵ��¼�
				if (selectionKey.isConnectable()) {  
					System.out.println("client connect");  
					socketChannel = (SocketChannel) selectionKey.channel();  //
					// �жϴ�ͨ�����Ƿ����ڽ������Ӳ�����  
					// ����׽���ͨ�������ӹ��̡�  
					if (socketChannel.isConnectionPending()) { 
						//������ӵĽ�����TCP�������֣�
						socketChannel.finishConnect();  
						System.out.println("�������!");  
						sendBuffer.clear();  
						sendBuffer.put("Hello,Server".getBytes());  
						sendBuffer.flip();  
						ByteBuffer s = ByteBuffer.wrap(BuildRequest.buildRequestByte());
						//                        socketChannel.write(s);  
					}  
					socketChannel.register(selector, SelectionKey.OP_WRITE);  
				} else if (selectionKey.isReadable()) {  
					socketChannel = (SocketChannel) selectionKey.channel();  
					//������������Ա��´ζ�ȡ  
					receiveBuffer.clear();  
					ByteBuffer receiveBuffer = ByteBuffer.allocate(BLOCK);  
					//��ȡ�����������������ݵ���������  
					count=socketChannel.read(receiveBuffer);  
					if(count>0){  
						byte[] stores = new byte[count];
						System.arraycopy(receiveBuffer.array(), 0, stores, 0, count);
						BuildRequest.parse(stores);

						//                        receiveText = new String( receiveBuffer.array(),0,count);  
						//                        System.out.println("�ͻ��˽��ܷ�����������--:"+receiveText);  
						socketChannel.register(selector, SelectionKey.OP_WRITE);  
					}  

				} else if (selectionKey.isWritable()) {  
					sendBuffer.clear();  
					write(selectionKey);
					//                    socketChannel = (SocketChannel) selectionKey.channel();  
					//                    sendText = "message from client--" + (flag++);  
					//                    sendBuffer.put(sendText.getBytes());  
					//                     //������������־��λ,��Ϊ������put�����ݱ�־���ı�Ҫ����ж�ȡ���ݷ��������,��Ҫ��λ  
					//                    sendBuffer.flip();  
					//                    ByteBuffer s = ByteBuffer.wrap(BuildRequest.buildRequestByte());
					//                    socketChannel.write(s);  
					//                    System.out.println("�ͻ�����������˷�������--��"+sendText);  
					//                    socketChannel.register(selector, SelectionKey.OP_READ);  
				}  
			}  
			selectionKeys.clear();  
		}  
	} 

	public static void send(byte[] in, Selector selector) {
		synchronized (lock) {
			requestQueen.add(in);
		}
		if (selector != null) {
			selector.wakeup();
		}
	}
	private final static Object lock = new Object();
	private static void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		synchronized (lock) {
			byte[] item;
			Iterator<byte[]> iter = requestQueen.iterator();
			while (iter.hasNext()) {
				item = iter.next();
				ByteBuffer buf = ByteBuffer.wrap(item);
				socketChannel.write(buf);
				iter.remove();
			}
			item = null;
		}
		key.interestOps(SelectionKey.OP_READ);
	}
}