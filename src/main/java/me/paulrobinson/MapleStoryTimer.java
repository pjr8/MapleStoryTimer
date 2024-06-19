/*
    @Author: Paul Robinson
    @Date: 6/18/2024
    @Description: A simple overlay for MapleStory that will remind you to pick up items before they despawn.
 */

package me.paulrobinson;

import javax.swing.*;
import java.awt.*;

public class MapleStoryTimer extends JFrame {

    private static final long FARM_TIME = 140_000;
    private static final long PICKUP_TIME = 25_000;
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private ImageIcon pickUpIcon;
    private JFrame pickUpFrame = null;
    private long lastFarmTime = System.currentTimeMillis();
    private long lastPickupTime;
    private boolean farming = true;

    public MapleStoryTimer() {
        super("Maplestory Timer");
        setupPickUpIcon();
        setupPopUpFrame();
        setUndecorated(true);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);
        setFocusable(false);
        setBackground(new Color(128, 128, 128));
        double x = (screenSize.width - getSize().width) * 0.75;
        int y = (screenSize.height - getSize().height)/4;
        setLocation((int) x, y);
        setOpacity(0.75f);
        JTextPane textPane = new JTextPane();
        textPane.setBackground(new Color(128, 128, 128));
        textPane.setFocusable(false);
        textPane.setText("Farming: 140s");
        textPane.setFont(new Font("Arial", Font.BOLD, 60));
        add(textPane);
        Timer timer = new Timer(10, e -> {
            long currentTime = System.currentTimeMillis();
            if (farming) {
                if (currentTime - lastFarmTime >= FARM_TIME) {
                    lastPickupTime = currentTime;
                    farming = false;
                    pickUpPopUp();
                } else {
                    textPane.setText("Farming: " + ((FARM_TIME / 1000) - (Math.abs(lastFarmTime - currentTime) / 1000)) + "s");
                }
            } else {
                if (currentTime - lastPickupTime >= PICKUP_TIME) {
                    farming = true;
                    lastFarmTime = currentTime;
                } else {
                    textPane.setText("Pickup: " + ((PICKUP_TIME / 1000) - (Math.abs(lastPickupTime - currentTime) / 1000)) + "s");
                }
            }
        });
        timer.start();
        setVisible(true);
        pack();

    }

    private void pickUpPopUp() {
        pickUpFrame.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                pickUpFrame.setVisible(false);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }).start();
    }

    private void setupPopUpFrame() {
        pickUpFrame = new JFrame();
        pickUpFrame.setUndecorated(true);
        JLabel lbl = new JLabel(pickUpIcon);
        pickUpFrame.getContentPane().add(lbl);
        pickUpFrame.setBackground(new Color(0, 0, 0, 0));
        pickUpFrame.setSize(pickUpIcon.getIconWidth(), pickUpIcon.getIconHeight());
        pickUpFrame.setFocusable(false);
        pickUpFrame.setAutoRequestFocus(false);

        int x = (screenSize.width - pickUpFrame.getSize().width)/2;
        int y = (screenSize.height - pickUpFrame.getSize().height)/4;
        pickUpFrame.setLocation(x, y);
        pickUpFrame.setAlwaysOnTop(true);
    }

    private void setupPickUpIcon() {
        ImageIcon im = new ImageIcon("src/main/resources/PickUpBetter.png");
        pickUpIcon = new ImageIcon(im.getImage().getScaledInstance(250, 250,  java.awt.Image.SCALE_SMOOTH));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MapleStoryTimer::new);
    }
}