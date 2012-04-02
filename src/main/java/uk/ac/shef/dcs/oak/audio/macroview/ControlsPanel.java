package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ControlsPanel extends JPanel
{
   AudioSection base = null;

   JButton fwdButton, rewButton, playButton;

   public ControlsPanel()
   {
      initGUI();
   }

   private void initGUI()
   {
      // fwdButton = new JButton("Fwd");
      // fwdButton.setEnabled(false);
      // this.add(fwdButton);

      rewButton = new JButton("Rew");
      rewButton.setEnabled(false);
      this.add(rewButton);
      rewButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            if (base != null)
               base.rewind();
         }
      });

      playButton = new JButton("Play");
      playButton.setEnabled(false);
      this.add(playButton);
      playButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent arg0)
         {
            if (playButton.getText().equals("Play"))
            {
               if (base != null)
                  base.play();
               playButton.setText("Pause");
            }
            else
            {
               if (base != null)
                  base.pause();
               playButton.setText("Play");
            }
         }
      });

   }

   public void setSection(AudioSection section)
   {
      base = section;

      // fwdButton.setEnabled(true);
      rewButton.setEnabled(true);
      playButton.setEnabled(true);
      if (section.isPlaying())
         playButton.setText("Pause");
      else
         playButton.setText("Play");
   }
}
