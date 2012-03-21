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
            section.setAudioFile(null);
            section.setEndBar(Integer.parseInt(elems[3]));
            section.setIndex(0);
            section.setLength(timeConvert(elems[5]) - timeConvert(elems[4]));
            section.setMovement(Integer.parseInt(elems[1]));
            section.setOffset(timeConvert(elems[4]));
            section.setPiece(elems[0]);
            section.setStartBar(Integer.parseInt(elems[2]));
            section.setRepeat(Integer.parseInt(elems[6]));

            sections.add(section);
         }
      }

      return sections;
   }

}
