package uk.ac.shef.dcs.oak.audio.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AudioPanel extends JPanel implements AudioModelListener
{
   int[] samples;
   int maxVal = 0;

   @Override
   public void playbackUpdated()
   {
      repaint();
   }

   private final AudioModel model;

   public AudioPanel(AudioModel mod)
   {
      model = mod;
      mod.addListener(this);
      samples = model.getSamples();
      for (int val : samples)
         maxVal = Math.max(val, maxVal);
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

   public static void main(String[] args)
   {
      AudioModel model = new AudioModel(new File("y6.wav"));
      AudioPanel panel = new AudioPanel(model);
      SympatheticAudioModel sympModel = new SympatheticAudioModel(new File("c6-ex-match.wav"),
            model, new File("tpath.path"));
      AudioPanel panel2 = new AudioPanel(sympModel);
      LinkerPanel panel3 = new LinkerPanel(sympModel);

      JFrame framer = new JFrame();
      framer.setLayout(new GridLayout(3, 1));
      framer.add(panel);
      framer.add(panel3);
      framer.add(panel2);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      framer.setExtendedState(JFrame.MAXIMIZED_BOTH);

      model.play();
   }
}
