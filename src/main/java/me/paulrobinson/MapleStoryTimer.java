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

    //Standard time to farm ~140s
    private static final long FARM_TIME = 140_000;

    //Standard time to pick up ~25s
    private static final long PICKUP_TIME = 25_000;

    //Grabs the screen size
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    //Icon for the pick up reminder (initialized in the constructor)
    private ImageIcon pickUpIcon;

    //JFrame for the pick up reminder (initialized in the constructor)
    private JFrame pickUpFrame = null;

    //Counts the time since the last farm, used to determine when to pick up
    private long lastFarmTime = System.currentTimeMillis();

    //Counts the time since the last pick up, used to determine when to farm
    private long lastPickupTime;

    //If true, the user is farming, if false, the user is picking up
    private boolean farming = true;

    //Graphics component for the timer
    private final TimerGraphicsComponent timerGraphicsComponent;

    //Scale for the timer's font & ui size.
    private static double scale = 1.0;

    public MapleStoryTimer() { //Start

        //Initialize JFrame
        super("Maplestory Timer"); //This is pointless, you can't even see the title! xD

        //Set up the JFrame
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

        //Set the location of the JFrame depending on the screen size
        double x = (screenSize.width - getSize().width) * 0.75;
        int y = (screenSize.height - getSize().height)/4;
        setLocation((int) x, y);

        //Initialization & setup of the timer graphics component
        timerGraphicsComponent = new TimerGraphicsComponent("test");
        timerGraphicsComponent.setSize(400, 400);
        timerGraphicsComponent.setVisible(true);
        add(timerGraphicsComponent);

        //Show the JFrame to user.
        setVisible(true);
        pack();

        //Add a listener to the JFrame to allow the user to drag it around the screen (Credit to whoever I found this from on StackOverflow!)
        FrameDragListener frameDragListener = new FrameDragListener(this);
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        //Add a listener to the JFrame to allow the user to make the UI bigger or smaller.
        addMouseWheelListener(MouseWheelListener -> {
            if (MouseWheelListener.getWheelRotation() > 0) {
                if (scale <= 0.1) return;
                setSize(new Dimension(getWidth() - 42, getHeight() - 7));
                scale -= 0.1;
            } else {
                if (scale >= 5.0) return; //Capped at 5. Who needs a bigger timer?
                setSize(new Dimension(getWidth() + 42, getHeight() + 7));
                scale += 0.1;
            }
        });

        Timer timer = new Timer(100, e -> { //Timer ran at 0.1s, runs the core logic of the program.
            long currentTime = System.currentTimeMillis();
            if (farming) { //User is farming
                if (currentTime - lastFarmTime >= FARM_TIME) { //Check if the user has farmed for long enough
                    lastPickupTime = currentTime;
                    farming = false;
                    givePickUpPopUp();
                } else { //If the user hasn't farmed for long enough, update the timer
                    timerGraphicsComponent.setToDraw("Farming: " + ((FARM_TIME / 1000) - (Math.abs(lastFarmTime - currentTime) / 1000)) + "s");
                    timerGraphicsComponent.repaint();
                }
            } else { //User is picking up all the good loot
                if (currentTime - lastPickupTime >= PICKUP_TIME) { //Check if the user has picked up for long enough
                    farming = true;
                    lastFarmTime = currentTime;
                } else { //If the user hasn't picked up for long enough, update the timer
                    timerGraphicsComponent.setToDraw("Pickup: " + ((PICKUP_TIME / 1000) - (Math.abs(lastPickupTime - currentTime) / 1000)) + "s");
                    timerGraphicsComponent.repaint();
                }
            }
        });
        timer.start();
        setVisible(true);
    }

    //Shows the pick up and sound effect reminder to the user
    private void givePickUpPopUp() {
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

    //Sets up the pop up frame for the pick up reminder
    private void setupPopUpFrame() {

        //Setup the JFrame
        pickUpFrame = new JFrame();
        pickUpFrame.setUndecorated(true);
        JLabel lbl = new JLabel(pickUpIcon);
        pickUpFrame.getContentPane().add(lbl);
        pickUpFrame.setBackground(new Color(0, 0, 0, 0));
        pickUpFrame.setSize(pickUpIcon.getIconWidth(), pickUpIcon.getIconHeight());
        pickUpFrame.setFocusable(false);
        pickUpFrame.setAutoRequestFocus(false);

        //Set the location of the JFrame depending on the screen size
        int x = (screenSize.width - pickUpFrame.getSize().width)/2;
        int y = (screenSize.height - pickUpFrame.getSize().height)/4;
        pickUpFrame.setLocation(x, y);
        pickUpFrame.setAlwaysOnTop(true);
    }

    //Loads the icon during initialization
    private void setupPickUpIcon() {
        ImageIcon im = new ImageIcon("src/main/resources/PickUpBetter.png");
        pickUpIcon = new ImageIcon(im.getImage().getScaledInstance(250, 250,  Image.SCALE_SMOOTH));
    }

    //Plays the maple meso collect sound to alert the user
    private void playAudio() {
        try {
            new Player(Objects.requireNonNull(getClass().getResourceAsStream("/maplemesocollect.mp3"))).play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }

    //Class for drawing the timer on the screen (Credit to whoever I found this from on StackOverflow!)
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

    //Class for dragging the timer (Credit to whoever I found this from on StackOverflow!)
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

    //Boilerplate code to start the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MapleStoryTimer::new);
    }
}