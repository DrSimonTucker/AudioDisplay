package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AudioSectionPanel extends JPanel
{
   int BUFFER = 5;
   private AudioSection chosen;
   int chosenIndex = -1;
   Cursor cursor = new Cursor();
   Map<AudioSection, Integer> indexMapper = new HashMap<AudioSection, Integer>();
   Map<Rectangle, AudioSection> mapper = new HashMap<Rectangle, AudioSection>();
   double maxBar = 1000;
   int maxIndex = 0;
   Map<AudioSection, AudioSectionPanel> panelMap = new HashMap<AudioSection, AudioSectionPanel>();
   MacroDerivedView parent;
   List<AudioSection> performance;
   List<AudioSection> sections = new LinkedList<AudioSection>();
   AudioSection[] selected = new AudioSection[1];
   int selectedPointer = 0;

   public AudioSectionPanel(MacroDerivedView par)
   {
      parent = par;
      this.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mousePressed(MouseEvent e)
         {
            if (e.getButton() == 1)
               leftClick(new Point(e.getX(), e.getY()));
            else
               rightClick(new Point(e.getX(), e.getY()));
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

   public String getSelected()
   {
      AudioSection section = selected[(selectedPointer - 1) % selected.length];
      if (section != null)
         return section.getAudioFile().substring(0, section.getAudioFile().length() - 4);
      else
         return "";
   }

   private void leftClick(Point mPoint)
   {

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

      if (cursor.section != null && cursor.section != chosen)
         cursor.section.stop();

      // Move the cursor
      if (chosen != null)
      {
         cursor.moveCursor(chosen, perc, rectang, this);
         chosen.play(perc, this);
         parent.playing(chosen);
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

   private void rightClick(Point mPoint)
   {
      // Locate the AudioSelection chosen
      chosen = null;

      for (Entry<Rectangle, AudioSection> entry : mapper.entrySet())
      {
         Rectangle rect = entry.getKey();
         if (rect.contains(mPoint))
            chosen = entry.getValue();
      }

      // Alter the colour
      chosen.toggleSelect();

      // Update the selected matrix
      int arrIndex = -1;
      for (int i = 0; i < selected.length; i++)
         if (selected[i] == chosen)
            arrIndex = i;
      if (arrIndex >= 0)
      {
         selected[arrIndex] = null;
         selectedPointer = (arrIndex - 1) % selected.length;
      }
      else
      {
         int addPos = (selectedPointer + 1) % selected.length;
         AudioSection lastChosen = selected[addPos];
         if (lastChosen != null)
            lastChosen.toggleSelect();
         selected[addPos] = chosen;
         selectedPointer = addPos;
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

   public void zoom()
   {
      // Need to have at least one audio sections to zoom
      int count = 0;
      for (AudioSection section : selected)
         if (section != null)
            count++;

      if (count > 0 && count < 3)
         parent.zoom();
   }

   public static void main(String[] args)
   {
      AudioSectionPanel panel = new AudioSectionPanel(null);
      panel.loadData("Haydn", 2);
      JFrame framer = new JFrame();
      framer.add(panel);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
   JButton zoomButton;

   public SelectionPanel(AudioSectionPanel in)
   {
      panel = in;
      initDisplay();
   }

   private void initDisplay()
   {
      this.setLayout(new GridLayout(1, 3));

      comboComposer = new JComboBox(new String[]
      { "Haydn" });
      this.add(comboComposer);
      comboComposer.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            reset();
         }

      });

      comboMovement = new JComboBox(new String[]
      { "2" });
      this.add(comboMovement);
      comboMovement.addActionListener(new ActionListener()
      {

         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            reset();
         }

      });

      zoomButton = new JButton("Zoom");
      this.add(zoomButton);
      zoomButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            panel.zoom();
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
