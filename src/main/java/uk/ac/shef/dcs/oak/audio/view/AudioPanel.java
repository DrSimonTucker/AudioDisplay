package uk.ac.shef.dcs.oak.audio.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AudioPanel extends JPanel implements AudioModelListener
{
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
   }

   public static void main(String[] args)
   {
      AudioModel model = new AudioModel(new File("y4.wav"));
      AudioPanel panel = new AudioPanel(model);
      AudioModel sympModel = new SympatheticAudioModel(new File("c6-ex-match.wav"), model,
            new File("tpath.path"));
      AudioPanel panel2 = new AudioPanel(sympModel);

      JFrame framer = new JFrame();
      framer.setLayout(new GridLayout(2, 1));
      framer.add(panel);
      framer.add(panel2);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setVisible(true);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      model.play();
   }
}
