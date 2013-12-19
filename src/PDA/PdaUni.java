/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PDA;

import java.awt.Color;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mikael Kofod, Sune Heide
 */
public class PdaUni extends Thread {

    int multiPort = 5000;
    int uniPort = multiPort + 1;
    int uniPort2 = uniPort + 1;
    PdaGui pdag;
    String owner;
    boolean acceptCheck = false;

    public PdaUni(PdaGui pdag, String username) {
        this.pdag = pdag;
        this.owner = username;
    }

    @Override
    public void run() {
        try {
            DatagramPacket packUni;
            DatagramSocket socket = new DatagramSocket(uniPort);
            socket.setSoTimeout(500);
            byte buf[] = new byte[2048];
            packUni = new DatagramPacket(buf, buf.length);
            while (pdag.startThread) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Unicaster Thread is interrupted");
                    break;
                }
                try {
                    socket.receive(packUni);
                } catch (SocketTimeoutException e) {
                }
                handleUniPack(packUni);
            }
            socket.close();
        } catch (Exception e) {
            Logger.getLogger(PdaUni.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * This method ensures that the correct steps are taken after a unicast has
     * been received, whether this is to send another unicast or to stop the
     * PDA.
     *
     * @param pack
     */
    public void handleUniPack(DatagramPacket pack) {
        String[] split;
        InetAddress address;
        String retrieved = new String(pack.getData(), 0, pack.getLength());
        split = retrieved.split(" ");
        address = pack.getAddress();

        pack.setData(new byte[2048]);

        if (split[0].equals("ACCEPTED")) {
            sendUnicast("PING " + owner, address);
            System.out.println("Jeg har modtaget ACCEPTED og vi sender PING");
            pdag.pdaColorPanel.setBackground(Color.GREEN);
            acceptCheck = true;
        }
        if (split[0].equals("DECLINED")) {
            System.out.println("Jeg har modtaget DECLINED");
            pdag.pdaColorPanel.setBackground(Color.RED);
            pdag.interruptThreads();
        }
    }

    /**
     * This method requires a message and a destination address, this is to
     * ensure that an understandable message is sent to the correct address so
     * that the communication between the two programs works.
     *
     * @param msg
     * @param IPaddress
     */
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
