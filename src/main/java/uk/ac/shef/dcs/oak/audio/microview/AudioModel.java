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

   boolean audio = true;
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

      final AudioModel modd = this;
      Thread updateThread = new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            while (running)
            {
               try
               {
                  Thread.sleep(1000);
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }

               // System.out.println(modd + " and " +
               // modd.audioPlayer.getMediaTime().getSeconds());

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

   public void forcePlaybackPerc(double perc)
   {
      setPlaybackPerc(perc);
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

   public boolean isActive()
   {
      return audio;
   }

   public boolean isPlaying()
   {
      return playing;
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

   public void pause()
   {
      System.out.println(this + " PAUSED");
      audioPlayer.stop();
      playing = false;
   }

   public void play()
   {
      System.out.println(this + " PLAYING");
      audioPlayer.start();
      playing = true;
   }

   public void rewind()
   {
      audioPlayer.setMediaTime(new Time(0));
   }

   protected void setActive(boolean val)
   {
      audio = val;
      if (val)
         play();
      else
         pause();
   }

   public void setPlaybackPerc(double perc)
   {
      // System.out
      // .println(this + " Setting media time => " + perc + " and " +
      // audioPlayer.getState());
      audioPlayer.setMediaTime(new Time(audioPlayer.getDuration().getSeconds() * perc));
      updateListeners();
   }

   protected void updateListeners()
   {
      for (AudioModelListener listener : listeners)
         listener.playbackUpdated();
   }
}
