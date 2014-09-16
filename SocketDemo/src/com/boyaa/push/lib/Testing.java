package com.boyaa.push.lib;

import com.BuildRequest;
import com.boyaa.push.lib.service.ISocketResponse;
import com.boyaa.push.lib.service.NioClient;
import com.boyaa.push.lib.service.Packet;

public class Testing {
	private static NioClient user = null;
	public static void main(String[] args) {
		user = new NioClient(socketListener);
		user.open("61.145.163.252",6688);
		Packet packet = new Packet();
		packet.pack(BuildRequest.buildRequestByte());
		user.send(packet);
	}
	private static ISocketResponse socketListener = new ISocketResponse() {
		@Override
		public void onSocketResponse(byte[] recv) {
			if (recv == null || recv.length == 0)
				return;
//			System.out.println(Arrays.toString(recv));
			BuildRequest.parse(recv);
			
		}
	};
}
