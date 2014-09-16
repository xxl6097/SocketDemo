package com.boyaa.push.lib.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 
 * @author Administrator
 * 
 */
public class NioClient {

	private final int STATE_OPEN = 1;// socket打开
	private final int STATE_CLOSE = 1 << 1;// socket关闭
	private final int STATE_CONNECT_START = 1 << 2;// 开始连接server
	private final int STATE_CONNECT_SUCCESS = 1 << 3;// 连接成功
	private final int STATE_CONNECT_FAILED = 1 << 4;// 连接失败
	private final int STATE_CONNECT_WAIT = 1 << 5;// 等待连接

	private String IP = "192.168.1.101";
	private int PORT = 60000;
	private int state = STATE_CONNECT_START;

	Selector selector;
	ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	SocketChannel socketChannel;

	private Thread conn = null;
	private Thread rec = null;

	private ISocketResponse respListener;
	private ArrayList<Packet> requestQueen = new ArrayList<Packet>();
	private final Object lock = new Object();
	private final String TAG = "NioClient";

	public int send(Packet in) {
		synchronized (lock) {
			requestQueen.add(in);
		}
		if (this.selector != null) {
			this.selector.wakeup();
		}
		return in.getId();
	}

	public void cancel(int reqId) {
		Iterator<Packet> mIterator = requestQueen.iterator();
		while (mIterator.hasNext()) {
			Packet packet = mIterator.next();
			if (packet.getId() == reqId) {
				mIterator.remove();
			}
		}
	}

	public NioClient(ISocketResponse respListener) {
		this.respListener = respListener;
	}

	public boolean isSocketConnected() {
		return ((state == STATE_CONNECT_SUCCESS) && (null != rec && rec
				.isAlive()));
	}

	public void open() {
		reconn();
	}

	public void open(String host, int port) {
		this.IP = host;
		this.PORT = port;
		reconn();
	}

	private long lastConnTime = 0;

	public synchronized void reconn() {
		if (System.currentTimeMillis() - lastConnTime < 2000) {
			return;
		}
		lastConnTime = System.currentTimeMillis();

		close();
		state = STATE_OPEN;
		conn = new Thread(new Conn());
		conn.start();
	}

	public synchronized void close() {
		try {
			if (state != STATE_CLOSE) {
				try {
					if (null != conn && conn.isAlive()) {
						conn.interrupt();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					conn = null;
				}

				if (null != selector && selector.isOpen()) {
					selector.close();
				}

				try {
					if (null != rec && rec.isAlive()) {
						rec.interrupt();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					rec = null;
				}

				state = STATE_CLOSE;
			}
			requestQueen.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class Conn implements Runnable {
		public void run() {
			// System.out.println("Conn :Start");
			try {
				state = STATE_CONNECT_START;

				selector = SelectorProvider.provider().openSelector();
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);

				InetSocketAddress address = new InetSocketAddress(IP, PORT);
				socketChannel.connect(address);
				socketChannel.register(selector, SelectionKey.OP_CONNECT);

				while (state != STATE_CLOSE) {
					// 从选择器已经就绪通道数量，一般情况返回1
					int readyChannels = selector.select();
					if (readyChannels == 0)
						continue;
					Iterator<SelectionKey> selectedKeys = selector
							.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();

						if (!key.isValid()) {
							continue;
						}

						if (key.isConnectable()) {
							finishConnection(key);
						} else if (key.isReadable()) {
							read(key);
						} else if (key.isWritable()) {
							write(key);
						}
					}
					synchronized (lock) {
						if (requestQueen.size() > 0) {
							SelectionKey key = socketChannel.keyFor(selector);
							key.interestOps(SelectionKey.OP_WRITE);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != socketChannel) {
					socketChannel.keyFor(selector).cancel();
					try {
						socketChannel.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			// System.out.println("Conn :End");
		}

		private boolean finishConnection(SelectionKey key) throws IOException {
			boolean result = false;
			SocketChannel socketChannel = (SocketChannel) key.channel();
			// 判断此通道上是否正在进行连接操作。
			// 完成套接字通道的连接过程。
			if (socketChannel.isConnectionPending()) {
				// 完成连接的建立（TCP三次握手）
				result = socketChannel.finishConnect();// 没有网络的时候也返回true
				if (result) {
					key.interestOps(SelectionKey.OP_READ);
					state = STATE_CONNECT_SUCCESS;
				}
				System.out.println("finishConnection :" + result);
			}
			return result;
		}

		private void read(SelectionKey key) throws IOException {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			readBuffer.clear();
			int numRead;
			numRead = socketChannel.read(readBuffer);
			if (numRead == -1) {
				key.channel().close();
				key.cancel();
				return;
			}
			// respListener.onSocketResponse(new String(readBuffer.array(),
			// 0,numRead));
			byte[] stores = new byte[numRead];
			System.arraycopy(readBuffer.array(), 0, stores, 0, numRead);
			respListener.onSocketResponse(stores);
		}

		private void write(SelectionKey key) throws IOException {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			synchronized (lock) {
				Packet item;
				Iterator<Packet> iter = requestQueen.iterator();
				while (iter.hasNext()) {
					item = iter.next();
					ByteBuffer buf = ByteBuffer.wrap(item.getPacket());
					socketChannel.write(buf);
					iter.remove();
				}
				item = null;
			}
			key.interestOps(SelectionKey.OP_READ);
		}
	}
}
