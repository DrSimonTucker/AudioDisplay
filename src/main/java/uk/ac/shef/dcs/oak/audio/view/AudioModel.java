package uk.ac.shef.dcs.oak.audio.view;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;

public class AudioModel
{
   Player audioPlayer;

   public AudioModel(File audioFile)
   {
      loadFile(audioFile);
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
