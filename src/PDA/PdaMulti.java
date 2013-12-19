/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PDA;

import java.awt.Color;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the PDA system, It is supposed to simulate a person having a
 * PDA on him while walking into vehicles with Train Servers from the same
 * system.
 *
 * @author Mikael Kofod, Sune Heide
 */
public class PdaMulti extends Thread {

    int multiPort = 5000;
    int uniPort = multiPort + 1;
    int uniPort2 = uniPort + 1;
    PdaGui pdag;
    PdaUni pdaU;
//    UnicastThread uniRe;
    String owner;
    String password;
    int count = 0;
    boolean sendLogin = true;

    /**
     * Constructor which receives username and password parameters from the
     * PdaGui classes and uses them throughout the entire class, pdaG is also
     * initialized here for updates on the GUI, The constructor also calls a
     * method that starts the thread.
     *
     * @param pdag
     * @param username
     * @param password
     */
    public PdaMulti(PdaGui pdag, String username, String password, PdaUni pdaU) {
        this.pdag = pdag;
        this.owner = username;
        this.password = password;
        this.pdaU = pdaU;
    }

    /**
     * This run() method starts the thread that listens to the same group as the
     * Train Server spams messages on, When the fifth multicast is received the
     * if statement changes conditions If 10 seconds pass by without receiving a
     * multicast the program will restart.
     */
    @Override
    public void run() {

        try {
            String group = "239.192.100.100";
            InetAddress multiAddress;
            DatagramPacket packMulti;

            multiAddress = InetAddress.getByName(group);
            MulticastSocket socket = new MulticastSocket(multiPort);
            socket.joinGroup(multiAddress);

            byte[] receivedData = new byte[1024];
            packMulti = new DatagramPacket(receivedData, receivedData.length);
            socket.setSoTimeout(10000);

            while (pdag.startThread) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Multicast thread is interrupted now");
                    break;
                }
                if (count < 4) {
                    socket.receive(packMulti);
                    pdag.pdaColorPanel.setBackground(Color.YELLOW);
                    System.out.println("Jeg har modtaget MULTICAST");
                    count++;
                } else {
                    socket.receive(packMulti);
                    handleMultiPack(packMulti);
                }
            }
            socket.close();
        } catch (SocketTimeoutException ex) {
            System.out.println("No response from Train Server - Multicast");
            pdag.pdaColorPanel.setBackground(Color.BLUE);
            sendLogin = true;
            count = 0;
            run();
        } catch (Exception e) {
            Logger.getLogger(PdaMulti.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * This method is called after a multicast has been received and will reply
     * corresponding to how far the program is, For the first reply the if
     * statements condition will be true, after the first reply it will continue
     * spamming the same message till the thread is closed.
     *
     * @param pack
     */
    public void handleMultiPack(DatagramPacket pack) {
        String[] split;
        InetAddress address;

        String retrieved = new String(pack.getData(), 0, pack.getLength());
        split = retrieved.split(" ");
        //Vi modtager HEJ IP:PORT
        address = pack.getAddress();

        pack.setData(new byte[2048]);

        if (split[0].equals("HEJ")) {
            try {
                if (sendLogin) {
                    sendUnicast("LOGIN " + owner + " " + password, address);
                    System.out.println("Jeg har modtaget HEJ vi sender LOGIN");
                    sendLogin = false;
                } else if (pdaU.acceptCheck) {
                    sendUnicast("PING " + owner + " " + password, address);
                    System.out.println("Jeg har modtaget HEJ og vi sender PING");
                }
            } catch (Exception e) {
                Logger.getLogger(PdaMulti.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
//
//    /**
//     * This method requires a message and a destination address, this is to
//     * ensure that an understandable message is sent to the correct address so
//     * that the communication between the two programs works.
//     *
//     * @param msg
//     * @param IPaddress
//     */

    public void sendUnicast(String msg, InetAddress IPaddress) {
        try {
            DatagramSocket socket = new DatagramSocket();

            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, IPaddress, uniPort2);
            socket.send(packet);

        } catch (Exception e) {
            Logger.getLogger(PdaMulti.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }
}