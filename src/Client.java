import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {
    private static Socket sc;
    private static final int SERVER_PORT = 8080;
    private static final String CONNSTR = "127.0.0.1";

//    private static final String addr = "C:\\Users\\13911\\Desktop\\java项目：文件传输系统\\fileTest\\ClientFile";
        private static final String addr = ".\\fileTest\\ClientFile";
    private static DecimalFormat df = null;

    /**
     * 控制函数
     * 启动客户端，循环监听指令
     * 运行Start后，将进入循环监听指令的状态。
     * 当从输入流检测到了有效指令，就与服务器建立socket连接，并调用相关函数完成指令内容。
     * 指令执行完毕后，指令处理函数应自动关闭socket连接，程序将将恢复循环监听的状态。
     */
    private static void Start () {
        try {

            Scanner s = new Scanner(System.in);

            while (true) {

                System.out.print("Client1.0$:");
                String[] command = s.nextLine().split(" ");

                if (command[0].equals("UPLOAD")) {
                    // 上传文件
                    sc = new Socket(CONNSTR, SERVER_PORT);
                    upLoad(sc, command[1]);
                }
                else if(command[0].equals("DOWNLOAD")) {
                    // 下载文件
                    sc = new Socket(CONNSTR, SERVER_PORT);
                    downLoad(sc, command[1]);
                }
                else if(command[0].equals("ALLFILES")) {
                    // 获取服务端的全部文件名
                    sc = new Socket(CONNSTR, SERVER_PORT);
                    allFiles(sc);
                }
                else {
                    System.out.println("无效指令");
                }
            }

        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
    }

    /**
     * 指令处理函数
     * 上传文件到服务器
     * @param sc: 建立连接后的socket对象
     */
    private static void upLoad(Socket sc, String fileName) {
        try {
            // 上传文件
            DataInputStream dis = new DataInputStream(sc.getInputStream());
            DataOutputStream dos = new DataOutputStream(sc.getOutputStream());

            dos.writeUTF("UPLOAD");
            dos.flush();

            File file = new File(addr+"\\"+fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);

                // 文件名和长度
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                // 开始传输文件
                System.out.println("======== 开始上传文件 ========");
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
                System.out.println("======== 文件上传成功 ========");
                dis.close();
                dos.close();
            } else {
                System.out.println("没有找到文件，请检查文件名");
            }
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            try {
                sc.close();
            } catch (IOException IOe) {
                IOe.printStackTrace();
            }
        }
    }

    /**
     * 指令处理函数
     * 从服务器下载文件
     * @param sc: 建立连接后的socket对象
     */
    private static void downLoad(Socket sc, String fileName) {
        try {
            DataInputStream dis = new DataInputStream(sc.getInputStream());
            DataOutputStream dos = new DataOutputStream(sc.getOutputStream());

            dos.writeUTF("DOWNLOAD");
            dos.flush();

            // 向服务器请求文件名，判断文件名是否合法
            dos.writeUTF(fileName);
            dos.flush();

            if (dis.readUTF().equals("SUCCESS")) {

                // 文件长度
                long fileLength = dis.readLong();

                File directory = new File(addr);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                FileOutputStream fos = new FileOutputStream(file);

                // 开始接收文件
                System.out.println("======== 开始下载文件 ========");
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
            } else {
                System.out.println("服务器中无此文件");
            }

        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
    }

    /**
     * 指令处理函数
     * 查看服务器端所有文件名
     * @param sc: 建立连接后的socket对象
     */
    private static void allFiles(Socket sc) {
        try {
            DataOutputStream dos = new DataOutputStream(sc.getOutputStream());
            DataInputStream dis = new DataInputStream(sc.getInputStream());

            dos.writeUTF("ALLFILES");
            dos.flush();

            ObjectInputStream ois = new ObjectInputStream(sc.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());

            String [] nameList = (String[])ois.readObject();

            for(int i=0; i<nameList.length; i++) {
                System.out.println(nameList[i]);
            }

            ois.close();
            oos.close();
            dos.close();
            ois.close();

        } catch (IOException | ClassNotFoundException IOe) {
            IOe.printStackTrace();
        }
    }

    /**
     * 服务函数
     * 格式化文件大小
     * @param length: 文件长度（字节数）
     */
    private static String getFormatFileSize(long length) {
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

    // 构造函数
    public static void main(String[] args) {
        Start();
    }
}
