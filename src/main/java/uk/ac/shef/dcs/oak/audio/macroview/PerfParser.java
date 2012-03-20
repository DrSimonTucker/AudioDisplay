package uk.ac.shef.dcs.oak.audio.macroview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class PerfParser extends FileParser
{

   @Override
   public List<AudioSection> readSections(InputStream is) throws IOException
   {
      List<AudioSection> sections = new LinkedList<AudioSection>();

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      // Skip the first line which is just the headings
      reader.readLine();
      for (String line = reader.readLine(); line != null; line = reader.readLine())
      {
         System.err.println(line);
         String[] elems = line.trim().split(",");

         if (elems.length == 7)
         {
            AudioSection section = new AudioSection();
            section.audioFile = null;
            section.endBar = Integer.parseInt(elems[3]);
            section.index = 0;
            section.length = timeConvert(elems[5]) - timeConvert(elems[4]);
            section.movement = Integer.parseInt(elems[1]);
            section.offset = timeConvert(elems[4]);
            section.piece = elems[0];
            section.startBar = Integer.parseInt(elems[2]);
            section.repeat = Integer.parseInt(elems[6]);

            sections.add(section);
         }
      }

      return sections;
   }

}
