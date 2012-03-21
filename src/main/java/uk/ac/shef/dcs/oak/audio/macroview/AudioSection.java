package uk.ac.shef.dcs.oak.audio.macroview;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.media.Manager;
import javax.media.Player;
import javax.media.Time;

public class AudioSection
{
   private Player aPlayer;
   private String audioFile;
   private Color col;
   private double endBar;
   private final AudioSection following = null;
   private int index;
   private long length;
   private int movement;
   private long offset;
   private String piece;
   private final AudioSection preceeding = null;

   private String rehearsal;

   private int repeat = 0;
   private double startBar;

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

   public boolean contains(double bar)
   {
      return startBar <= bar && endBar >= bar;
   }

   public Player getaPlayer()
   {
      return aPlayer;
   }

   public String getAudioFile()
   {
      return audioFile;
   }

   public double getBar()
   {
      double timePerc = aPlayer.getMediaTime().getSeconds() - offset / length;
      return (endBar - startBar) * timePerc + startBar;
   }

   public Color getCol()
   {
      return col;
   }

   public double getEndBar()
   {
      if (endBar < 0)
      {
         // Compute the end bar
         double length = 0 - endBar;
         double numBars = getLength() * 1000 / length;
         endBar = startBar + numBars;
      }
      return endBar;
   }

   public AudioSection getFollowing()
   {
      return following;
   }

   public int getIndex()
   {
      return index;
   }

   public long getLength()
   {
      if (length < 0)
      {
         // Pull the length from the AudioPlayer info
         initAudio();
         length = (long) (aPlayer.getDuration().getSeconds());
      }
      return length;
   }

   public int getMovement()
   {
      return movement;
   }

   public long getOffset()
   {
      return offset;
   }

   public String getPiece()
   {
      return piece;
   }

   public AudioSection getPreceeding()
   {
      return preceeding;
   }

   public String getRehearsal()
   {
      return rehearsal;
   }

   public int getRepeat()
   {
      return repeat;
   }

   public double getStartBar()
   {
      return startBar;
   }

   private void initAudio()
   {
      if (aPlayer == null)
         try
         {
            InputStream is = getClass().getResourceAsStream(audioFile);
            if (is == null)
               is = new FileInputStream(audioFile);
            aPlayer = Manager.createRealizedPlayer(new StreamSource(is));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
   }

   public void play(double perc, final AudioSectionPanel panel)
   {
      initAudio();
      aPlayer.setMediaTime(new Time(getLength() * perc + offset));
      System.out.println("Playing from " + (getLength() * perc + offset));
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

   public void playBar(double bar, final AudioSectionPanel panel)
   {
      play(startBar + ((bar - startBar) / (endBar - startBar)), panel);
   }

   public void setaPlayer(Player aPlayer)
   {
      this.aPlayer = aPlayer;
   }

   public void setAudioFile(String audioFile)
   {
      this.audioFile = audioFile;
   }

   public void setCol(Color col)
   {
      this.col = col;
   }

   public void setEndBar(double endBar)
   {
      this.endBar = endBar;
   }

   public void setIndex(int index)
   {
      this.index = index;
   }

   public void setLength(long length)
   {
      this.length = length;
   }

   public void setMovement(int movement)
   {
      this.movement = movement;
   }

   public void setOffset(long offset)
   {
      this.offset = offset;
   }

   public void setPiece(String piece)
   {
      this.piece = piece;
   }

   public void setRehearsal(String rehearsal)
   {
      this.rehearsal = rehearsal;
   }

   public void setRepeat(int repeat)
   {
      this.repeat = repeat;
   }

   public void setStartBar(double startBar)
   {
      this.startBar = startBar;
   }

   public void stop()
   {
      aPlayer.stop();
   }
}
