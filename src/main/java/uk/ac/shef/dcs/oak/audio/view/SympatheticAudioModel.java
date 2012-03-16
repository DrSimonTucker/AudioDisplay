package uk.ac.shef.dcs.oak.audio.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SympatheticAudioModel extends AudioModel implements AudioModelListener
{
   AudioModel sympMod;
   Map<Double, Double> syncMap = new TreeMap<Double, Double>();

   public Map<Double, Double> getSyncMap()
   {
      return syncMap;
   }

   public SympatheticAudioModel(File f, AudioModel mod, File syncDataFile)
   {
      super(f);
      mod.addListener(this);
      loadSyncMap(syncDataFile);
      sympMod = mod;
   }

   @Override
   public void playbackUpdated()
   {
      setPlaybackPerc(resolveSyncMap(sympMod.getPlaybackPerc()));
   }

   private void loadSyncMap(File f)
   {
      try
      {
         double topLeft = 459;
         double topRight = 599;
         BufferedReader reader = new BufferedReader(new FileReader(f));
         syncMap.put(0.0, 0.0);
         syncMap.put(1.0, 1.0);
         for (String line = reader.readLine(); line != null; line = reader.readLine())
         {
            String[] elems = line.trim().split("\\s+");
            Double left = Double.parseDouble(elems[0]);
            Double right = Double.parseDouble(elems[1]);

            syncMap.put(left / topLeft, right / topRight);
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private double resolveSyncMap(double perc)
   {
      Double closestBelow = 1.0;
      Double bestMatchBelow = 0.0;
      Double closestAbove = 1.0;
      Double bestMatchAbove = 1.0;

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
}
