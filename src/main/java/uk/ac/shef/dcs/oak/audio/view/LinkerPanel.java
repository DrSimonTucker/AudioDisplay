package uk.ac.shef.dcs.oak.audio.view;

import java.awt.Graphics;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

public class LinkerPanel extends JPanel
{
   private final Map<Double, Double> syncMap;

   public LinkerPanel(SympatheticAudioModel am)
   {
      syncMap = am.getSyncMap();
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);

      // Draw in the connector lines
      double matcher = 0;
      for (Entry<Double, Double> entry : syncMap.entrySet())
         if (entry.getKey() > matcher)
         {
            g.drawLine((int) (entry.getKey() * this.getWidth()), 0,
                  (int) (entry.getValue() * this.getWidth()), this.getHeight());
            matcher += 0.1;
         }
   }
}
