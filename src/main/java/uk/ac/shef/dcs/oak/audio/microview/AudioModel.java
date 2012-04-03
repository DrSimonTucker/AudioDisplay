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
   long end = -1;
   List<AudioModelListener> listeners = new LinkedList<AudioModelListener>();
   boolean playing = false;
   boolean running = true;
   long start = -1;

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
      System.out.println(this + " => " + perc);
      setPlaybackPerc(perc);
   }

   public double getPlaybackPerc()
   {
      if (end > 0)
         return (audioPlayer.getMediaTime().getSeconds() * fs - start) / (end - start);
      else
         return (audioPlayer.getMediaTime().getSeconds()) / audioPlayer.getDuration().getSeconds();
   }

   public int[] getSamples()
   {
      WavReader reader = new WavReader(audioF);
      return reader.getSamples();
   }

   public int[] getSamples(long start, long end)
   {
      this.start = start;
      this.end = end;
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
      // audioPlayer.stop();
      audioPlayer.getGainControl().setMute(true);
      playing = false;
   }

   public void play()
   {
      System.out.println(this + " PLAYING");
      // Set the volume to max
      audioPlayer.getGainControl().setMute(false);
      if (audioPlayer.getState() != Player.Started)
      {
         System.out.println(this + " STARTING");
         audioPlayer.start();
      }
      playing = true;
   }

   public void rewind()
   {
      audioPlayer.setMediaTime(new Time(0));
   }

   protected void setActive(boolean val)
   {
      if (val)
         play();
      else
         pause();
      audio = val;
   }

   public void setPlaybackPerc(double perc)
   {
      double actualPerc = perc;
      if (end > 0)
      {
         double samps = (end - start) * perc;
         double overallSamps = samps + start;
         actualPerc = overallSamps / (audioPlayer.getDuration().getSeconds() * fs);
      }
      System.out.println(this + " Setting media time => " + perc + "," + actualPerc + " and "
            + audioPlayer.getState() + " and " + audioPlayer.getDuration().getSeconds() + " given "
            + end);
      audioPlayer.setMediaTime(new Time(audioPlayer.getDuration().getSeconds() * actualPerc));
      updateListeners();
   }

   protected void updateListeners()
   {
      // System.out.println("Updating: " + listeners.size());
      for (AudioModelListener listener : listeners)
         listener.playbackUpdated();
   }
}
