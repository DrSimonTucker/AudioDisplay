package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import uk.ac.shef.dcs.oak.audio.microview.MicroView;

public class MacroDerivedView extends JFrame
{
   MicroView micro;

   AudioSectionPanel sectionPanel;

   public MacroDerivedView()
   {
      try
      {
         build();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void build() throws IOException
   {
      // Load in the performance
      AudioSection performance = new AudioSection();
      performance.setAudioFile("perf-m2.wav");
      performance.setEndBar(1000);
      performance.setStartBar(1);
      performance.setPiece("Haydn");
      performance.setIndex(0);
      performance.setLength(-1);
      performance.setMovement(2);
      performance.setOffset(0);
      performance.setRepeat(0);
      List<AudioSection> perfSections = new LinkedList<AudioSection>();
      perfSections.add(performance);

      List<AudioSection> rehearsals = new LinkedList<AudioSection>();
      BufferedReader reader = new BufferedReader(new FileReader(new File("dfile.txt")));
      for (String line = reader.readLine(); line != null; line = reader.readLine())
      {
         String[] elems = line.trim().split("\\s+");
         int number = Integer.parseInt(elems[0]);
         double perc = Double.parseDouble(elems[2]);

         AudioSection rehearsal = new AudioSection();
         rehearsal.setAudioFile("y" + number + ".wav");
         rehearsal.setStartBar(1000 * perc);
         rehearsal.setEndBar(-performance.getLength());
         rehearsal.setPiece("Haydn");
         rehearsal.setIndex(number);
         rehearsal.setLength(-1);
         rehearsal.setMovement(2);
         rehearsal.setOffset(0);
         rehearsal.setRepeat(0);

         rehearsals.add(rehearsal);
      }

      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      sectionPanel = new AudioSectionPanel(this);
      SelectionPanel selectionPanel = new SelectionPanel(sectionPanel);
      // sectionPanel.loadData("Haydn", 1);

      sectionPanel.setPerformance(perfSections);
      for (AudioSection section : rehearsals)
         sectionPanel.addAudio(section);
      this.add(sectionPanel, BorderLayout.CENTER);
      this.add(selectionPanel, BorderLayout.NORTH);
      this.setSize(500, 522);
      this.setLocationRelativeTo(null);

      this.setFocusable(true);
      // framer.setExtendedState(JFrame.MAXIMIZED_BOTH);
      this.addKeyListener(new KeyAdapter()
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

   public void zoom()
   {
      // Build the necessary micro view
      micro = new MicroView();

      // Flip the two displays
      this.remove(sectionPanel);
      this.add(micro, BorderLayout.CENTER);

      this.validate();
   }

   public static void main(String[] args)
   {
      MacroDerivedView mine = new MacroDerivedView();
      mine.setVisible(true);
   }
}
