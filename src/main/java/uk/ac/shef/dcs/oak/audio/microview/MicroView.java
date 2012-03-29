package uk.ac.shef.dcs.oak.audio.microview;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JPanel;

public class MicroView extends JPanel
{
   public MicroView()
   {
      initDisplay();
   }

   private void initDisplay()
   {
      AudioModel model = new AudioModel(new File("y1-5.wav"));
      AudioPanel panel = new AudioPanel(model);
      SympatheticAudioModel sympModel = new SympatheticAudioModel(new File("perf-m2.wav"), model,
            new File("pathr1-y5.path"));
      AudioPanel panel2 = new AudioPanel(sympModel);
      LinkerPanel panel3 = new LinkerPanel(sympModel);

      this.setLayout(new GridLayout(3, 1));
      this.add(panel);
      this.add(panel3);
      this.add(panel2);

      model.play();
   }
}
