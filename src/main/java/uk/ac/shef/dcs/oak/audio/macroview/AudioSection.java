package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;

public class AudioSection
{
   String audioFile;
   double startBar;
   double endBar;
   long offset;
   long length;
   int index;
   String piece;
   int movement;
   String rehearsal;
   Color col;
   int repeat = 0;
   AudioSection following = null;
   AudioSection preceeding = null;

   Player aPlayer;

   public boolean contains(double bar)
   {
      return startBar <= bar && endBar >= bar;
   }

   public double getBar()
   {
      double timePerc = aPlayer.getMediaTime().getSeconds() - offset / length;
      return (endBar - startBar) * timePerc + startBar;
   }

   public void playBar(double bar, final AudioSectionPanel panel)
   {
      play(startBar + ((bar - startBar) / (endBar - startBar)), panel);
   }

   public void play(double perc, final AudioSectionPanel panel)
   {
      try
      {
         InputStream is = getClass().getResourceAsStream(audioFile);
         if (is == null)
            is = new FileInputStream("src/main/resources" + audioFile);
         aPlayer = Manager.createRealizedPlayer(new StreamSource(is));
         aPlayer.setMediaTime(new Time(length * perc + offset));
         aPlayer.start();

         Thread uThread = new Thread(new Runnable()
         {
            @Override
            public void run()
            {
               while (true)
                  try
                  {
                     Thread.sleep(1000);
                     if (aPlayer.getState() == Player.Started
                           && (aPlayer.getMediaTime().getSeconds() - offset < length))
                     {
                        double nPerc = (aPlayer.getMediaTime().getSeconds() - offset) / length;
                        panel.updateCursorPerc(nPerc);
                     }
                     else
                     {
                        aPlayer.stop();
                        break;
                     }

                  }
                  catch (InterruptedException e)
                  {
                     e.printStackTrace();
                  }
            }
         });
         uThread.start();
      }
      catch (CannotRealizeException e)
      {
         e.printStackTrace();
      }
      catch (NoPlayerException e)
      {
         e.printStackTrace();
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void stop()
   {
      aPlayer.stop();
   }

   public AudioSection()
   {
      // Empty constructor
   }

   public AudioSection(double startBar, double endBar, int index)
   {
      this.startBar = startBar;
      this.endBar = endBar;
      this.index = index;
   }
}
