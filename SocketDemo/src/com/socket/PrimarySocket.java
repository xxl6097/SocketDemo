package com.socket;

import java.util.List;

import ostrichmyself.util.socket.SanySocketClient;

import com.BuildRequest;
import com.wisegeek.finance.protobuf.RealTimeQuoteData.RealTimeQuoteDataResponse.RealData;

public class PrimarySocket {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SanySocketClient client = new SanySocketClient("61.145.163.252", 6688);// "115.29.198.240",
		// 6700
		client.connect();

		try {
			client.send(BuildRequest.buildRequestByte());
			while (true) {
				byte[] recv = client.receive();
				if (recv == null || recv.length == 0)
					continue;
				List<RealData> list = BuildRequest.parse(recv);
//				System.out.println(Arrays.toString(recv));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
