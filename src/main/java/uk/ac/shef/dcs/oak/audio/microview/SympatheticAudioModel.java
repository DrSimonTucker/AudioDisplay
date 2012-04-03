package uk.ac.shef.dcs.oak.audio.microview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SympatheticAudioModel extends AudioModel implements AudioModelListener
{
   Map<Double, Double> adjMap = null;
   int[] cutSamples;
   long endSamples;
   double lowerBound = 0.05;
   Map<Double, Double> revAdjMap = null;
   Map<Double, Double> revSyncMap = new TreeMap<Double, Double>();
   long startSamples;
   boolean stretch = false;
   AudioModel sympMod;
   Map<Double, Double> syncMap = new TreeMap<Double, Double>();
   long totalSamples;

   public SympatheticAudioModel(File f, AudioModel mod, File syncDataFile, File elems)
   {
      super(f);
      super.setActive(false);
      mod.addListener(this);
      loadSyncMap(syncDataFile, elems);
      sympMod = mod;

   }

   public void forcePause()
   {
      super.pause();
   }

   public void forcePlay()
   {
      super.play();
   }

   @Override
   public void forcePlaybackPerc(double perc)
   {
      if (!isActive())
         sympMod.setPlaybackPerc(resolveRevSyncMap(perc));
      else
         super.forcePlaybackPerc(perc);
   }

   public Map<Double, Double> getRevSyncMap()
   {
      // Adjust the syncMap if stretched
      if (stretch)
      {
         if (revAdjMap == null)
         {
            System.out.println("Getting adj map");

            revAdjMap = new TreeMap<Double, Double>();
            double bound = revSyncMap.get(1.0);
            for (Entry<Double, Double> entry : revSyncMap.entrySet())
               revAdjMap
                     .put(entry.getKey(), (entry.getValue() - lowerBound) / (bound - lowerBound));
         }
         return revAdjMap;
      }
      else
         return revSyncMap;
   }

   @Override
   public int[] getSamples()
   {
      if (stretch)
      {
         if (cutSamples == null)
         {
            int[] masterSamples = super.getSamples();
            int offset = ((int) (lowerBound * masterSamples.length));
            System.out.println("OFFSET = " + offset);
            cutSamples = new int[(int) (masterSamples.length * (resolveSyncMap(1.0, true)))
                  - offset];
            for (int i = offset; i < (cutSamples.length + offset); i++)
               cutSamples[i - offset] = masterSamples[i];
         }
         return cutSamples;
      }
      else
         return super.getSamples(startSamples, endSamples);
   }

   public Map<Double, Double> getSyncMap()
   {
      // Adjust the syncMap if stretched
      if (stretch)
      {
         if (adjMap == null)
         {
            System.out.println("Getting adj map");

            adjMap = new TreeMap<Double, Double>();
            double bound = syncMap.get(1.0);
            for (Entry<Double, Double> entry : syncMap.entrySet())
               adjMap.put(entry.getKey(), (entry.getValue() - lowerBound) / (bound - lowerBound));
         }
         return adjMap;
      }
      else
         return syncMap;
   }

   private void loadSyncMap(File f, File elemFile)
   {
      try
      {
         BufferedReader elemReader = new BufferedReader(new FileReader(elemFile));
         String[] bits = elemReader.readLine().trim().split("\\s+");
         System.out.println(bits.length);
         double topLeft = Double.parseDouble(bits[0]);
         double topRight = Double.parseDouble(bits[1]);
         startSamples = (long) Double.parseDouble(bits[2]);
         endSamples = (long) Double.parseDouble(bits[3]);
         BufferedReader reader = new BufferedReader(new FileReader(f));

         // syncMap.put(0.0, 0.0);
         syncMap.put(1.0, 1.0);
         revSyncMap.put(1.0, 1.0);
         for (String line = reader.readLine(); line != null; line = reader.readLine())
         {
            String[] elems = line.trim().split("\\s+");
            Double left = Double.parseDouble(elems[0]);
            Double right = Double.parseDouble(elems[1]);

            revSyncMap.put(right / topRight, left / topLeft);
            syncMap.put(left / topLeft, right / topRight);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @Override
   public void pause()
   {
      System.out.println("PAUSE SYMP ACTIVE: " + isActive());
      if (isActive())
         super.pause();
      else if (sympMod != null)
         sympMod.pause();
   }

   @Override
   public void play()
   {
      System.out.println("PLAY SYMP ACTIVE: " + isActive());
      if (isActive())
         super.play();
      else if (sympMod != null)
         sympMod.play();
   }

   @Override
   public void playbackUpdated()
   {
      if (!isActive())
         setPlaybackPerc(resolveSyncMap(sympMod.getPlaybackPerc()));
   }

   private double resolveRevSyncMap(double perc)
   {
      return resolveRevSyncMap(perc, false);
   }

   private double resolveRevSyncMap(double perc, boolean forceOld)
   {
      Double closestBelow = 1.0;
      Double bestMatchBelow = 0.0;
      Double closestAbove = 1.0;
      Double bestMatchAbove = 1.0;

      if (!forceOld)
         for (Entry<Double, Double> entry : getRevSyncMap().entrySet())
         {
            double diff = perc - entry.getKey();
            if (diff >= 0 && diff < closestBelow)
            {
               closestBelow = diff;
               bestMatchBelow = entry.getValue();
            }
            if (diff <= 0 && Math.abs(diff) < closestAbove)
            {
               closestAbove = diff;
               bestMatchAbove = entry.getValue();
            }
         }
      else
         for (Entry<Double, Double> entry : revSyncMap.entrySet())
         {
            double diff = perc - entry.getKey();
            if (diff >= 0 && diff < closestBelow)
            {
               closestBelow = diff;
               bestMatchBelow = entry.getValue();
            }
            if (diff <= 0 && Math.abs(diff) < closestAbove)
            {
               closestAbove = diff;
               bestMatchAbove = entry.getValue();
            }
         }

      return (bestMatchAbove + bestMatchBelow) / 2;
   }

   private double resolveSyncMap(double perc)
   {
      return resolveSyncMap(perc, false);
   }

   private double resolveSyncMap(double perc, boolean forceOld)
   {
      Double closestBelow = 1.0;
      Double bestMatchBelow = 0.0;
      Double closestAbove = 1.0;
      Double bestMatchAbove = 1.0;

      if (!forceOld)
         for (Entry<Double, Double> entry : getSyncMap().entrySet())
         {
            double diff = perc - entry.getKey();
            if (diff >= 0 && diff < closestBelow)
            {
               closestBelow = diff;
               bestMatchBelow = entry.getValue();
            }
            if (diff <= 0 && Math.abs(diff) < closestAbove)
            {
               closestAbove = diff;
               bestMatchAbove = entry.getValue();
            }
         }
      else
         for (Entry<Double, Double> entry : syncMap.entrySet())
         {
            double diff = perc - entry.getKey();
            if (diff >= 0 && diff < closestBelow)
            {
               closestBelow = diff;
               bestMatchBelow = entry.getValue();
            }
            if (diff <= 0 && Math.abs(diff) < closestAbove)
            {
               closestAbove = diff;
               bestMatchAbove = entry.getValue();
            }
         }

      return (bestMatchAbove + bestMatchBelow) / 2;
   }

   @Override
   protected void setActive(boolean val)
   {
      if (val)
         forcePlay();
      else
         forcePause();
      audio = val;
   }

   @Override
   public void setPlaybackPerc(double perc)
   {
      // Compute this as a percentage of the overall file
      super.setPlaybackPerc(perc);
   }

   public void switchModel()
   {
      if (this.isActive())
      {
         // Switch to the other model
         this.setActive(false);
         sympMod.setActive(true);
      }
      else
      {
         this.setActive(true);
         sympMod.setActive(false);
      }
   }

   @Override
   protected void updateListeners()
   {
      super.updateListeners();

      if (isActive())
         sympMod.setPlaybackPerc(resolveRevSyncMap(getPlaybackPerc()));
   }
}
