/*
    @Author: Paul Robinson
    @Date: 6/18/2024
    @Description: A simple overlay for MapleStory that will remind you to pick up items before they despawn.
 */

package me.paulrobinson;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class MapleStoryTimer extends JFrame {

    private static final long FARM_TIME = 140_000;
    private static final long PICKUP_TIME = 25_000;
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private ImageIcon pickUpIcon;
    private JFrame pickUpFrame = null;
    private long lastFarmTime = System.currentTimeMillis();
    private long lastPickupTime;
    private boolean farming = true;
    private final TimerGraphicsComponent timerGraphicsComponent;
    private static double scale = 1.0;

    public MapleStoryTimer() {
        super("Maplestory Timer");
        setupPickUpIcon();
        setupPopUpFrame();
        setPreferredSize(new Dimension(425, 100));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setAutoRequestFocus(false);
        setFocusable(false);
        setOpacity(1f);
        setBackground(new Color(0, 0, 0, 2));
        double x = (screenSize.width - getSize().width) * 0.75;
        int y = (screenSize.height - getSize().height)/4;
        setLocation((int) x, y);
        timerGraphicsComponent = new TimerGraphicsComponent("test");
        timerGraphicsComponent.setSize(400, 400);
        timerGraphicsComponent.setVisible(true);
        add(timerGraphicsComponent);
        setVisible(true);
        pack();
        FrameDragListener frameDragListener = new FrameDragListener(this);
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);
        addMouseWheelListener(MouseWheelListener -> {
            if (MouseWheelListener.getWheelRotation() > 0) {
                if (scale <= 0.1) return;
                setSize(new Dimension(getWidth() - 42, getHeight() - 7));
                scale -= 0.1;
            } else {
                if (scale >= 5.0) return;
                setSize(new Dimension(getWidth() + 42, getHeight() + 7));
                scale += 0.1;
            }
        });
        Timer timer = new Timer(100, e -> {
            long currentTime = System.currentTimeMillis();
            if (farming) {
                if (currentTime - lastFarmTime >= FARM_TIME) {
                    lastPickupTime = currentTime;
                    farming = false;
                    pickUpPopUp();
                } else {
                    timerGraphicsComponent.setToDraw("Farming: " + ((FARM_TIME / 1000) - (Math.abs(lastFarmTime - currentTime) / 1000)) + "s");
                    timerGraphicsComponent.repaint();
                }
            } else {
                if (currentTime - lastPickupTime >= PICKUP_TIME) {
                    farming = true;
                    lastFarmTime = currentTime;
                } else {
                    timerGraphicsComponent.setToDraw("Pickup: " + ((PICKUP_TIME / 1000) - (Math.abs(lastPickupTime - currentTime) / 1000)) + "s");
                    timerGraphicsComponent.repaint();
                }
            }
        });
        timer.start();
        setVisible(true);
    }

    private void pickUpPopUp() {
        pickUpFrame.setVisible(true);
        playAudio();
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
        pickUpIcon = new ImageIcon(im.getImage().getScaledInstance(250, 250,  Image.SCALE_SMOOTH));
    }

    private void playAudio() {
        try {
            new Player(Objects.requireNonNull(getClass().getResourceAsStream("/maplemesocollect.mp3"))).play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }

    static class TimerGraphicsComponent extends JComponent {

        private String toDraw;

        public TimerGraphicsComponent(String toDraw) {
            this.toDraw = toDraw;
        }

        public void setToDraw(String toDraw) {
            this.toDraw = toDraw;
        }

        @Override
        public void paintComponent(Graphics g) {
            if(g instanceof Graphics2D g2)
            {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                double fontSizeDouble = 60 * scale;
                int fontSize = (int) fontSizeDouble;
                int y = 50;
                if (scale > 1.0) {
                    double yDouble = 50 * scale;
                    y = (int) yDouble;
                }
                g2.setFont(new Font("Arial", Font.BOLD, fontSize));
                g2.setColor(Color.RED);
                g2.drawString(toDraw,0, y);
            }
        }
    }

    static class FrameDragListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MapleStoryTimer::new);
    }
}