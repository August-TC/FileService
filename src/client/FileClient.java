package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient
{
    private final int TCP_PORT = 2020;
    private final int UDP_PORT = 2021;

    private static String HOST;

    private Socket socket;
    private DatagramSocket datagramSocket;

    public FileClient() throws SocketException
    {
        this.socket = new Socket();
        this.datagramSocket = new DatagramSocket();
    }

    public static void main(String[] args)
    {
        try
        {
            if (args.length ==1 ){
                HOST = args[0];
            }
            FileClient client = new FileClient();
            client.connectHost(HOST);
            client.send();
        }catch (IllegalArgumentException e)
        {
            System.err.println(e.getMessage());
            System.err.println("Usage : java client.FileClient <HOST>");
        } catch (SocketException e)
        {
            System.err.println(e.getMessage());
            System.err.println("=== Connection fail. ===");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void send()
    {
        try
        {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            System.out.println(br.readLine());
            PrintWriter pw = new PrintWriter(bw, true);
            Scanner in = new Scanner(System.in);
            String cmd;
            while ((cmd = in.nextLine()) != null) {
                pw.println(cmd);
                if (cmd.equals("bye")) {
                    break;
                }
                resultOutput(br);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private boolean receiveDataPacket() throws IOException
    {
        SocketAddress socketAddress = new InetSocketAddress(HOST,UDP_PORT);
        String ok = "OK";
        DatagramPacket send = new DatagramPacket(ok.getBytes(),ok.length(),socketAddress);
        datagramSocket.send(send);
        DatagramPacket get = new DatagramPacket(new byte[8192],8192);
        datagramSocket.receive(get);
        String receive = new String(get.getData(),0,get.getLength());
        if (receive != null)
        {
            FileWriter fw = new FileWriter("get.txt");
            fw.write(receive);
            fw.flush();
            fw.close();
            System.out.println(receive);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void resultOutput(BufferedReader br) throws IOException
    {
        String result = null;
        while (!(result = br.readLine()).equals("done!"))
        {
            if (result.equals("send"))
            {
                if (receiveDataPacket())
                {
                    System.out.println("Succeed in getting the file.");
                }
                else
                {
                    System.out.println("Fail to get the file.");
                }
            }
            else
            {
                System.out.println(result);
            }
        }
        System.out.println(br.readLine());
    }

    private void connectHost(String host) throws IOException
    {
        socket.connect(new InetSocketAddress(host,TCP_PORT));
        datagramSocket.connect(new InetSocketAddress(host,UDP_PORT));
    }
}
