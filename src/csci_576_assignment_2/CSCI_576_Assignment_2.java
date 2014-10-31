//////////////////////////////////////////////////////
//Name: Shreyansh Daga
//USC ID: 6375 3348 33
//Assignment: #2 CSCI 576
//Date: 3/14/2012
//File: CSCI_576_Assignment_2
///////////////////////////////////////////////////////

package csci_576_assignment_2;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.exit;
import javax.swing.*;
import sun.awt.im.InputMethodJFrame;


/**
 *
 * @author Shreyansh
 */
public class CSCI_576_Assignment_2 
{
    static BufferedImage imgIP;
    static JFrame frmIP, frmOP;
    static JLabel lblIP, lblOP;
    static double[][] daCos = new double[8][8];
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        // TODO code application logic here
        String strFileName = args[0];
        int  iQL = Integer.parseInt(args[1]);
        int iDM = Integer.parseInt(args[2]);
        int iLat = Integer.parseInt(args[3]);
        BufferedImage[] imgOP = new BufferedImage[64];
        int[][][] iaDCT;
        
        if(iDM > 7)
        {
            System.out.println("Error !! Quantization Level should be less than 8 and greater than equal to 0");
            exit(0);
        }                       
        
        InitWinParams();
        
        imgIP = ReadImage(strFileName, 352, 288);
        
        DisplayImage(imgIP, 0);
        
        for(int i = 0;i<8;i++)
        {
            for(int j = 0;j<8;j++)
            {
                daCos[i][j] = cos((2*i+1)*j*3.14159/16.00);
            }
        }
        
        iaDCT = DCT_Quantized(imgIP, iQL);        
        
        if(iDM == 1)
            IDCT_BLockWise(iaDCT, iQL, iLat);
        else if(iDM == 2)
            IDCT_Progressive(iaDCT, iQL, iLat);
        else if(iDM == 3)
            IDCT_SBA(iaDCT, iQL, iLat);                                               
    }
    
    public static void InitWinParams()
    {
        frmIP = new JFrame();
        frmOP = new JFrame();
        
        frmIP.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frmOP.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);                
        
        lblIP = new JLabel();
        lblOP = new JLabel();
    }
    
    //Reads the raw SGI image
    public static BufferedImage ReadImage(String strImageName, int iW, int iH) 
    {
        BufferedImage imgNew = new BufferedImage(iW, iH, BufferedImage.TYPE_INT_RGB);

        try 
        {
            File file = new File(strImageName);
            InputStream is = new FileInputStream(file);

            long len = file.length();
            byte[] bytes = new byte[(int) len];

            int offset = 0;
            int numRead = 0;

            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) 
            {
                offset += numRead;
            }

            int ind = 0;

            for (int y = 0; y < iH; y++) 
            {
                for (int x = 0; x < iW; x++) 
                {
                    //byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + iH * iW];
                    byte b = bytes[ind + iH * iW * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    imgNew.setRGB(x, y, pix);
                    ind++;
                    //System.out.println("Ind: " + ind + " X: " + x + " Y: " + y);
                }
            }
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return imgNew;
    }
    
    public static void DisplayImage(BufferedImage imgOP, int iType)
    {
        if(iType == 0)//Input Image
        {            
            lblIP.setIcon(new ImageIcon(imgOP));            
            frmIP.getContentPane().add(lblIP,BorderLayout.CENTER);
            frmIP.setLocation(20, 20);
            frmIP.pack();
            frmIP.setVisible(true);
        }
        else if(iType == 1)//Output Image
        {
            
            lblOP.setIcon(new ImageIcon(imgOP));
            frmOP.getContentPane().removeAll();
            frmOP.getContentPane().add(lblOP,BorderLayout.CENTER);
            frmOP.setLocation(frmIP.getWidth() + 20, 20);
            frmOP.pack();
            frmOP.setVisible(true);
        }
    }
    
    public static int[][][] DCT_Quantized(BufferedImage imgIP, int iQL)
    {
        BufferedImage imgOP = new BufferedImage(352,288,imgIP.getType());
        int[][][] iaDCTImage = new int[352][288][3];
        int iH = imgIP.getHeight();
        int iW = imgIP.getWidth();               
        
        for(int i = 0;i<iW;i+=8)
        {
            for(int j = 0;j<iH;j+=8)
            {                
                for(int u = 0;u<8;u++)
                {
                    for(int v = 0;v<8;v++)
                    {                        
                        float fCu = 1.0f, fCv = 1.0f;
                        float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;
                        
                        if(u == 0)
                            fCu =  0.707f;
                        if(v == 0)
                            fCv = 0.707f;
                                                     
                        for(int x = 0;x<8;x++)
                        {
                            for(int y = 0;y<8;y++)
                            {                                
                                int iR, iG, iB;                                
                                
                                iR = (imgIP.getRGB(i+x,j+y)>>16) & 0xFF;
                                iG = (imgIP.getRGB(i+x,j+y)>>8) & 0xFF;
                                iB = imgIP.getRGB(i+x,j+y) & 0xFF;
                                
                                //fRRes += iR * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));
                                //fGRes += iG * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));
                                //fBRes += iB * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));
                                fRRes += iR*daCos[x][u]*daCos[y][v];
                                fGRes += iG*daCos[x][u]*daCos[y][v];
                                fBRes += iB*daCos[x][u]*daCos[y][v];
                                
                            }
                        }                        
                        iaDCTImage[i+u][j+v][0] = (int) Math.round(fRRes * 0.25*fCu*fCv/Math.pow(2, iQL));
                        iaDCTImage[i+u][j+v][1] = (int) Math.round(fGRes * 0.25*fCu*fCv/Math.pow(2, iQL));
                        iaDCTImage[i+u][j+v][2] = (int)Math.round(fBRes * 0.25*fCu*fCv/Math.pow(2, iQL));
                    }
                }
            }
        }                
                
        return iaDCTImage;
    }
    
    public static BufferedImage IDCT(int[][][] iaDCT, int iQL, int Row, int Col)
    {        
        int[][][] iaIDCTImage = new int[352][288][3];
        int iH = imgIP.getHeight();
        int iW = imgIP.getWidth();
        
        BufferedImage imgOP = new BufferedImage(352, 288, imgIP.getType());
        
        for(int i = 0;i<iW;i+=8)
        {
            for(int j = 0;j<iH;j+=8)
            {                                
                for(int x = 0;x<8;x++)
                {
                    for(int y = 0;y<8;y++)
                    {                                                
                        float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;                                                    
                        
                        for(int u = 0;u<8;u++)
                        {
                            for(int v = 0;v<8;v++)
                            {
                                float fCu = 1.0f, fCv = 1.0f;                                
                                if(u == 0)
                                    fCu =  0.707f;
                                if(v == 0)
                                    fCv = 0.707f;
                                
                                double iR, iG, iB;                                                                
                                if(u < Col && v < Row)
                                {
                                    iR = iaDCT[i + u][j + v][0] * Math.pow(2, iQL);
                                    iG = iaDCT[i + u][j + v][1] * Math.pow(2, iQL);
                                    iB = iaDCT[i + u][j + v][2] * Math.pow(2, iQL);
                                }
                                else
                                {
                                    iG = 0;//iaDCT[i + u][j + v][1] * (2^iQL);
                                    iB = 0;//iaDCT[i + u][j + v][2] * (2^iQL);
                                    iR =  0;//iaDCT[i + u][j + v][0] * (2^iQL);                                    
                                }
                                
                                //IDCT Logic                               
                                //fRRes += fCu * fCv * iR * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));
                                //fGRes += fCu * fCv * iG * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));
                                //fBRes += fCu * fCv * iB * cos((2*x+1)*(u*3.14159)/(16.00f)) * cos((2*y+1)*(v*3.14159)/(16.00f));                                                                
                                fRRes += fCu * fCv * iR*daCos[x][u]*daCos[y][v];
                                fGRes += fCu * fCv * iG*daCos[x][u]*daCos[y][v];
                                fBRes += fCu * fCv * iB*daCos[x][u]*daCos[y][v];
                            }
                        }
                        
                        fRRes *= 0.25;
                        fGRes *= 0.25;
                        fBRes *= 0.25;                        
                        
                        if(fRRes <= 0)
                            fRRes = 0;
                        else if(fRRes >= 255)
                            fRRes = 255;
                            
                        if(fGRes <= 0)
                            fGRes = 0;
                        else if(fGRes >= 255)
                            fGRes = 255;
                            
                        if(fBRes <= 0)
                            fBRes = 0;
                        else if(fBRes >= 255)
                            fBRes = 255; 
                        
                        iaIDCTImage[i + x][j + y][0]  = (int)fRRes;
                        iaIDCTImage[i + x][j + y][1]  = (int)fGRes;
                        iaIDCTImage[i + x][j + y][2]  = (int)fBRes;
                    }
                }                                               
            }
        }
        
        for(int i = 0;i<iW;i++)
        {
            for(int j = 0;j<iH;j++)
            {                
                int iColor = 0xff000000 | ((iaIDCTImage[i][j][0] & 0xff) << 16) | ((iaIDCTImage[i][j][1] & 0xff) << 8) | (iaIDCTImage[i][j][2] & 0xff);
                
                imgOP.setRGB(i, j, iColor);
            }
        }
        
        return imgOP;
    }
    
    public static void IDCT_BLockWise(int[][][] iaDCT, int iQL, int iLat)
    {
        int[][][] iaIDCTImage = new int[352][288][3];
        int iH = imgIP.getHeight();
        int iW = imgIP.getWidth();
        
        BufferedImage imgOP = new BufferedImage(352, 288, imgIP.getType());
        
        long iTime = System.currentTimeMillis();
        for(int i = 0;i<iH;i+=8)
        {
            for(int j = 0;j<iW;j+=8)
            {                                    
                for(int x = 0;x<8;x++)
                {
                    for(int y = 0;y<8;y++)
                    {                                                
                        float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;                                                    
                        
                        for(int u = 0;u<8;u++)
                        {
                            for(int v = 0;v<8;v++)
                            {
                                float fCu = 1.0f, fCv = 1.0f;                                
                                if(u == 0)
                                    fCu =  0.707f;
                                if(v == 0)
                                    fCv = 0.707f;
                                
                                double iR, iG, iB;                                                                                                
                                    
                                iR = iaDCT[j + u][i + v][0] * Math.pow(2, iQL);
                                iG = iaDCT[j + u][i + v][1] * Math.pow(2, iQL);
                                iB = iaDCT[j + u][i + v][2] * Math.pow(2, iQL);                                    
                                
                                //IDCT Logic                               
                                fRRes += fCu * fCv * iR * daCos[x][u]*daCos[y][v];
                                fGRes += fCu * fCv * iG * daCos[x][u]*daCos[y][v];
                                fBRes += fCu * fCv * iB * daCos[x][u]*daCos[y][v];
                            }
                        }
                        
                        fRRes *= 0.25;
                        fGRes *= 0.25;
                        fBRes *= 0.25;                        
                        
                        if(fRRes <= 0)
                            fRRes = 0;
                        else if(fRRes >= 255)
                            fRRes = 255;
                            
                        if(fGRes <= 0)
                            fGRes = 0;
                        else if(fGRes >= 255)
                            fGRes = 255;
                            
                        if(fBRes <= 0)
                            fBRes = 0;
                        else if(fBRes >= 255)
                            fBRes = 255; 
                        
                        iaIDCTImage[j + x][i + y][0]  = (int)fRRes;
                        iaIDCTImage[j + x][i + y][1]  = (int)fGRes;
                        iaIDCTImage[j + x][i + y][2]  = (int)fBRes;
                        
                        int iColor = 0xff000000 | ((iaIDCTImage[j+x][i+y][0] & 0xff) << 16) | ((iaIDCTImage[j+x][i+y][1] & 0xff) << 8) | (iaIDCTImage[j+x][i+y][2] & 0xff);
                        imgOP.setRGB(j+x, i+y, iColor);
                    }
                }                                                              
                
                DisplayImage(imgOP, 1);
                try
                {
                    Thread.sleep((int) iLat);
                } 
                catch (InterruptedException ex) 
                {
                    Thread.currentThread().interrupt();
                }                
            }                        
        }                                               
    }
    
    public static void IDCT_Progressive(int[][][] iaDCT, int iQL, int iLat)
    {        
        int[][][] iaIDCTImage = new int[352][288][3];
        int iH = imgIP.getHeight();
        int iW = imgIP.getWidth();
        int iFreqCount = 0;
        BufferedImage imgOP = new BufferedImage(352, 288, imgIP.getType());
        
        long iTime = System.currentTimeMillis();
        while(iFreqCount < 64)
        {
            for(int i = 0;i<iH;i+=8)
            {
                for(int j = 0;j<iW;j+=8)
                {                                    
                    for(int x = 0;x<8;x++)
                    {
                        for(int y = 0;y<8;y++)
                        {                                                
                            float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;                                                    
                        
                            for(int u = 0;u<8;u++)
                            {
                                for(int v = 0;v<8;v++)
                                {
                                    float fCu = 1.0f, fCv = 1.0f;                                
                                    if(u == 0)
                                        fCu =  0.707f;
                                    if(v == 0)
                                        fCv = 0.707f;
                                
                                    double iR, iG, iB;                                                                                                
                                    if((u*8 + v) <= (iFreqCount))
                                    {
                                        iR = iaDCT[j + u][i + v][0] * Math.pow(2, iQL);
                                        iG = iaDCT[j + u][i + v][1] * Math.pow(2, iQL);
                                        iB = iaDCT[j + u][i + v][2] * Math.pow(2, iQL);                                    
                                    }
                                    else
                                    {
                                        iR = 0;
                                        iG = 0;
                                        iB = 0;
                                    }
                                    //IDCT Logic                               
                                    fRRes += fCu * fCv * iR * daCos[x][u]*daCos[y][v];
                                    fGRes += fCu * fCv * iG * daCos[x][u]*daCos[y][v];
                                    fBRes += fCu * fCv * iB * daCos[x][u]*daCos[y][v];
                                }
                            }
                        
                            fRRes *= 0.25;
                            fGRes *= 0.25;
                            fBRes *= 0.25;
                        
                            if(fRRes <= 0)
                                fRRes = 0;
                            else if(fRRes >= 255)
                                fRRes = 255;
                            
                            if(fGRes <= 0)
                                fGRes = 0;
                            else if(fGRes >= 255)
                                fGRes = 255;
                            
                            if(fBRes <= 0)
                                fBRes = 0;
                            else if(fBRes >= 255)
                                fBRes = 255; 
                        
                            iaIDCTImage[j + x][i + y][0]  = (int)fRRes;
                            iaIDCTImage[j + x][i + y][1]  = (int)fGRes;
                            iaIDCTImage[j + x][i + y][2]  = (int)fBRes;
                        
                            int iColor = 0xff000000 | ((iaIDCTImage[j+x][i+y][0] & 0xff) << 16) | ((iaIDCTImage[j+x][i+y][1] & 0xff) << 8) | (iaIDCTImage[j+x][i+y][2] & 0xff);
                            imgOP.setRGB(j+x, i+y, iColor);
                        }
                    }
                }
            }
        
            DisplayImage(imgOP, 1);
            try 
            {
                Thread.sleep((int) iLat);
            } 
            catch (InterruptedException ex) 
            {
                Thread.currentThread().interrupt();
            }
            iFreqCount++;
            System.out.println("FreqComponent: " + iFreqCount);
        }            
    }
    
    public static void IDCT_SBA(int[][][] iaDCT, int iQL, int iLat)
    {
        int[][][] iaIDCTImage = new int[352][288][3];
        int iH = imgIP.getHeight();
        int iW = imgIP.getWidth();
        Integer bMask = 0x07FF;
        Integer bMask_1;// = 0x7FFFFFFF;
        
        BufferedImage imgOP = new BufferedImage(352, 288, imgIP.getType());
        int b = 0;
        
        while(b < 12)
        {
            for(int i = 0;i<iH;i+=8)
            {
                for(int j = 0;j<iW;j+=8)
                {                                    
                    for(int x = 0;x<8;x++)
                    {
                        for(int y = 0;y<8;y++)
                        {                                                
                            float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;                                                    
                        
                            for(int u = 0;u<8;u++)
                            {
                                for(int v = 0;v<8;v++)
                                {
                                    float fCu = 1.0f, fCv = 1.0f;                                
                                    if(u == 0)
                                        fCu =  0.707f;
                                    if(v == 0)
                                        fCv = 0.707f;
                                
                                    int iR, iG, iB;                                                                                                
                                    double dR, dG, dB;
                                    
                                    iR = (int) (iaDCT[j + u][i + v][0] * Math.pow(2, iQL));
                                    iG = (int) (iaDCT[j + u][i + v][1] * Math.pow(2, iQL));
                                    iB = (int) (iaDCT[j + u][i + v][2] * Math.pow(2, iQL));
                                
                                    bMask_1 = ~bMask;
                                    //SBA
                                    if(iR < 0)
                                    {
                                        iR *= -1;                                        
                                        iR = iR & (bMask_1);
                                        iR *= -1;
                                    }
                                    else
                                    {
                                        iR = iR & (bMask_1);
                                    }
                                    if(iG < 0)
                                    {
                                        iG *= -1;
                                        iG = iG & (bMask_1);
                                        iG *= -1;
                                    }
                                    else
                                    {
                                        iG = iG & (bMask_1);
                                    }
                                    if(iB < 0)
                                    {
                                        iB *= -1;
                                        iB = iB & (bMask_1);
                                        iB *= -1;
                                    }
                                    else
                                    {
                                        iB = iB & (bMask_1);
                                    }
                                
                                    dR = iR;// *  Math.pow(2, iQL);
                                    dG = iG;// *  Math.pow(2, iQL);
                                    dB = iB;// *  Math.pow(2, iQL);
                                
                                    //IDCT Logic                               
                                    fRRes += fCu * fCv * dR * daCos[x][u]*daCos[y][v];
                                    fGRes += fCu * fCv * dG * daCos[x][u]*daCos[y][v];
                                    fBRes += fCu * fCv * dB * daCos[x][u]*daCos[y][v];
                                }
                            }
                        
                            fRRes *= 0.25;
                            fGRes *= 0.25;
                            fBRes *= 0.25;                        
                        
                            if(fRRes <= 0)
                                fRRes = 0;
                            else if(fRRes >= 255)
                                fRRes = 255;
                            
                            if(fGRes <= 0)
                                fGRes = 0;
                            else if(fGRes >= 255)
                                fGRes = 255;
                            
                            if(fBRes <= 0)
                                fBRes = 0;
                            else if(fBRes >= 255)
                                fBRes = 255; 
                        
                            iaIDCTImage[j + x][i + y][0]  = (int)fRRes;
                            iaIDCTImage[j + x][i + y][1]  = (int)fGRes;
                            iaIDCTImage[j + x][i + y][2]  = (int)fBRes;    
                            
                            int iColor = 0xff000000 | ((iaIDCTImage[j+x][i+y][0] & 0xff) << 16) | ((iaIDCTImage[j+x][i+y][1] & 0xff) << 8) | (iaIDCTImage[j+x][i+y][2] & 0xff);
                            imgOP.setRGB(j+x, i+y, iColor);
                        }
                    }                                                                                                           
                }                        
            }             
        
            DisplayImage(imgOP, 1);
            try 
            {
                Thread.sleep((int) iLat);
            } 
            catch (InterruptedException ex) 
            {
                Thread.currentThread().interrupt();
            }
            
            bMask = bMask >> 1;
            b++;
            System.out.println("Bit: " + (13 - b));
        }  
    }
}
