package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AudioSectionPanel extends JPanel
{
   /**
	 * 
	 */
   private static final long serialVersionUID = 1464088055416062558L;

   int BUFFER = 5;
   private AudioSection chosen;
   int chosenIndex = -1;
   Cursor cursor = new Cursor();
   Map<AudioSection, Integer> indexMapper = new HashMap<AudioSection, Integer>();
   Map<Rectangle, AudioSection> mapper = new HashMap<Rectangle, AudioSection>();
   double maxBar = 1000;
   int maxIndex = 0;
   Map<AudioSection, AudioSectionPanel> panelMap = new HashMap<AudioSection, AudioSectionPanel>();
   List<AudioSection> performance;

   List<AudioSection> sections = new LinkedList<AudioSection>();

   public AudioSectionPanel()
   {
      this.addMouseListener(new MouseAdapter()
      {

         @Override
         public void mousePressed(MouseEvent e)
         {
            selectionTest(new Point(e.getX(), e.getY()));
            repaint();
         }

      });

   }

   public void addAudio(AudioSection section)
   {
      sections.add(section);
      maxIndex = Math.max(maxIndex, section.getIndex());
      maxBar = Math.max(maxBar, section.getEndBar());
   }

   public void down()
   {

      double bar = chosen.getBar();
      for (int i = chosenIndex + 1; i < sections.size(); i++)
      {
         AudioSection sect = sections.get(i);
         if (sect.contains(bar))
         {
            chosen.stop();
            sect.playBar(bar, panelMap.get(sect));
            break;
         }
      }
   }

   public void loadData(String piece, int movement)
   {
      sections.clear();
      maxBar = 0;
      maxIndex = 0;

      // Build the performance audio section
      FileParser parser = new FileParser();
      PerfParser pparser = new PerfParser();
      int index = 1;
      try
      {
         // Parse the performance data
         InputStream isp = getClass().getResourceAsStream("/etc/perftimings.csv");
         if (isp == null)
            isp = new FileInputStream(new File("src/main/resources/etc/perftimings.csv"));

         System.err.println("READING FROM: "
               + new File("src/main/resources/etc/perftimings.csv").getAbsolutePath());

         Collection<AudioSection> psections = pparser.readSections(isp);
         for (AudioSection section : psections)
            section.setAudioFile("/etc/performance.wav");

         performance = new LinkedList<AudioSection>();
         for (AudioSection section : psections)
            if (section.getMovement() == movement && section.getPiece().equals(piece))
               performance.add(section);
            else
               System.err.println(section.getPiece() + " and " + section.getMovement());

         System.err.println("PERF = " + performance.size());

         for (AudioSection section : performance)
            maxBar = Math.max(maxBar, section.getEndBar());

         InputStream is = getClass().getResourceAsStream("/etc/timings.csv");
         if (is == null)
            is = new FileInputStream(new File("src/main/resources/etc/timings.csv"));
         Collection<AudioSection> sections = parser.readSections(is);
         for (AudioSection section : sections)
            if (section.getPiece().equals(piece) && section.getRehearsal().equals("R1")
                  && section.getMovement() == movement)
            {
               section.setAudioFile("/etc/r1-p2.wav");
               section.setCol(Color.RED);
               section.setIndex(index++);
               addAudio(section);
            }
            else if (section.getPiece().equals(piece) && section.getRehearsal().equals("R2")
                  && section.getMovement() == movement)
            {
               section.setAudioFile("/etc/r2.wav");
               section.setCol(Color.GREEN);
               section.setIndex(index++);
               addAudio(section);
            }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);
      mapper.clear();

      // Paint the performance along the bottom of the screen
      g.setColor(Color.BLACK);
      for (AudioSection section : performance)
         paintBar(g, section.getStartBar() / maxBar, section.getEndBar() / maxBar, 0, section);
      g.setColor(Color.RED);

      // Paint in the other bars
      for (int i = 0; i < sections.size(); i++)
      {
         AudioSection section = sections.get(i);
         paintBar(g, section.getStartBar() / maxBar, section.getEndBar() / maxBar,
               section.getIndex(), section);
      }

      cursor.paintCursor(g);
   }

   private void paintBar(Graphics g, double percStart, double percEnd, int index,
         AudioSection section)
   {
      Color tCol = g.getColor();
      g.setColor(section.getCol());
      int barHeight = ((this.getHeight() - BUFFER * (maxIndex + 1) - BUFFER)) / (maxIndex + 1);

      int leftPos = (int) (BUFFER + (this.getWidth() - 2 * BUFFER) * percStart);
      int rightPos = (int) ((this.getWidth() - 2 * BUFFER) * percEnd) + BUFFER;
      int topPos = this.getHeight() - (index * barHeight + index * BUFFER + BUFFER);
      int botPos = this.getHeight() - ((index + 1) * barHeight + index * BUFFER + BUFFER);

      // Adjust for repeats
      if (section.getRepeat() == 2)
         botPos = (botPos + topPos) / 2 + 2;
      else if (section.getRepeat() == 1)
         topPos = (botPos + topPos) / 2 - 2;

      // Store this in the mapper
      mapper.put(new Rectangle(leftPos, botPos, rightPos - leftPos, topPos - botPos), section);
      if (section == cursor.section)
         cursor.resetRect(new Rectangle(leftPos, botPos, rightPos - leftPos, topPos - botPos), this);

      // Paint in the rectangle
      g.fillRect(leftPos, botPos, rightPos - leftPos, (topPos - botPos));

      g.setColor(tCol);
   }

   private void selectionTest(Point mPoint)
   {
      if (cursor.section != null)
         cursor.section.stop();

      // Locate the AudioSelection chosen
      chosen = null;
      double perc = 0.0;
      Rectangle rectang = null;

      for (Entry<Rectangle, AudioSection> entry : mapper.entrySet())
      {
         Rectangle rect = entry.getKey();
         if (rect.contains(mPoint))
         {
            chosen = entry.getValue();
            perc = (mPoint.getX() - rect.getMinX() + 0.0) / rect.getWidth();
            rectang = rect;
         }
      }

      // Move the cursor
      if (chosen != null)
      {
         cursor.moveCursor(chosen, perc, rectang, this);
         chosen.play(perc, this);
      }
   }

   public void setPerformance(List<AudioSection> section)
   {
      performance = section;
   }

   public void stop()
   {
      chosen.stop();
   }

   public void updateCursorPerc(double val)
   {
      cursor.percPos = val;
      repaint();
   }

   public static void main(String[] args)
   {
      JFrame framer = new JFrame();
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final AudioSectionPanel sectionPanel = new AudioSectionPanel();
      SelectionPanel selectionPanel = new SelectionPanel(sectionPanel);
      sectionPanel.loadData("Haydn", 1);
      framer.add(sectionPanel, BorderLayout.CENTER);
      framer.add(selectionPanel, BorderLayout.NORTH);
      framer.setSize(500, 522);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);

      framer.setFocusable(true);
      framer.addKeyListener(new KeyAdapter()
      {

         @Override
         public void keyPressed(KeyEvent e)
         {
            System.err.println("Pressed " + e.getKeyCode() + "/" + KeyEvent.VK_S);
            if (e.getKeyCode() == KeyEvent.VK_S)
               sectionPanel.stop();
         }

      });
   }
}

class Cursor
{
   JPanel parent;
   double percPos = -1.0;
   boolean playing = false;
   Rectangle rect;
   AudioSection section = null;

   public void moveCursor(AudioSection section, double perc, Rectangle rect, JPanel par)
   {
      this.section = section;
      percPos = perc;
      this.rect = rect;
      this.parent = par;
   }

   public void paintCursor(Graphics g)
   {
      if (section != null && percPos >= 0 && percPos <= 1)
      {
         Color currCol = g.getColor();
         g.setColor(Color.WHITE);
         int xPos = (int) (rect.getMinX() + (rect.getWidth() * percPos));
         g.drawLine(xPos, 0, xPos, parent.getHeight());

         g.setColor(Color.RED);
         xPos = (int) (rect.getMinX() + (rect.getWidth() * percPos));
         g.drawLine(xPos, (int) rect.getMinY(), xPos, (int) rect.getMaxY());
         g.setColor(currCol);

      }
   }

   public void resetRect(Rectangle rect, JPanel par)
   {
      this.rect = rect;
      this.parent = par;
   }
}

class SelectionPanel extends JPanel
{
   JComboBox comboComposer, comboMovement;
   AudioSectionPanel panel;

   public SelectionPanel(AudioSectionPanel in)
   {
      panel = in;
      initDisplay();
   }

   private void initDisplay()
   {
      this.setLayout(new GridLayout(1, 2));

      comboComposer = new JComboBox(new String[]
      { "Haydn" });
      this.add(comboComposer);

      comboMovement = new JComboBox(new String[]
      { "2" });
      this.add(comboMovement);

      comboComposer.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            reset();
         }

      });

      comboMovement.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            reset();
         }

      });

   }

   private void reset()
   {
      // panel.loadData(comboComposer.getSelectedItem().toString(),
      // Integer.parseInt(comboMovement.getSelectedItem().toString()));
      panel.repaint();
   }
}
