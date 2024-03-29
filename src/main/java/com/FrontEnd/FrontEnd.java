package com.FrontEnd;


import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.Endpoint;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.Request.RequestData;

public class FrontEnd {
    public static final String sequencerIP = "192.168.247.53";
    public static final int sequencerPort = 2233;

    public static void main(String[] args) {
        FrontendImpl feImpl = new FrontendImpl();
        Endpoint endpoint = Endpoint.publish("http://localhost:8080/frontend?wsdl", feImpl);
        System.out.println("Frontend server is published: " + endpoint.isPublished());

        //TODO: Assign port numbers to all devices
        Runnable thread1 = new FrontendThread(44553, feImpl); //this port is frontend's



        Executor executor = Executors.newFixedThreadPool(1);
        executor.execute(thread1);
    }


    public static void receiveResponsesFromRms() {
        // DatagramSocket serverSocket = new DatagramSocket(2234);
        // byte[] data = new byte[1024];
        // DatagramPacket packet = new DatagramPacket(data, data.length);
        // serverSocket.receive(packet);

        // System.out.println("VIA UDP -- Packets received at Frontend socket!!");

        // String arr = new String(packet.getData()).trim();
        // System.out.print("VIA UDP - Message received from port -- " + packet.getPort()
        //         + " to Frontend server: " + arr);

        // String result = MethodMapper(arr, feImpl, userLogger);

        // byte[] b = result.getBytes();

        // InetAddress ip = InetAddress.getLocalHost();
        // DatagramPacket packetResult = new DatagramPacket(b, b.length, ip, packet.getPort());
        // serverSocket.send(packetResult);
        // System.out.print("VIA UDP - Message sent from Frontend server to : " + packet.getPort());

        // serverSocket.close();
    }

    public static void sendErrorMessageToRM() {

    }
}