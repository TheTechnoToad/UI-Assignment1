import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI {

    /**
     * Reference to the original image.
     */
    private BufferedImage originalImage;
    /**
     * Image used to make changes.
     */
    private BufferedImage canvasImage;
    /**
     * The main GUI that might be added to a frame or applet.
     */
    private JPanel gui;
    /**
     * The color to use when calling clear, rectangle or other
     * drawing functionality.
     */
    private Color color = Color.WHITE;
    /**
     * General user messages.
     */
    private JLabel output = new JLabel("Assignment 1");

    private BufferedImage colorSample = new BufferedImage(
            16, 16, BufferedImage.TYPE_INT_RGB);
    private JLabel imageLabel;
    private int activeTool;
    public static final int DRAW_TOOL = 1;
    public static final int RECTANGLE_TOOL = 2;
    public static final int OVAL_TOOL = 3;
    public static final int LINE_TOOL = 4;
    public static final int CURVE_TOOL = 5;

    private Point selectionStart;
    private Rectangle selection;
    private Stroke stroke = new BasicStroke(
            3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.7f);
    private RenderingHints renderingHints;

    public JComponent getGui() {
        if (gui == null) {
            Map<Key, Object> hintsMap = new HashMap<RenderingHints.Key, Object>();
            hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            renderingHints = new RenderingHints(hintsMap);

            setImage(new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB));
            gui = new JPanel(new BorderLayout(4, 4));
            gui.setBorder(new EmptyBorder(5, 3, 5, 3));

            JPanel imageView = new JPanel(new GridBagLayout());
            imageView.setPreferredSize(new Dimension(480, 320));
            imageLabel = new JLabel(new ImageIcon(canvasImage));
            JScrollPane imageScroll = new JScrollPane(imageView);
            imageView.add(imageLabel);
            imageLabel.addMouseMotionListener(new ImageMouseMotionListener());
            imageLabel.addMouseListener(new ImageMouseListener());
            gui.add(imageScroll, BorderLayout.CENTER);

            JToolBar tb = new JToolBar();
            tb.setFloatable(false);
            JButton colorButton = new JButton("Color");
            colorButton.setMnemonic('o');
            colorButton.setToolTipText("Choose a Color");
            ActionListener colorListener = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    Color c = JColorChooser.showDialog(
                            gui, "Please Choose a color", color);
                    if (c != null) {
                        setColor(c);
                    }
                }
            };
            colorButton.addActionListener(colorListener);
            colorButton.setIcon(new ImageIcon(colorSample));
            tb.add(colorButton);

            setColor(color);

            final SpinnerNumberModel strokeModel =
                    new SpinnerNumberModel(3, 1, 16, 1);
            JSpinner strokeSize = new JSpinner(strokeModel);
            ChangeListener strokeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    Object o = strokeModel.getValue();
                    Integer i = (Integer) o;
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
                    clear(canvasImage);
                }
            };
            JButton clearButton = new JButton("Clear");
            tb.add(clearButton);
            clearButton.addActionListener(clearListener);

            gui.add(tb, BorderLayout.PAGE_START);

            JToolBar tools = new JToolBar(JToolBar.VERTICAL);
            tools.setFloatable(false);
            final JRadioButton draw = new JRadioButton("Draw");
            final JRadioButton rectangle = new JRadioButton("Rectangle");
            final JRadioButton oval = new JRadioButton("Oval");
            final JRadioButton line = new JRadioButton("Line");
            final JRadioButton curve = new JRadioButton("Curve");

            tools.add(draw);
            tools.add(rectangle);
            tools.add(oval);
            tools.add(line);
            tools.add(curve);


            ButtonGroup bg = new ButtonGroup();
            bg.add(draw);
            bg.add(rectangle);
            bg.add(oval);
            bg.add(line);
            bg.add(curve);

            ActionListener toolGroupListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (ae.getSource() == draw) {
                        activeTool = DRAW_TOOL;
                    } else if (ae.getSource() == rectangle) {
                        activeTool = RECTANGLE_TOOL;
                    } else if (ae.getSource() == oval) {
                        activeTool = OVAL_TOOL;
                    } else if (ae.getSource() == line) {
                        activeTool = LINE_TOOL;
                    } else if (ae.getSource() == curve) {
                        activeTool = CURVE_TOOL;
                    }
                }
            };
            draw.addActionListener(toolGroupListener);
            rectangle.addActionListener(toolGroupListener);
            oval.addActionListener(toolGroupListener);
            line.addActionListener(toolGroupListener);
            curve.addActionListener(toolGroupListener);

            gui.add(tools, BorderLayout.LINE_END);

            gui.add(output, BorderLayout.PAGE_END);
            clear(colorSample);
            clear(canvasImage);
        }

        return gui;
    }

    /**
     * Clears the entire image area by painting it with the current color.
     */
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
        canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.drawImage(image, 0, 0, gui);
        g.dispose();

        selection = new Rectangle(0, 0, w, h);
        if (this.imageLabel != null) {
            imageLabel.setIcon(new ImageIcon(canvasImage));
            this.imageLabel.repaint();
        }
        if (gui != null) {
            gui.invalidate();
        }
    }

    /**
     * Set the current painting color and refresh any elements needed.
     */
    public void setColor(Color color) {
        this.color = color;
        clear(colorSample);
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
                GUI gui = new GUI();

                JFrame f = new JFrame("UI Assignment1!");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setLocationByPlatform(true);

                f.setContentPane(gui.getGui());

                f.pack();
                f.setMinimumSize(f.getSize());
                f.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void draw(Point point, Graphics2D g) {
        int n = 0;
        g.drawLine(point.x, point.y, point.x + n, point.y + n);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void drawRectangle(int x, int y, int width, int height, Graphics2D g) {
        g.fillRect(x, y, width, height);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void drawOval(int x, int y, int width, int height, Graphics2D g) {
        g.fillOval(x, y, width, height);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void drawLine(int x, int y, int x2, int y2, Graphics2D g) {
        g.drawLine(x, y, x2, y2);
        g.dispose();
        this.imageLabel.repaint();
    }

    public void drawCurve(int x, int y, int x2, int y2, Graphics2D g) {
        g.drawArc(x, y, x2, y2, 0, 90);
        g.dispose();
        this.imageLabel.repaint();
    }

    public Graphics2D createImage() {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        return g;
    }

    class ImageMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent arg0) {
            if (activeTool == GUI.DRAW_TOOL) {
                draw(arg0.getPoint(), createImage());
            } else if (activeTool == GUI.RECTANGLE_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Rectangle Press");
            } else if (activeTool == GUI.OVAL_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Oval Press");
            } else if (activeTool == GUI.LINE_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Line Press");
            } else if (activeTool == GUI.CURVE_TOOL) {
                selectionStart = arg0.getPoint();
                System.out.println("Curve Press");
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
            if (activeTool == GUI.LINE_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x,
                        arg0.getPoint().y);
            } else if (activeTool != GUI.DRAW_TOOL) {
                selection = new Rectangle(
                        selectionStart.x,
                        selectionStart.y,
                        arg0.getPoint().x - selectionStart.x,
                        arg0.getPoint().y - selectionStart.y);
            }
            if (activeTool == GUI.RECTANGLE_TOOL) {
                drawRectangle(selection.x, selection.y, selection.width, selection.height, createImage());
            }

            if (activeTool == GUI.OVAL_TOOL) {
                drawOval(selection.x, selection.y, selection.width, selection.height, createImage());
            }

            if (activeTool == GUI.LINE_TOOL) {
                drawLine(selection.x, selection.y, selection.width, selection.height, createImage());
            }

            if (activeTool == GUI.CURVE_TOOL) {
                drawCurve(selection.x, selection.y, selection.width, selection.height, createImage());
            }


        }
    }

    class ImageMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent arg0) {
            reportPositionAndColor(arg0);
            if (activeTool == GUI.DRAW_TOOL) {
                draw(arg0.getPoint(), createImage());
            }
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
            reportPositionAndColor(arg0);
        }

    }

    private void reportPositionAndColor(MouseEvent me) {
        String text = "";
        text += "X,Y: " + (me.getPoint().x + 1) + "," + (me.getPoint().y + 1);
        output.setText(text);
    }
}