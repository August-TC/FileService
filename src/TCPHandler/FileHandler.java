package TCPHandler;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class FileHandler implements Runnable
{
    private Socket socket;
    private DatagramSocket datagramSocket;
    private String root;
    private String crtPath;

    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;

    public FileHandler(Socket socket, DatagramSocket datagramSocket, String root)
    {
        this.socket = socket;
        this.datagramSocket = datagramSocket;
        this.root = root;
        crtPath = "/";
    }

    public void initStream() throws IOException
    {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    @Override
    public void run()
    {

        try{

            System.out.println("=== New Connection ===");
            System.out.println("[ "+socket.getInetAddress()+" : "+socket.getPort()+" ]");

            // Initiate I/O stream
            initStream();

            String cmd;
            System.err.println(root+crtPath);

            InetAddress ia = InetAddress.getLocalHost();
            if (crtPath.equals("/"))
            {
                pw.println(ia.getHostAddress()+" >>> Successfully Connect");
            }
            while (null != (cmd = br.readLine())) {
                System.out.println("[ "+socket.getInetAddress()+" : "+socket.getPort()+" ] >>> "+cmd);
                String result = "Fail";
                if (cmd.equals("bye")) {
                    System.out.println("=== Disconnect ===");
                    break;
                }
                else
                {
                    if (doCmd(cmd))
                    {
                        result = "Done";
                    }
                }

                pw.println("done!");
                if (crtPath.equals("/"))
                {
                    pw.println(ia.getHostAddress()+" >>> "+result);
                }
                else
                {
                    pw.println(crtPath+" >>> "+result);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }finally
        {
            try
            {
                if (null != socket) {
                    socket.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean doCmd(String cmd) throws IOException
    {
        if (cmd.equals("ls"))
        {
            File files = new File(root+crtPath);
            if (files == null)
            {
                pw.println("NO more files or directory");
                return false;
            }
            for (File file:
                    files.listFiles())
            {
                if (file.isDirectory())
                {
                    pw.println("<dir>\t"+file.getName()+"\t"+file.length());
                }
                else if(file.isFile())
                {
                    pw.println("<file>\t"+file.getName()+"\t"+file.length());
                }
                else
                {
                    pw.println("<other>\t"+file.getName()+"\t"+file.length());
                }
            }
            return true;
        }
        else if (cmd.startsWith("cd"))
        {
            String[] attrs = cmd.split(" ");
            if (attrs.length > 0 && attrs[0].equals("cd"))
            {
                switch (attrs.length)
                {
                    case 1:
                        pw.println("Usage : cd <directory>");
                        break;
                    case 2:
                        if (attrs[1].equals(".."))
                        {
                            if (!crtPath.equals("/"))
                            {
                                crtPath = parentPath(root+crtPath);
                                return true;
                            }
                            else
                            {
                                pw.println("Reach the root directory.");
                            }
                        }
                        else
                        {
                            if (!attrs[1].endsWith("/"))
                            {
                                attrs[1] = attrs[1] + "/";
                            }
                            if (attrs[1].startsWith("/"))
                            {
                                if (checkPath(root + attrs[1]))
                                {
                                    crtPath = attrs[1];
                                    return true;
                                }
                                else
                                {
                                    pw.println("The directory is NOT exist!");
                                }
                            }
                            else
                            {
                                if (checkPath(root + crtPath + attrs[1]))
                                {
                                    crtPath = crtPath +attrs[1];
                                    return true;
                                }
                                else
                                {
                                    pw.println("The directory is NOT exist!");
                                }
                            }
                        }
                        break;
                    default:
                        pw.println("No such command : " + cmd);
                        break;
                }
            }
            else {
                pw.println("No such command : " + cmd);
            }
        }
        else if (cmd.startsWith("get"))
        {
            String[] attrs = cmd.split(" ");
            if (attrs.length > 0 && attrs[0].equals("get"))
            {
                switch (attrs.length)
                {
                    case 1:
                        pw.println("Usage : get <file>");
                        break;
                    case 2:
                        //return true;
                        if (checkPath(root+crtPath+attrs[1]))
                        {
                            pw.println("send");
                        }
                        else
                        {
                            pw.println("Not exist");
                            return false;
                        }

                        DatagramPacket dp = new DatagramPacket(new byte[8192],8192);
                        datagramSocket.receive(dp);
                        if (dp != null)
                        {
                            dp.setData("do \"get <file>\" function using UDP.".getBytes());
                            datagramSocket.send(dp);
                            return true;
                        }
                        break;
                    default:
                        pw.println("No such command : " + cmd);
                }
            }
            else {
                pw.println("No such command : " + cmd);
            }
        }
        else
        {
            pw.println("No such command : " + cmd);
        }
        return false;
    }

    private String parentPath(String crtPath)
    {
        File file = new File(crtPath);
        String parent = file.getParent();
        return  parent.substring(root.length(),parent.length()) + "/";
    }

    private boolean checkPath(String path)
    {
        File file = new File(path);
        return file.exists();
    }
}
