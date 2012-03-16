package uk.ac.shef.dcs.oak.audio.view;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;

public class AudioModel
{
   Player audioPlayer;
   boolean playing = false;
   List<AudioModelListener> listeners = new LinkedList<AudioModelListener>();
   boolean running = true;

   public void addListener(AudioModelListener listener)
   {
      listeners.add(listener);
   }

   public AudioModel(File audioFile)
   {
      loadFile(audioFile);

      Thread updateThread = new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            while (running)
            {
               try
               {
                  Thread.sleep(100);
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }

               if (playing)
                  updateListeners();
            }
         }
      });
      updateThread.start();
   }

   private void updateListeners()
   {
      for (AudioModelListener listener : listeners)
         listener.playbackUpdated();
   }

   public void play()
   {
      audioPlayer.start();
      playing = true;
   }

   public void setPlaybackPerc(double perc)
   {
      audioPlayer.setMediaTime(new Time(audioPlayer.getDuration().getSeconds() * perc));
      updateListeners();
   }

   public double getPlaybackPerc()
   {
      return (audioPlayer.getMediaTime().getSeconds()) / audioPlayer.getDuration().getSeconds();
   }

   private void loadFile(File audioFile)
   {
      try
      {
         audioPlayer = Manager.createRealizedPlayer(audioFile.toURI().toURL());
      }
      catch (NoPlayerException e)
      {
         e.printStackTrace();
      }
      catch (CannotRealizeException e)
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
}
