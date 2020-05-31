import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI {

    /** Reference to the original image. */
    private BufferedImage originalImage;
    /** Image used to make changes. */
    private BufferedImage canvasImage;
    /** The main GUI that might be added to a frame or applet. */
    private JPanel gui;
    /** The color to use when calling clear, text or other 
     * drawing functionality. */
    private Color color = Color.WHITE;
    /** General user messages. */
    private JLabel output = new JLabel("Assignment 1");

    private BufferedImage colorSample = new BufferedImage(
            16,16,BufferedImage.TYPE_INT_RGB);
    private JLabel imageLabel;
    private int activeTool;
    public static final int SELECTION_TOOL = 0;
    public static final int DRAW_TOOL = 1;
    public static final int TEXT_TOOL = 2;
    public static final int RECTANGLE_TOOL = 3;
    public static final int OVAL_TOOL = 4;
    public static final int LINE_TOOL = 5;
    public static final int CURVE_TOOL = 6;

    private Point selectionStart;
    private Point rectangleStart;
    private Rectangle selection;
    private Rectangle rectangleImage;
    private boolean dirty = false;
    private Stroke stroke = new BasicStroke(
            3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1.7f);
    private RenderingHints renderingHints;

    public JComponent getGui() {
        if (gui==null) {
            Map<Key, Object> hintsMap = new HashMap<RenderingHints.Key,Object>();
            hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            renderingHints = new RenderingHints(hintsMap);

            setImage(new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB));
            gui = new JPanel(new BorderLayout(4,4));
            gui.setBorder(new EmptyBorder(5,3,5,3));

            JPanel imageView = new JPanel(new GridBagLayout());
            imageView.setPreferredSize(new Dimension(480,320));
            imageLabel = new JLabel(new ImageIcon(canvasImage));
            JScrollPane imageScroll = new JScrollPane(imageView);
            imageView.add(imageLabel);
            imageLabel.addMouseMotionListener(new ImageMouseMotionListener());
            imageLabel.addMouseListener(new ImageMouseListener());
            gui.add(imageScroll,BorderLayout.CENTER);

            JToolBar tb = new JToolBar();
            tb.setFloatable(false);
            JButton colorButton = new JButton("Color");
            colorButton.setMnemonic('o');
            colorButton.setToolTipText("Choose a Color");
            ActionListener colorListener = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    Color c = JColorChooser.showDialog(
                            gui, "Choose a color", color);
                    if (c!=null) {
                        setColor(c);
                    }
                }
            };
            colorButton.addActionListener(colorListener);
            colorButton.setIcon(new ImageIcon(colorSample));
            tb.add(colorButton);

            setColor(color);

            final SpinnerNumberModel strokeModel =
                    new SpinnerNumberModel(3,1,16,1);
            JSpinner strokeSize = new JSpinner(strokeModel);
            ChangeListener strokeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    Object o = strokeModel.getValue();
                    Integer i = (Integer)o;
                    stroke = new BasicStroke(
                            i.intValue(),
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND,
                            1.7f);
                }
            };
            strokeSize.addChangeListener(strokeListener);
            strokeSize.setMaximumSize(strokeSize.getPreferredSize());
            JLabel strokeLabel = new JLabel("Stroke");
            strokeLabel.setLabelFor(strokeSize);
            strokeLabel.setDisplayedMnemonic('t');
            tb.add(strokeLabel);
            tb.add(strokeSize);

            tb.addSeparator();

            ActionListener clearListener = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    int result = JOptionPane.OK_OPTION;
                    if (dirty) {
                        result = JOptionPane.showConfirmDialog(
                                gui, "Erase the current painting?");
                    }
                    if (result==JOptionPane.OK_OPTION) {
                        clear(canvasImage);
                    }
                }
            };
            JButton clearButton = new JButton("Clear");
            tb.add(clearButton);
            clearButton.addActionListener(clearListener);

            gui.add(tb, BorderLayout.PAGE_START);

            JToolBar tools = new JToolBar(JToolBar.VERTICAL);
            tools.setFloatable(false);
            JButton crop = new JButton("Crop");
            final JRadioButton select = new JRadioButton("Select", true);
            final JRadioButton draw = new JRadioButton("Draw");
            final JRadioButton text = new JRadioButton("Text");
            final JRadioButton rectangle = new JRadioButton("Rectangle");
            final JRadioButton oval = new JRadioButton("Oval");
            final JRadioButton line = new JRadioButton("Line");
            final JRadioButton curve = new JRadioButton("Curve");

            tools.add(crop);
            tools.add(select);
            tools.add(draw);
            tools.add(text);
            tools.add(rectangle);
            tools.add(oval);
            tools.add(line);
            tools.add(curve);


            ButtonGroup bg = new ButtonGroup();
            bg.add(select);
            bg.add(text);
            bg.add(draw);
            bg.add(rectangle);
            bg.add(oval);
            bg.add(line);
            bg.add(curve);

            ActionListener toolGroupListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (ae.getSource()==select) {
                        activeTool = SELECTION_TOOL;
                    } else if (ae.getSource()==draw) {
                        activeTool = DRAW_TOOL;
                    } else if (ae.getSource()==text) {
                        activeTool = TEXT_TOOL;
                    }
                    else if (ae.getSource()==rectangle) {
                        activeTool = RECTANGLE_TOOL;
                    }
                    else if (ae.getSource()==oval) {
                        activeTool = OVAL_TOOL;
                    }
                    else if (ae.getSource()==line) {
                        activeTool = LINE_TOOL;
                    }
                    else if (ae.getSource()==curve) {
                        activeTool = CURVE_TOOL;
                    }
                }
            };
            select.addActionListener(toolGroupListener);
            draw.addActionListener(toolGroupListener);
            text.addActionListener(toolGroupListener);
            rectangle.addActionListener(toolGroupListener);
            oval.addActionListener(toolGroupListener);
            line.addActionListener(toolGroupListener);

            gui.add(tools, BorderLayout.LINE_END);

            gui.add(output,BorderLayout.PAGE_END);
            clear(colorSample);
            clear(canvasImage);
        }

        return gui;
    }

    /** Clears the entire image area by painting it with the current color. */
    public void clear(BufferedImage bi) {
        Graphics2D g = bi.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(color);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        g.dispose();
        imageLabel.repaint();
    }

    public void setImage(BufferedImage image) {
        this.originalImage = image;
        int w = image.getWidth();
        int h = image.getHeight();
        canvasImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.drawImage(image, 0, 0, gui);
        g.dispose();

        selection = new Rectangle(0,0,w,h);
        if (this.imageLabel!=null) {
            imageLabel.setIcon(new ImageIcon(canvasImage));
            this.imageLabel.repaint();
        }
        if (gui!=null) {
            gui.invalidate();
        }
    }

    /** Set the current painting color and refresh any elements needed. */
    public void setColor(Color color) {
        this.color = color;
        clear(colorSample);
    }

    private JMenu getFileMenu(boolean webstart){
        JMenu file = new JMenu("File");
        file.setMnemonic('f');

        JMenuItem newImageItem = new JMenuItem("New");
        newImageItem.setMnemonic('n');
        ActionListener newImage = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                BufferedImage bi = new BufferedImage(
                        360, 300, BufferedImage.TYPE_INT_ARGB);
                clear(bi);
                setImage(bi);
            }
        };
        newImageItem.addActionListener(newImage);
        file.add(newImageItem);

        if (webstart) {
            //TODO Add open/save functionality using JNLP API
        } else {
            //TODO Add save functionality using J2SE API
            file.addSeparator();
            ActionListener openListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (!dirty) {
                        JFileChooser ch = getFileChooser();
                        int result = ch.showOpenDialog(gui);
                        if (result==JFileChooser.APPROVE_OPTION ) {
                            try {
                                BufferedImage bi = ImageIO.read(
                                        ch.getSelectedFile());
                                setImage(bi);
                            } catch (IOException e) {
                                showError(e);
                                e.printStackTrace();
                            }
                        }
                    } else {
                        // TODO
                        JOptionPane.showMessageDialog(
                                gui, "TODO - prompt save image..");
                    }
                }
            };
            JMenuItem openItem = new JMenuItem("Open");
            openItem.setMnemonic('o');
            openItem.addActionListener(openListener);
            file.add(openItem);

            ActionListener saveListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser ch = getFileChooser();
                    int result = ch.showSaveDialog(gui);
                    if (result==JFileChooser.APPROVE_OPTION ) {
                        try {
                            File f = ch.getSelectedFile();
                            ImageIO.write(GUI.this.canvasImage, "png", f);
                            GUI.this.originalImage = GUI.this.canvasImage;
                            dirty = false;
                        } catch (IOException ioe) {
                            showError(ioe);
                            ioe.printStackTrace();
                        }
                    }
                }
            };
            JMenuItem saveItem = new JMenuItem("Save");
            saveItem.addActionListener(saveListener);
            saveItem.setMnemonic('s');
            file.add(saveItem);
        }

        if (canExit()) {
            ActionListener exit = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    System.exit(0);
                }
            };
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.setMnemonic('x');
            file.addSeparator();
            exitItem.addActionListener(exit);
            file.add(exitItem);
        }

        return file;
    }

    private void showError(Throwable t) {
        JOptionPane.showMessageDialog(
                gui,
                t.getMessage(),
                t.toString(),
                JOptionPane.ERROR_MESSAGE);
    }

    JFileChooser chooser = null;

    public JFileChooser getFileChooser() {
        if (chooser==null) {
            chooser = new JFileChooser();
            FileFilter ff = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            chooser.setFileFilter(ff);
        }
        return chooser;

    }

    public boolean canExit() {
        boolean canExit = false;
        SecurityManager sm = System.getSecurityManager();
        if (sm==null) {
            canExit = true;
        } else {
            try {
                sm.checkExit(0);
                canExit = true;
            } catch(Exception stayFalse) {
            }
        }

        return canExit;
    }

    public JMenuBar getMenuBar(boolean webstart){
        JMenuBar mb = new JMenuBar();
        mb.add(this.getFileMenu(webstart));
        return mb;
    }

    public static void main(String[] args) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // use default
                }
                GUI bp = new GUI();

                JFrame f = new JFrame("DooDoodle!");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setLocationByPlatform(true);

                f.setContentPane(bp.getGui());
                f.setJMenuBar(bp.getMenuBar(false));

                f.pack();
                f.setMinimumSize(f.getSize());
                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void text(Point point) {
        String text = JOptionPane.showInputDialog(gui, "Text to add", "Text");
        if (text!=null) {
            Graphics2D g = this.canvasImage.createGraphics();
            g.setRenderingHints(renderingHints);
            g.setColor(this.color);
            g.setStroke(stroke);
            int n = 0;
            g.drawString(text,point.x,point.y);
            g.dispose();
            this.imageLabel.repaint();
        }
    }

    public void draw(Point point) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        int n = 0;
        g.drawLine(point.x, point.y, point.x+n, point.y+n);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void rectangle(Rectangle rectangleImage) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        int n = 0;
        g.draw(rectangleImage);
        g.fill(rectangleImage);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void oval(int x, int y, int width, int height) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        g.fillOval(x,y,width,height);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void line(int x, int y, int x2, int y2) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        g.drawLine(x,y,x2,y2);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void curve(int x, int y,int cx,int cy, int x2, int y2) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        QuadCurve2D q = new QuadCurve2D.Float();

        g.dispose();
        this.imageLabel.repaint();
    }

    /*public void drawShape(){
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        g.drawRect(point.x, point.y);


    }*/

    class ImageMouseListener extends MouseAdapter {
/*
* add functionality of rectangle, oval, line functions to mousePressed method.
* Seems a conditional determines what action to take.
*
 */
        @Override
        public void mousePressed(MouseEvent arg0) {
            // TODO Auto-generated method stub
            if (activeTool==GUI.SELECTION_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("select me");
            } else if (activeTool==GUI.DRAW_TOOL) {
                draw(arg0.getPoint());
            } else if (activeTool==GUI.TEXT_TOOL) {
                // TODO
                text(arg0.getPoint());
            } else if (activeTool==GUI.RECTANGLE_TOOL) {
                // TODO
                rectangleStart = arg0.getPoint();
                System.out.println("rectanglearoo");
            } else if (activeTool==GUI.OVAL_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Oval");
                // TODO
                selectionStart = arg0.getPoint();
            } else if (activeTool==GUI.LINE_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Oval");
                // TODO
            } else if (activeTool==GUI.CURVE_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Oval");
                // TODO
            } else {
                JOptionPane.showMessageDialog(
                        gui,
                        "Application error.  :(",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {
            if (activeTool==GUI.SELECTION_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x,
                        arg0.getPoint().y);
            }
            if (activeTool==GUI.RECTANGLE_TOOL) {
                rectangleImage = new Rectangle(
                        rectangleStart.x,
                        rectangleStart.y,
                        arg0.getPoint().x-rectangleStart.x,
                        arg0.getPoint().y-rectangleStart.y);
                rectangle(rectangleImage);
            }

            if (activeTool==GUI.OVAL_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x - selectionStart.x,
                        arg0.getPoint().y- selectionStart.y);
                oval(selection.x,selection.y,selection.width,selection.height);
            }

            if (activeTool==GUI.LINE_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x,
                        arg0.getPoint().y);
                line(selection.x,selection.y,selection.width,selection.height);
            }

            if (activeTool==GUI.CURVE_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x,
                        arg0.getPoint().y);
                line(selection.x,selection.y,selection.width,selection.height);
            }



        }
    }

    class ImageMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent arg0) {
            reportPositionAndColor(arg0);
            if (activeTool==GUI.SELECTION_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x-selectionStart.x,
                        arg0.getPoint().y-selectionStart.y);
            } else if (activeTool==GUI.DRAW_TOOL) {
                draw(arg0.getPoint());
            }  else if (activeTool==GUI.OVAL_TOOL) {

            } else if (activeTool==GUI.LINE_TOOL) {

            }
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
            reportPositionAndColor(arg0);
        }

    }

    private void reportPositionAndColor(MouseEvent me) {
        String text = "";
        if (activeTool==GUI.SELECTION_TOOL) {
            text += "Selection (X,Y:WxH): " +
                    (int)selection.getX() +
                    "," +
                    (int)selection.getY() +
                    ":" +
                    (int)selection.getWidth() +
                    "x" +
                    (int)selection.getHeight();
        } else {
            text += "X,Y: " + (me.getPoint().x+1) + "," + (me.getPoint().y+1);
        }
        output.setText(text);
    }
}