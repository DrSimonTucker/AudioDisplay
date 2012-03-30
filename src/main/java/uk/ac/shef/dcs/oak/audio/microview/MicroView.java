package uk.ac.shef.dcs.oak.audio.microview;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JPanel;

public class MicroView extends JPanel
{
   public MicroView(String main, String sub)
   {
      initDisplay(main, sub);
   }

   private void initDisplay(String main, String sub)
   {
      AudioModel model = new AudioModel(new File(sub + ".wav"));
      AudioPanel panel = new AudioPanel(model);
      SympatheticAudioModel sympModel = new SympatheticAudioModel(new File(main), model, new File(
            "path-" + sub), new File("elems-" + sub));
      AudioPanel panel2 = new AudioPanel(sympModel);
      LinkerPanel panel3 = new LinkerPanel(sympModel);

      this.setLayout(new GridLayout(3, 1));
      this.add(panel);
      this.add(panel3);
      this.add(panel2);

      model.play();
   }
}
