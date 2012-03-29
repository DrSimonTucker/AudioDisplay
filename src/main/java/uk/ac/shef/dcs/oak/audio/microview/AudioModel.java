package uk.ac.shef.dcs.oak.audio.microview;

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
   // The sample frequency
   public static final int fs = 44100;

   /** The audio file represented by the model */
   File audioF;
   Player audioPlayer;
   List<AudioModelListener> listeners = new LinkedList<AudioModelListener>();
   boolean playing = false;
   boolean running = true;

   public AudioModel(File audioFile)
   {
      audioF = audioFile;
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

   public void addListener(AudioModelListener listener)
   {
      listeners.add(listener);
   }

   public double getPlaybackPerc()
   {
      return (audioPlayer.getMediaTime().getSeconds()) / audioPlayer.getDuration().getSeconds();
   }

   public int[] getSamples()
   {
      WavReader reader = new WavReader(audioF);
      return reader.getSamples();
   }

   public int[] getSamples(long start, long end)
   {
      WavReader reader = new WavReader(audioF);
      return reader.getSamples(start, end);
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

   private void updateListeners()
   {
      for (AudioModelListener listener : listeners)
         listener.playbackUpdated();
   }
}
