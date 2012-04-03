package uk.ac.shef.dcs.oak.audio.microview;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JPanel;

public class MicroView extends JPanel
{
   AudioModel model;
   SympatheticAudioModel sympModel;

   public MicroView(String main, String sub)
   {
      initDisplay(main, sub);
   }

   public SympatheticAudioModel getModel()
   {
      return sympModel;
   }

   private void initDisplay(String main, String sub)
   {
      model = new AudioModel(new File(sub + ".wav"));
      AudioPanel panel = new AudioPanel(model);
      sympModel = new SympatheticAudioModel(new File(main), model, new File("path-" + sub),
            new File("elems-" + sub));
      AudioPanel panel2 = new AudioPanel(sympModel);
      LinkerPanel panel3 = new LinkerPanel(sympModel);

      this.setLayout(new GridLayout(3, 1));
      this.add(panel);
      this.add(panel3);
      this.add(panel2);

      model.play();
   }

   public void stop()
   {
      sympModel.pause();
      model.pause();
   }
}
