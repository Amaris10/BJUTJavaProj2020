import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

public class Server {

    private ServerSocket ss;
    private static final int  SERVER_PORT = 8080;
    private static DecimalFormat df = null;

    // 构造函数，在这里启动服务端
    public Server() {
        try {
            ss = new ServerSocket(SERVER_PORT);
            while(true) {
                Socket sc = ss.accept();
                new Thread(new FileDeliver(sc)).start();
            }
        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
    }

    /**
     * 服务线程
     * 客户端连接处理线程
     * 当主调函数中的accept方法捕捉到客户端发来socket连接请求时，新建该线程
     * 此线程中首先判断客户端发来的指令类型，然后提供相应的服务。
     */
    private class FileDeliver implements Runnable {

        Socket sc;
        DataInputStream dis;
        DataOutputStream dos;
        FileInputStream fis;
        FileOutputStream fos;

//        private static final String addr = "C:\\Users\\13911\\Desktop\\java项目：文件传输系统\\fileTest\\ServerFile";
        private static final String addr = ".\\fileTest\\ServerFile";

        public FileDeliver(Socket sc) {
            this.sc = sc;
        }

        /**
         * 控制函数
         * 判断客户端传来的指令类型，调用相应的指令处理函数
         * 此函数中应建立好数据传输管道，指令处理函数直接使用创建好的管道
         */
        @Override
        public void run() {
            try {
                dis = new DataInputStream(sc.getInputStream());
                dos = new DataOutputStream(sc.getOutputStream());

                // 监听客户机的指令
                String command = dis.readUTF();

                if (command.equals("UPLOAD")) {
                    upLoad();
                }

                else if (command.equals("DOWNLOAD")) {
                    downLoad();
                }

                else if (command.equals("ALLFILES")) {
                    allFiles();
                }

                else {
                    System.out.println("无效指令");
                }

            } catch (IOException IOe) {
                IOe.printStackTrace();
            }
        }

        /**
         * 指令处理函数
         * 接收客户端的上传文件
         */
        private void upLoad() {
            // 接收上传文件
            System.out.println("收到上传文件请求");
            try{
                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();

                File directory = new File(addr);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                fos = new FileOutputStream(file);

                // 开始接收文件
                System.out.println("======== 开始接收文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                    progress += length;
                    System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                System.out.println();
                System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength) + "] ========");
                dis.close();
                dos.close();

            } catch (Exception e) {

            }
        }

        /**
         * 指令处理函数
         * 向客户端下载文件
         */
        private void downLoad() {
            System.out.println("收到下载文件请求");
            try {

                // 判断文件名是否合法
                String fileName = dis.readUTF();
                File file = new File(addr+"//"+fileName);

                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);

                    // 发送"文件名合法"消息
                    dos.writeUTF("SUCCESS");
                    dos.flush();

                    // 文件长度
                    dos.writeLong(file.length());
                    dos.flush();

                    // 开始传输文件
                    System.out.println("======== 开始下发文件 ========");
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    long progress = 0;
                    while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                        dos.write(bytes, 0, length);
                        dos.flush();
                        progress += length;
                        System.out.print("| " + (100 * progress / file.length()) + "% |");
                    }
                    System.out.println();
                    System.out.println("======== 文件下发成功 ========");
                    dis.close();
                    dos.close();
                } else {
                    // 发送"文件名非法"消息
                    dos.writeUTF("FAIL");
                    dos.flush();
                    System.out.println("客户端发送的文件名无效");
                }

            } catch (Exception e) {

            }
        }

        /**
         * 指令处理函数
         * 返回服务端的全部文件名
         */
        private void allFiles() {
            String[] nameList=new File(addr).list();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(sc.getInputStream());

                oos.writeObject(nameList);
                oos.flush();
                oos.close();
            } catch (IOException IOe) {
                IOe.printStackTrace();
            }
        }
    }

    /**
     * 服务函数
     * 格式化文件大小
     */
    private String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if (size >= 1) {
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if (size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if (size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }

    public static void main(String[] args) {
        new Server();
    }
}
