package com.example.i3remote;

import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.io.InputStream;

public class SSHConnection {
    private JSch jschSSHChannel;
    private String userName;
    private String connectionIP;
    private int connectionPort;
    private String password;
    private Session session;
    private int timeOut;

    public SSHConnection(Host host) {
        jschSSHChannel = new JSch();

        this.userName = host.user;
        this.password = host.password;
        this.connectionIP = host.hostname;
        this.connectionPort = 22;
        this.timeOut = 60000;

        connect();


      /*
      String xhost="127.0.0.1";
      int xport=0;
      String display=JOptionPane.showInputDialog("Enter display name",
                                                 xhost+":"+xport);
      xhost=display.substring(0, display.indexOf(':'));
      xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
      session.setX11Host(xhost);
      session.setX11Port(xport+6000);
      */

        // username and password will be given via UserInfo interface.
//            UserInfo ui=new MyUserInfo();
//            session.setUserInfo(ui);
    }

    private void connect() {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... ints) {
                String errorMessage = null;

                try {
                    session = jschSSHChannel.getSession(userName,
                            connectionIP, connectionPort);
                    session.setPassword(password);
                    java.util.Properties config = new java.util.Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.connect(timeOut);
                } catch (JSchException jschX) {
                    errorMessage = jschX.getMessage();
                }

                System.out.println("****************************error: " + errorMessage);
                return null;
            }
        }.execute(0);
    }

    public void sendParallelCommand(Command command) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                StringBuilder outputBuffer = new StringBuilder();

                try {
                    Channel channel = session.openChannel("exec");
                    ((ChannelExec) channel).setCommand(strings[0]);

                    // X Forwarding
                    // channel.setXForwarding(true);

                    channel.setInputStream(null);

                    //channel.setOutputStream(System.out);

                    //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
                    //((ChannelExec)channel).setErrStream(fos);
                    ((ChannelExec) channel).setErrStream(System.err);

                    InputStream in = channel.getInputStream();

                    channel.connect();

                    byte[] tmp = new byte[1024];
                    while (true) {
                        while (in.available() > 0) {
                            int i = in.read(tmp, 0, 1024);
                            if (i < 0) break;
                            System.out.print(new String(tmp, 0, i));
                        }
                        if (channel.isClosed()) {
                            if (in.available() > 0) continue;
                            System.out.println("exit-status: " + channel.getExitStatus());
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ee) {
                        }
                    }

                    channel.disconnect();
                } catch (IOException ioX) {
                    System.out.println(ioX.getMessage());
                } catch (JSchException jschX) {
                    System.out.println(jschX.getMessage());
                }
                return null;
            }
        }.execute(command.getCommand());
    }

    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // X Forwarding
            // channel.setXForwarding(true);

            channel.setInputStream(null);

            //channel.setOutputStream(System.out);

            //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            //((ChannelExec)channel).setErrStream(fos);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            channel.disconnect();
        } catch (IOException ioX) {
            System.out.println(ioX.getMessage());
            return null;
        } catch (JSchException jschX) {
            System.out.println(jschX.getMessage());
            return null;
        }

        return outputBuffer.toString();
    }

    public void sendCommand(Command command) {
        new AsyncTask<Command, Void, Void>() {
            @Override
            protected Void doInBackground(Command... commands) {
                try {
//                    session = jschSSHChannel.getSession(userName,
//                            connectionIP, connectionPort);
//                    session.setPassword(password);
//                    java.util.Properties config = new java.util.Properties();
//                    config.put("StrictHostKeyChecking", "no");
//                    session.setConfig(config);
//                    session.connect(timeOut);

                    // call sendCommand for each command and the output
                    //(without prompts) is returned
                    String result = sendCommand(commands[0].getCommand());
                    // close only after all commands are sent

                    System.out.println("****************************result: " + result);

//                    session.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(command);
    }

    public void disconnect() {
        session.disconnect();
    }


//    public static class MyUserInfo implements UserInfo {
//        public String getPassword(){ return passwd; }
//        public boolean promptYesNo(String str){
//            Object[] options={ "yes", "no" };
//            int foo=JOptionPane.showOptionDialog(null,
//                    str,
//                    "Warning",
//                    JOptionPane.DEFAULT_OPTION,
//                    JOptionPane.WARNING_MESSAGE,
//                    null, options, options[0]);
//            return foo==0;
//        }
//
//        String passwd;
//
//        public String getPassphrase(){ return null; }
//        public boolean promptPassphrase(String message){ return true; }
//        public boolean promptPassword(String message){
//            Object[] ob={passwordField};
//            int result=
//                    JOptionPane.showConfirmDialog(null, ob, message,
//                            JOptionPane.OK_CANCEL_OPTION);
//            if(result==JOptionPane.OK_OPTION){
//                passwd=passwordField.getText();
//                return true;
//            }
//            else{
//                return false;
//            }
//        }
//        public void showMessage(String message){
//            JOptionPane.showMessageDialog(null, message);
//        }
//        final GridBagConstraints gbc =
//                new GridBagConstraints(0,0,1,1,1,1,
//                        GridBagConstraints.NORTHWEST,
//                        GridBagConstraints.NONE,
//                        new Insets(0,0,0,0),0,0);
//        private Container panel;
//        public String[] promptKeyboardInteractive(String destination,
//                                                  String name,
//                                                  String instruction,
//                                                  String[] prompt,
//                                                  boolean[] echo){
//            panel = new JPanel();
//            panel.setLayout(new GridBagLayout());
//
//            gbc.weightx = 1.0;
//            gbc.gridwidth = GridBagConstraints.REMAINDER;
//            gbc.gridx = 0;
//            panel.add(new JLabel(instruction), gbc);
//            gbc.gridy++;
//
//            gbc.gridwidth = GridBagConstraints.RELATIVE;
//
//            JTextField[] texts=new JTextField[prompt.length];
//            for(int i=0; i<prompt.length; i++){
//                gbc.fill = GridBagConstraints.NONE;
//                gbc.gridx = 0;
//                gbc.weightx = 1;
//                panel.add(new JLabel(prompt[i]),gbc);
//
//                gbc.gridx = 1;
//                gbc.fill = GridBagConstraints.HORIZONTAL;
//                gbc.weighty = 1;
//                if(echo[i]){
//                    texts[i]=new JTextField(20);
//                }
//                else{
//                    texts[i]=new JPasswordField(20);
//                }
//                panel.add(texts[i], gbc);
//                gbc.gridy++;
//            }
//
//            if(JOptionPane.showConfirmDialog(null, panel,
//                    destination+": "+name,
//                    JOptionPane.OK_CANCEL_OPTION,
//                    JOptionPane.QUESTION_MESSAGE)
//                    ==JOptionPane.OK_OPTION){
//                String[] response=new String[prompt.length];
//                for(int i=0; i<prompt.length; i++){
//                    response[i]=texts[i].getText();
//                }
//                return response;
//            }
//            else{
//                return null;  // cancel
//            }
//        }
//    }
}
