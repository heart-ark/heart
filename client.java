package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Random;

import static javax.swing.JOptionPane.showMessageDialog;

public class client extends JPanel implements ActionListener,Runnable
{
    private int life = 10;


    private JLabel lbLife = new JLabel();//生命模块
    private JLabel lbMoveChar = new JLabel();//掉落的单词模块
    private JTextField tfseparate = new JTextField();//分隔用的线（框高度为一）
    private JTextField tfseparate1 = new JTextField();//截止线
    private JTextField tfword = new JTextField();//输入的单词框
    private JLabel tfwordtips = new JLabel();//单词提示框
    public String name = JOptionPane.showInputDialog("输入用户名");//弹窗
    public void paintComponent(Graphics g) {
        Image img = new ImageIcon("img.jpg").getImage();
        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
    }

    private String right="已掌握单词.txt";
    private String wrong="未掌握单词.txt";

    private Socket  s = null;
    private Timer timer = new Timer(300,this);


    private String word = null;
    private String chinese = null;

    private Random rnd = new Random();
    private BufferedReader br = null;
    private PrintStream ps = null;
    private String filenameTemp;
    private boolean flag=false;

    private int il; //靠随机数来指向对应行数
    String strSave = null;

    private boolean canRun = true;

    public client( ) throws Exception
    {
        this.setLayout(null);
        this.setSize(900, 450);

        File file=new File("E:/"+name);
        file.mkdir();//建立文件夹
        filenameTemp="E:\\";
        File filename1 = new File("E:/"+name+"/"+right);
        File filename2 = new File("E:/"+name+"/"+wrong);
        if (!filename1.exists()) {//建立相应文本
            filename1.createNewFile();
            filename2.createNewFile();
        }

        this.add(lbLife);
        lbLife.setFont(new Font("华文彩云", Font.BOLD, 20));
        lbLife.setForeground(Color.white);
        lbLife.setBounds(0, 0, 300, 20);

        this.add(lbMoveChar);
        lbMoveChar.setFont(new Font("华文琥珀", Font.BOLD, 20));
        lbMoveChar.setForeground(Color.white);

        this.add(tfseparate);
        tfseparate.setSize(900, 1);
        tfseparate.setLocation(0, 343);

        this.add(tfseparate1);
        tfseparate1.setSize(900, 6);
        tfseparate1.setLocation(0, 295);
        tfseparate1.setBackground(Color.white);

        this.add(tfword);
        tfword.setLocation(100, 355);
        tfword.setSize(750, 50);
        tfword.setBackground(Color.gray);
        tfword.setForeground(Color.cyan);

        this.add(tfwordtips);
        tfwordtips.setLocation(100, 305);
        tfwordtips.setSize(200, 35);
        tfwordtips.setFont(new Font("华文琥珀", Font.BOLD, 20));
        tfwordtips.setForeground(Color.cyan);

        //以上都是控件颜色尺寸设置及放置

        this.init();//调用init函数进行初始化

        try{
            s = new Socket("127.0.0.1",2333);
            InputStream is = s.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = s.getOutputStream();
            ps = new PrintStream(os);
            ps.println(name);
            new Thread(this).start();//多线程调用run方法

        }catch (Exception ex){
            showMessageDialog(this,"游戏异常退出！");//提示框
            System.exit(0);

        }
        tfword.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent e) {
                if((char)e.getKeyChar()==KeyEvent.VK_ENTER) {
                    try{
                        if((tfword.getText()).equals(word))
                        {
                            // boolean a = true;
                            System.out.println("回答正确");
//                            timer.stop();
                            showmassage("恭喜回答正确");
                            writeFile(filenameTemp+name+"\\"+right,strSave+"\r\n");
                            life+=1;
                            ps.println("LIFE#0");

                        }
                        else{
                            System.out.println("答案错误");
                            //System.out.println("输入框的文本"+tfword.getText());
                            //System.out.println("当前的word为："+word);
                            showmassage("回答错误，答案是"+word);
                            writeFile(filenameTemp+name+"\\"+wrong,strSave+"        回答错误"+"\r\n");
                            life-=2;
                            //用于向服务器标识需要随机数，对方生命值加一
                            ps.println("LIFE#0");
                        }
                        init();
                        checkFail();
                        //javax.swing.JOptionPane.showMessageDialog(null,"1！");
                    } catch (Exception ex) {
                        canRun = false;
                        //javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
                        System.exit(0);
                    }
                }
            }
        });
        timer.start();
    }
    public void showmassage(String str){
        showMessageDialog(this,str);
    }
    public void writeFile(String filename, String str) {
        try {
            FileOutputStream fos = new FileOutputStream(filename, true);
            byte[] b = str.getBytes();
            fos.write(b);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//写文件操作，不同场景下写入的文件不一样


    public void readWord(String filename, int il)
    {
    try {
            FileInputStream fi = new FileInputStream(filename);//从文件中获取字节
            InputStreamReader isr = new InputStreamReader(fi, "UTF-8");//字节流，指定编码格式，充当转换桥梁
            BufferedReader br = new BufferedReader(isr);//字符流
            //br此处用来按行读取文档
            while (br.readLine() != null && il >= 0)
            {
                il--;
                //String str;
                if (il < 0)
                {
                    //用来按行读取，并分割出单词和解释
                    //String的split方法支持正则表达式；
                    //正则表达式\s表示匹配任何空白字符，+表示匹配一次或多次。
                    String str1 = br.readLine();
                    //保存的字符串
                    strSave = str1;//加入换行符
                    String[] strs1 = str1.split("\\s+");
                    word = strs1[0];//提取出单词，0存单词，1存翻译
                    chinese = strs1[1];
                    System.out.println("1单词英文：" + word);
                    System.out.println("1单词中文：" + strs1[1]);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public String RandWord(String word){
        Random r = new Random();
        int length=word.length();
        StringBuffer randword=new StringBuffer();
        int rn1=r.nextInt(length);
        int rn2=r.nextInt(length);
        while (rn2==rn1){rn2=r.nextInt(length);}
        for(int i=0;i<length;i++){
            if(i==rn1 || i==rn2){
                randword.append(word.charAt(i));
            }else{
                randword.append("_");
            }
            randword.append(" ");
        }
        return randword.toString();
    }

    public void init()//用来更新生命值，读取单词，设置单词提示，设置单词意思，设置掉落位置
    {
        lbLife.setText("当前生命值：" + life);//设置当前生命值
        readWord("C:\\Users\\HEART\\Desktop\\word.txt", il);//读取单词
        tfwordtips.setText(RandWord(word));
        lbMoveChar.setText(chinese); //掉落模块的内容初始化
        lbMoveChar.setBounds(400,0,200,50);//初始掉落位置
        tfword.setText("");//将输入框置空
    }

    public void checkFail(){
        lbLife.setText("当前生命值：" + life);
        if(life<-100){
            JOptionPane.showMessageDialog(this, "您已被服务器踢出");
            System.exit(0);
        }else if(life <= 0){
            ps.println("WIN#");
            timer.stop();
            showMessageDialog(this,"生命值耗尽，游戏失败！");
            System.exit(0);
        }
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(lbMoveChar.getY() >= 260){
            writeFile(filenameTemp+name+"\\"+wrong,strSave+"         未回答"+"\\r\\n");
            //life--;
            showMessageDialog(this,"未回答，正确答案是"+word);
            checkFail();
            //随机数由服务器产生
            ps.println("ASKRN#");
            //System.out.println("底部调用checkFail");
        }
        lbMoveChar.setLocation(lbMoveChar.getX(),lbMoveChar.getY()+5);
    }//让单词动起来，还要加上判断是否超过区域


    @Override
    public void run() {
        try{
            while(canRun){
                String str = br.readLine();
//                System.out.println(str);
                if(str.equals("-9999")){
                    life-=9999;
                    checkFail();
//                    System.out.println(life);
                }
                String[] strs = str.split("#");
                //判断从服务器接收到的消息是初始化number（用作随机读词）
                if(strs[0].equals("START")){
                    il = Integer.parseInt(strs[1]);
                    //判断是减生命值的消息
                }else if(strs[0].equals("LIFE")) {
                    //若消息是既包含LIFE又包含RND
                    int score = Integer.parseInt(strs[1]);
                    //实现生命值的减少
                    life+=score;
                    checkFail();
                    //如果strs[]的格式为“LIFE#-1#START#srn"
                    if(strs[2].equals("START")){
                        il = Integer.parseInt(strs[3]);
                    }
                }else if(strs[0].equals("UWIN")){
                    //你赢了并退出游戏
                    timer.stop();
                    showMessageDialog(this,"游戏结束，你赢了！");
                    System.exit(0);
                }else if(strs[0].equals("NOASK")){
                    //if(strs[1].equals("START"))
                    life--;
                    il = Integer.parseInt(strs[2]);
                }
                init();
            }
        }catch (Exception ex){
            canRun = false;
            showMessageDialog(this,"游戏异常退出！");
            System.exit(0);
        }
    }

    public static void main(String[] args)throws Exception {
        new frame();
    }

}

