package uk.ac.shef.dcs.oak.audio.microview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class AudioPanel extends JPanel implements AudioModelListener
{
   int maxVal = 0;
   private final AudioModel model;

   int[] samples;

   public AudioPanel(AudioModel mod)
   {
      model = mod;
      mod.addListener(this);
      samples = model.getSamples();
      System.out.println(this + " => " + samples.length);
      for (int val : samples)
         maxVal = Math.max(val, maxVal);

      addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent e)
         {
            double perc = (e.getX() + 0.0) / getWidth();
            model.forcePlaybackPerc(perc);
         }
      });
   }

   @Override
   public void paint(Graphics g)
   {
      super.paint(g);

      // Draw a black box around our width
      g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

      // Draw the playback line
      g.setColor(Color.red);
      int pixPoint = (int) (model.getPlaybackPerc() * this.getWidth());
      g.drawLine(pixPoint, 0, pixPoint, this.getHeight());
      g.setColor(Color.black);

      // Draw the waveform
      if (model.isActive())
         g.setColor(Color.magenta);
      else
         g.setColor(Color.black);

      int counter = samples.length / this.getWidth();
      for (int i = 0; i < this.getWidth(); i++)
      {
         int sum = 0;
         double count = 0.0;
         for (int j = i * counter; j < Math.min((i + 1) * counter, samples.length); j++)
         {
            sum += Math.abs(samples[j]);
            count += 1.0;
         }

         // Get the new point
         // System.out.println((sum / count) + " vs" + maxVal);
         int newPoint = (int) ((sum / count) * (this.getHeight() / 2) / maxVal);

         // Plot the necessary line
         g.drawLine(i, this.getHeight() / 2 + newPoint, i, this.getHeight() / 2 - newPoint);
      }
   }

   @Override
   public void playbackUpdated()
   {
      repaint();
   }

}
