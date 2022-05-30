package com.game;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class server extends JFrame implements Runnable,ActionListener{
    private Socket s = null;
    private ServerSocket ss = null;
    private Random rnd = new Random();
    private MyJPanel jpl = new MyJPanel();
    private ArrayList<ChatThread> clients = new ArrayList<ChatThread>();//保存每个客户端连入的变长数组
    private JComboBox jcb = new JComboBox();
    private JLabel jlb = new JLabel();

    public server() throws Exception {
        this.add(jpl);
        jpl.setLayout(null);
        this.setTitle("服务器端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 200);
        this.setVisible(true);
        jpl.add(jcb);
        jpl.add(jlb);
        jlb.setSize(200, 20);
        jlb.setFont(new Font("楷体", Font.BOLD, 20));
        jlb.setForeground(Color.DARK_GRAY);
        jlb.setText("当前用户");
        jlb.setLocation(140, 30);
        jcb.setLocation(150, 50);
        jcb.setSize(70, 20);
        jcb.addItem(" ");
        jcb.setVisible(true);
        jcb.addActionListener(this);
        ss = new ServerSocket(2333);//服务器开辟一个端口
        new Thread(this).start();//接受客户连接的死循环开始运行
    }

    public void run() {//此线程是用来接收等待客户端不断连入时的线程
        try {
            while (true) {
                s = ss.accept();//等待接入
                ChatThread ct = new ChatThread(s);//有客户端接入的时候为其创造一个线程
                clients.add(ct);//将这个线程加入到线程数组
                jcb.addItem(ct.name);
                jcb.setVisible(true);
                ct.start();//启动这个线程
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "游戏异常退出！");
            System.exit(0);

        }
    }
    class ChatThread extends Thread {
        private Socket s = null;
        private BufferedReader br = null;
        private PrintStream ps = null;
        private boolean canRun = true;
        private String name;

        public ChatThread(Socket s) throws Exception {
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
            name=br.readLine();
            System.out.println(name);
        }

        public void sendMessage(String msg){
            for (ChatThread ct : clients) {
                ct.ps.println(msg);
            }
        }

        public void run() {
            try {
                //开始时，服务器首先生成一个大小有限制的随机数，并发给所有的客户端
                int wn = rnd.nextInt(2000);
                System.out.println("Start:" + wn);
                String swn = "START#" + Integer.toString(wn);
                sendMessage(swn);
                while (canRun) {
                    //在这里
                    String str = br.readLine();
                    String[] strs = str.split("#");
                    if (strs[0].equals("LIFE")) {
//                        String[] strin =strs[1].split("$");
                        //将生命值转发给所有的客户端
                        //sendMessage(strs[1]);
                        int rn = rnd.nextInt(2000);
                        //System.out.println("RdNumber:" + rn);
                        //System.out.println("减或加生命值");
                        String srn = "START#" + Integer.toString(rn);
                        sendMessage("LIFE#" + strs[1] + "#" + srn);
                        System.out.println("LIFE#" + strs[1] + "#" + srn);
                    } else if (strs[0].equals("WIN")) {
                        //有一方生命值已经归为0，另一方胜利
                        String msgWIN = "UWIN#";
                        sendMessage(msgWIN);
                    } else if (strs[0].equals("ASKRN")) {
                        int rn1 = rnd.nextInt(2000);
                        String swn1 = "START#" + Integer.toString(rn1);
                        System.out.println("仅用于同步");
                        sendMessage("NOASK#"+ swn1);
                    }
                }
            } catch (Exception ex) {
                canRun = false;
                clients.remove(this);
                jcb.removeItem(name);
            }
        }
    }
    class MyJPanel extends JPanel {
        public void paintComponent(Graphics g) {
            Image img = new ImageIcon("img2.jpeg").getImage();
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), jpl);
        }
    }

    public static void main(String[] args)throws Exception{
        new server();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object name = jcb.getSelectedItem();
        if (null == name || name.equals(" "))
            return;
        int choice = JOptionPane.showConfirmDialog(this, "您确定要踢出" + name + "吗？");
        if (JOptionPane.YES_OPTION == choice) {
            for (ChatThread c : clients) {
                if (name.equals(c.name)) {
                    c.ps.println("-9999");
                    this.setTitle("用户" + name + "踢出成功");
                    jcb.removeItem(name);
                    JOptionPane.showMessageDialog(null, "踢除成功！");
                }else{
                    String msgWIN = "UWIN#";
                    c.ps.println(msgWIN);
                }

            }
        }
    }
}
