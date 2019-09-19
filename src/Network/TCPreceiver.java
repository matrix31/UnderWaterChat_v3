
/* UnderWater Chat App | Franck Bourzat | IMDEA Network */

package Network;


import Config.csv_read;
import static Config.csv_read.read;
import ConsoleDisplay.display;
import static View.ATConsole.jATdisplay;
import static View.View.*;
import static ConsoleDisplay.display.set;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.lang.Object.*;
import java.nio.file.Files;
import java.nio.file.Path;


public class TCPreceiver extends Thread {

    public Socket socket;

   
    
    private final String checkAT = "+++AT!AR:2:OK\r\n";
    private final String adoptedRemoteAdr="+++AT:7:RADDR";
    private final String BuffnotEmpty = "+++AT!AR:27:ERROR"+" "+"BUFFERS"+" "+"ARE"+" "+"NOT"+" "+"EMPTY\r\n";
    private String str;
    private String str_sub ;
    private String fileName ; 
    private final String[] imgExt = {"ai", "eps","pdf","psd","jpg","jpeg","gif","tif","png","svg"} ;
   
    private final byte txt_byte = 0x01 ;
    private final byte file_byte = 0x02 ;
    private final byte AT_byte = 0x2b;
    private byte byteType;
 
    public static boolean first_fragment = true ;
    public static boolean clickable = true ;//???????,
    
    private int size ;
    private int i = 0 ; 
    private int byteFileSize;
    private int ATcpt = 0 ;
    private long beginningTime; 
    private float ratio; 
    
 

    

    // Constructor : take current socket in argument
    public TCPreceiver(Socket socket) {
        this.socket=socket;
    }
    
    // Listening Thread
   @Override
    public void run(){
        
        ByteArrayOutputStream recept = new ByteArrayOutputStream();
        byte [] fileSizeArray = new byte[4];
        try{
            
       
        while (true){
         
          
         
            try {
                while  ( socket.getInputStream().available()>0){
                 
                    
        
                    DataInputStream  input = new DataInputStream(socket.getInputStream());
                    byte[] ByteArray = new byte[socket.getInputStream().available()];
                    
                    input.read(ByteArray);
                    if (first_fragment){
                    byteType = ByteArray[0];
                    }
 
                    /* first packet received */
                    if (first_fragment){
                        
                        /* Data received from file */
                        if (byteType == file_byte){
                        
                            i++;
                            beginningTime = System.currentTimeMillis();
                            /* Get de size of the file from the Header */
                            System.arraycopy(ByteArray,1,fileSizeArray,0,4);
                            ByteBuffer bytebuff = ByteBuffer.allocate(4);
                            bytebuff = ByteBuffer.wrap(fileSizeArray);
                            
                            
                            size = bytebuff.getInt();
                            
                            /* Get de name of the file from the Header */
                            byteFileSize = ByteArray[5];
                            byte [] byteFileName = new byte[byteFileSize];
                            System.arraycopy(ByteArray,6,byteFileName,0,byteFileSize);
                            fileName = new String(byteFileName);
                            
                            /* Reception */
                            recept.write(ByteArray, byteFileSize+6,ByteArray.length-(byteFileSize+6));
                            byte[] FILE = recept.toByteArray();
                            
                            /* Displays file tranfert progress in %tage */
                            ratio = ((float) FILE.length ) / ((float) size);
                            display Dfile = new display(ratio);
                            Dfile.FilePercent(ratio);
                            
                            first_fragment = false ;
                            
                            
                            /* if no fragmantation induced by the hardware */
                            if ( FILE.length == size){
                                
                                long endTime1 = System.currentTimeMillis();
                                
                                /* File creation */
                                File file = new File("./ChatApp/Files/Received",fileName);
                                FileOutputStream recvFile = new FileOutputStream(file);
                                recvFile.write(FILE);
                                recvFile.close();
                                
                                
                                recept.close();
                                recept.reset();
                                
                                /* Displays */
                                display Displ = new display(fileName,size,beginningTime,endTime1);
                                Displ.FileFeatures(fileName, size, beginningTime, endTime1);
           
                                size = 0;
                                ratio = 0;
                                first_fragment = true ;
                         
                              
                                
                                
                                
                            /* Pop up window with the image file */   
                            
                            /* make fonctions */
                            /* if not an image ->  not displayed */
                            String ext = "";
                            int point = fileName.lastIndexOf('.');
                            if (i > 0) {
                                ext = fileName.substring(point+1);
                            }
                            for ( String extt : imgExt ){       
                                if ( ext.equals(extt)){
                                    
                                    BufferedImage bimg = ImageIO.read(new File("./ChatApp/Files/Received",fileName));

                                    int width = bimg.getWidth();
                                    int height = bimg.getHeight();

                                    JFrame imageFrame = new JFrame();
                                    
                                    imageFrame.setTitle(fileName);
                                    imageFrame.setSize(width, height);
                                    imageFrame.setLocationRelativeTo(null);
                                    imageFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                                    Icon icon = new ImageIcon(fileName);

                                    JLabel label = new JLabel();
                                    label.setIcon(icon);
                                    imageFrame.add(label); // Add to  JFrame
                                    imageFrame.setVisible(true); 
                                
                                }
                                else {}
                            }
                                
                                
                                    
                        }
                        }
                        
                        /* Data for conversation to be displayed */
                        
                        if (byteType == txt_byte){
                            
                            // avoid 
                            for ( int i = 0 ; i < ByteArray.length ; i++){
                                if (ByteArray[i]== 0x26){
                                    if (ByteArray[i+1]== 0x25){
                                        if (ByteArray[i+2]== 0x24){

                                            ByteArray[i] = 0x2b ;
                                            ByteArray[i+1] = 0x2b ;
                                            ByteArray[i+2] = 0x2b ;
                                            
                                        }
                                        
                                    }
                                    
                                }
                            }
                            str = new String(ByteArray) ; // Byte to String
                            str_sub = str.substring(1); // Delete the type byte (first byte)
                            
                     
  
                            jAreaConv.append("["+remoteAdr+"] : "+str_sub+"\n"); // display text
                            jAreaConv.setCaretPosition(jAreaConv.getDocument().getLength()); // auto scroll when adding text
                        }
                        
                        /* AT command for setting the remote address */               
                        if (byteType == AT_byte){
                             ATcpt++; 
       
                            str = new String(ByteArray) ; // convert byte to string

                            if (str.equals(checkAT)){
                                
                                display d = new display();
                                d.adrSetOk();
                                set = false ; 
                               
                        
                        }
                            if ( str.equals((BuffnotEmpty))){
                               // tcpclient.SendAT("+++ATZ4"+"\n");
                                csv_read read = new csv_read();
                                
                    
                                System.out.println("clean buff send");
                               // tcpclient.SendAT(ATadr);
                                /* a tester clean puis reset remote adr*/
                                
                            }
                            /* command for ATconsole */ 
                            if (ATcpt > 1){
                                /*
                                if (str.equals(checkAT)){
                                System.out.println(" ChatApp > Remote Address has been set correctly");
                                System.out.println(" ChatApp > You can chat and send files\n");
                                }
                                */
                                 if(!str.equals(checkAT))
                                jATdisplay.append("    Modem >  "+str+"\n");
                            }
                                
                                
                        
                    
                        
                    }
                    }
                    
                    /* fragments number x received */
                    else {
                        i++;
                        
                        /* Reception */
                        recept.write(ByteArray, 0,ByteArray.length);
                        byte[] FILE = recept.toByteArray();
                        
                      
                        
                        
                        
                       
                      /* Displays file tranfert progress in %tage */
                         ratio = ((float) FILE.length ) / ((float) size);
                         display Dfile = new display(ratio);
                         csv_read read = new csv_read();
                         Dfile.FilePercent(ratio);
                       
                
                        /* Wait the last fragment */
                        if ( FILE.length == size){
                            
                            long endTime2 = System.currentTimeMillis();
                            
                            /* File creation */
                            File file = new File("./ChatApp/Files/Received",fileName);
                            FileOutputStream recvFile = new FileOutputStream(file);
                            recvFile.write(FILE);                         
                            recvFile.close();
                            
                            /* Reset ByteArrayOutputStream */
                            recept.close();
                            recept.reset();
                            
                            /* Displays */
                           display displa = new display(fileName,size,beginningTime,endTime2);
                           displa.FileFeatures(fileName, size, beginningTime, endTime2);
                            
                            
                            
                           
                
                            
                            first_fragment = true ;
                            size = 0;
                            ratio = 0;
               
                            
                            /* Pop up window with the file */
                            
                            String ext = "";
                            int point = fileName.lastIndexOf('.');
                            if (i > 0) {
                                ext = fileName.substring(point+1);
                            }
                            for ( String extt : imgExt ){       
                                if ( ext.equals(extt)){
                                    
                                    
                                    BufferedImage bimg = ImageIO.read(new File("./ChatApp/Files/Received",fileName));
   
                                    int width = bimg.getWidth(); 
                                    int height = bimg.getHeight();                                
                     
                                    JFrame imageFrame = new JFrame();
                                    imageFrame.setTitle(fileName);
                                    imageFrame.setSize(width, height);
                                    imageFrame.setLocationRelativeTo(null);
                                    imageFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                               
                                    JLabel label = new JLabel(new ImageIcon(bimg));
                                    imageFrame.add(label); // Add to  JFrame
                                    imageFrame.setVisible(true); 
                                
                                }
                                else {}
                            }
                               
                            
                        }
                    }
                    
                    /*
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.println("\n");
                    System.out.println("Fragment numero "+i+" : "+ Arrays.toString(ByteArray));
                    System.out.println("\n");
                    System.out.println("Taille du fichier sur 4 bytes : "+ Arrays.toString(fileSizeArray));
                    System.out.println("Taille du fichier "+size);
                    System.out.println("nombre byte du fichier recus jusqu'a maintenant "+byte_nb);
                    System.out.println("Index tableau "+arrayIndex);
                    */
                    
                    
                
            } 
                }  catch (IOException ex) {
                Logger.getLogger(TCPreceiver.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }   
        }
        catch (NullPointerException n){
               
            }
    }
}
            
        


/*******************************************************************************************************/
                      
    
                /*
                int t = ByteArray.length -1 ;
                byte [] test = new byte[t] ;
                System.arraycopy(ByteArray,1,test,0,t); // new array without the header
                Inflater decompressor = new Inflater(); // decompression of the file
                decompressor.setInput(test);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(test.length);
                
                byte[] buf = new byte[8192];
                while (!decompressor.finished()) {
                try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
                } catch (DataFormatException e) {
                }
                }
                try {
                bos.close();
                } catch (IOException e) {
                }
                byte[] decompressedData = bos.toByteArray();
                FileOutputStream fileOut = new FileOutputStream("/home/ubiquity/Downloads/img.jpeg");
                System.out.println("I have received a file");
                fileOut.write(decompressedData);
                System.out.println(Arrays.toString(decompressedData));
                fileOut.close();
                }
                
                else {
                
                }
                }
                System.out.println("nb de byte recu : "+ i);
                
                
                
                /*
                if(str.substring(0,13).equals(adoptedRemoteAdr)){
                System.out.println();
                
                System.out.println("Remote modem "+str.substring(14,15)+" send you a message while you were not connected");
                jAreaConv.append("["+socket.getInetAddress()+"] : "+str.substring(17)+"\n"); // display text
                }
                */
                
                
                
                
                
                
                //     ByteArrayOutputStream bos = new ByteArrayOutputStream(t);
                
                
                //  while(socket.getInputStream().available() >0) {
                //    bos.write(test);
                
                
                
                // }
                
                
                //   byte[] Fragment = bos.toByteArray();
                
                
                
                
                
                
                
                
                
                /*
                byte[] Data = new byte[2192]; // a changer avec taille totale
                
                for ( int j = 0 ; j < 2 ; j++){
                
                System.arraycopy(Fragement,0,Data,j*1024,Fragement.length);
                System.out.println(Arrays.toString(Data));
                }
                
                ByteArrayInputStream bin = new ByteArrayInputStream(Data);
                BufferedImage imageReceived = ImageIO.read(bin);
                File fichier = new File("/home/ubiquity/Downloads/img.jpeg");
                ImageIO.write(imageReceived, "jpeg",fichier);
                System.out.println("I have received a file");
                System.out.println(Arrays.toString(Data));
                
                //  System.out.println(Arrays.toString(decompressedData));
                //  FileOutputStream fileOut = new FileOutputStream("/home/ubiquity/Downloads/img.jpeg");
                //fileOut.write(decompressedData);
                
                
                System.out.println("nb de byte recu : "+ i);
                
                
                
                
                
                int sizeInt = (byteSize & 0xFF) ;
                System.out.println("taille fichier "+ sizeInt);
                System.out.println(byteSize);
                */
 
                // alimenter le tableau finale ici
                // quand fin attente construire image
           

         
   
                   
            

             
                    

                    
    

   

             
             
            
            


           
    
  

  
       
                  
                  
                  
                  
                  
                  
              
                     
                     
      


        
            
                    
                    
                    
                    
                    
                    
                    
                    
                    
  
        

    

                
                
                
                
                
                
   
        
    
 


