package uk.ac.shef.dcs.oak.audio.view;

import java.awt.Graphics;

import javax.swing.JPanel;

public class AudioPanel extends JPanel
{
   AudioModel model;

   public AudioPanel(AudioModel mod)
   {
      model = mod;
   }

   @Override
   public void paint(Graphics g)
   {
      // Draw a black box around our width
      g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
   }

   public static void main(String[] args)
   {
      AudioModel model = new AudioModel(new File("y4.wav"));
   }
}
