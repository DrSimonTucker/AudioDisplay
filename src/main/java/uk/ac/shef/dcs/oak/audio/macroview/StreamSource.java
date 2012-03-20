package uk.ac.shef.dcs.oak.audio.macroview;

import java.io.IOException;
import java.io.InputStream;

import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceStream;

public class StreamSource extends PullDataSource
{
   class InputStreamPullStream implements PullSourceStream, Seekable
   {
      protected InputStream in;
      protected long streamPoint;
      protected long tellPoint;
      protected ContentDescriptor unknownCD = new ContentDescriptor("unknown");

      byte[] headerBuffer;

      int headerPointer = 0;

      public InputStreamPullStream(InputStream in)
      {
         this.in = in;
      }

      // just being tidy
      public void close()
      {
         // We ignore this
      }

      @Override
      public boolean endOfStream()
      {
         try
         {
            System.err.println("REPORTING END OF STREAM");
            return (in.available() == -1);
         }
         catch (IOException ioe)
         {
            return true;
         }
      }

      @Override
      public ContentDescriptor getContentDescriptor()
      {
         return unknownCD;
      }

      // spec'ed by SourceStream

      @Override
      public long getContentLength()
      {
         return SourceStream.LENGTH_UNKNOWN;
      }

      // spec'ed by Controls
      @Override
      public Object getControl(String controlType)
      {
         return null;
      }

      @Override
      public Object[] getControls()
      {
         return EMPTY_OBJECT_ARRAY;
      }

      // spec'ed by Seekable
      @Override
      public boolean isRandomAccess()
      {
         return false;
      }

      public void open() throws IOException
      {
         tellPoint = 0;
      }

      @Override
      public int read(byte[] buf, int off, int length) throws IOException
      {
         // Build the header if we need to
         if (headerPointer == -1)
         {
            System.err.println("Fixing the audio format");
            // FIX THE AUDIO FORMAT
            int sf = in.read() + in.read() * 256;
            int nchannels = in.read();
            int ssize = in.read();
            headerBuffer = new byte[44];
            insertWAVEHeader(headerBuffer, sf, nchannels, ssize);
            headerPointer = 0;
         }
         else
            headerBuffer = new byte[0];

         if (headerPointer < headerBuffer.length)
         {
            int readSize = length - (headerBuffer.length - headerPointer);

            // Fill from the header buffer
            int bufRead = 0;
            int pointerAtStart = headerPointer;
            while (headerPointer < headerBuffer.length && bufRead < length)
            {
               buf[off + bufRead] = headerBuffer[headerPointer++];
               bufRead++;
            }

            // Now add proper data
            int bytesRead = headerPointer - pointerAtStart;
            if (readSize > 0)
               bytesRead += in.read(buf, off + bufRead, readSize);

            return bytesRead;
         }
         else
         {
            int bytesRead = in.read(buf, off, length);
            tellPoint += bytesRead;
            return bytesRead;
         }
      }

      @Override
      public long seek(long position)
      {

         // approach -- if seek is further in than tell,
         // then just skip bytes to get there
         // else close, reopen, and skip to position
         try
         {
            if (position >= tellPoint)
               thoroughSkip(position - tellPoint);
            else
            {
               close();
               open();
               // now skip to this position
               thoroughSkip(position);
            }
            return tellPoint;
         }
         catch (IOException ioe)
         {
            return 0; // bogus... who even knows where we are now?
         }
      }

      @Override
      public long tell()
      {
         return tellPoint;
      }

      public void thoroughSkip(long skipCount) throws IOException
      {
         try
         {
            long totalSkipped = 0;
            while (totalSkipped < skipCount)
            {
               long skipped = in.skip(skipCount - totalSkipped);
               totalSkipped += skipped;
               tellPoint += skipped;
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
            System.exit(1);
         }
      }

      @Override
      public boolean willReadBlock()
      {
         try
         {
            return (in.available() > 0);
         }
         catch (IOException ioe)
         {
            return true;
         }
      }

      private void insertWAVEHeader(byte[] buffer, int sampleFrequency, int noChannels,
            int sampleSize)
      {
         // Insert the preamble
         String preamb1 = "RIFF";
         byte[] bArr = preamb1.getBytes();
         for (int i = 0; i < bArr.length; i++)
            buffer[i] = bArr[i];

         buffer[4] = 10;
         buffer[5] = 0;
         buffer[6] = 0;
         buffer[7] = 0;
         String preamb2 = "WAVEfmt";
         bArr = preamb2.getBytes();
         for (int i = 0; i < bArr.length; i++)
            buffer[i + 8] = bArr[i];
         buffer[15] = 32;
         buffer[16] = 16;
         buffer[17] = 0;
         buffer[18] = 0;
         buffer[19] = 0;
         buffer[20] = 1;
         buffer[21] = 0;
         buffer[22] = (byte) noChannels;
         buffer[23] = 0;

         // Insert the sample rate info (assume sf is < 256*256)
         buffer[24] = (byte) (sampleFrequency % 256);
         buffer[25] = (byte) (sampleFrequency / 256);
         buffer[26] = 0;
         buffer[27] = 0;

         // Insert the byte rate (sf * nch * nbits/8)
         buffer[28] = (byte) ((sampleFrequency * noChannels * sampleSize / 8) % 256);
         buffer[29] = (byte) ((sampleFrequency * noChannels * sampleSize / 8) / 256);
         buffer[30] = 0;
         buffer[31] = 0;

         // Insert the block align ?
         buffer[32] = 4;
         buffer[33] = 0;

         // Insert the bits per sample
         buffer[34] = (byte) sampleSize;
         buffer[35] = 0;

         String preamb3 = "data";
         bArr = preamb3.getBytes();
         for (int i = 0; i < bArr.length; i++)
            buffer[i + 36] = bArr[i];

         // I made this up
         buffer[40] = (byte) 1;
         buffer[41] = (byte) 1;
         buffer[42] = (byte) 1;
         buffer[43] = (byte) 1;
      }
   }

   protected static Object[] EMPTY_OBJECT_ARRAY =
   {};

   PullSourceStream pss;

   InputStream stream;

   public StreamSource(InputStream stream)
   {
      this.stream = stream;
   }

   @Override
   public void connect() throws IOException
   {
   }

   @Override
   public void disconnect()
   {
   }

   @Override
   public String getContentType()
   {
      return FileTypeDescriptor.WAVE;
   }

   @Override
   public Object getControl(String arg0)
   {
      return null;
   }

   @Override
   public Object[] getControls()
   {
      return EMPTY_OBJECT_ARRAY;
   }

   @Override
   public Time getDuration()
   {
      return DataSource.DURATION_UNKNOWN;
   }

   @Override
   public PullSourceStream[] getStreams()
   {
      pss = new InputStreamPullStream(stream);
      return new PullSourceStream[]
      { pss };
   }

   @Override
   public void start() throws IOException
   {
   }

   @Override
   public void stop() throws IOException
   {
   }
}
