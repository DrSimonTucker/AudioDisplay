package uk.ac.shef.dcs.oak.audio.macroview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class FileParser
{
   protected long timeConvert(String in)
   {
      String[] elems = in.split(":");
      if (elems.length == 3)
         return Long.parseLong(elems[0]) * 3600 + Long.parseLong(elems[1]) * 60
               + Long.parseLong(elems[2]);
      else
         return Long.parseLong(elems[0]) * 60 + Long.parseLong(elems[1]);
   }

   public List<AudioSection> readSections(InputStream is) throws IOException
   {
      List<AudioSection> sections = new LinkedList<AudioSection>();

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      int index = 1;

      // Skip the first line
      reader.readLine();

      for (String line = reader.readLine(); line != null; line = reader.readLine())
      {
         String[] elems = line.trim().split(",");

         if (elems.length >= 10)
         {
            AudioSection section = new AudioSection();
            section.audioFile = null;
            if (!elems[4].equals("end") && !elems[4].equals("//"))
               section.endBar = Integer.parseInt(elems[4]);
            section.index = index;
            if (!elems[1].equals("//") && !elems[0].equals("//"))
               section.length = timeConvert(elems[1]) - timeConvert(elems[0]);
            section.movement = Integer.parseInt(elems[9]);
            section.offset = timeConvert(elems[0]);
            section.piece = elems[8];
            if (!elems[2].equals("//"))
               section.startBar = Integer.parseInt(elems[2]);
            section.rehearsal = elems[7];
            section.repeat = 0;

            sections.add(section);
         }
         index++;
      }

      return sections;
   }
}
