package server;

import TCPHandler.FileHandler;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer
{
    private ServerSocket serverSocket;
    private DatagramSocket datagramSocket;
    private final int TCP_PORT = 2020;
    private final int UDP_PORT = 2021;

    private static String root;

    public FileServer() throws IOException
    {
        this.serverSocket = new ServerSocket(TCP_PORT,3);
        this.datagramSocket = new DatagramSocket(UDP_PORT);

        System.out.println("=== Server start ===");
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        FileServer.root = root;
    }

    /**
     * Service
     */
    public void service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                Thread work = new Thread(new FileHandler(socket, datagramSocket,root));
                // 为客户连接创建工作线程
                work.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args)
    {
        try
        {
            FileServer fileServer = new FileServer();
            if (checkRootPath(fileServer,args))
            {
                fileServer.service();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean checkRootPath(FileServer fileServer, String[] args)
    {
        if(args.length == 1)
        {
            String root_input = args[0];
            if (root_input.endsWith("/"))
            {
                root_input = root_input.substring(0,root_input.length()-1);
            }
            File file = new File(root_input);
            if (file.exists())
            {
                fileServer.setRoot(root_input);
                return true;
            }
            else
            {
                System.err.println("The directory is NOT exist!");
                return false;
            }
        }
        else if (args.length == 0)
        {
            System.err.println("Usage: java FileServer <dir>");
            return false;
        }
        else
        {
            System.err.println("== Only ONE path can be set as root path. ==");
            System.err.println("Usage: java FileServer <dir>");
            return false;
        }
    }
}
