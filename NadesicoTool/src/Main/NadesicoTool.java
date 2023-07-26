package Main;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class NadesicoTool {
	public static Scanner scanner = new Scanner(System.in);
	public static String fileName;
	public static byte[] fileBytes;
	
    public static void main(String[] args) {
    	menu();
    }
    
    private static void menu() {
		System.out.println("Press 1 to patch modded values (causes text error).\n" + 
				   		   "Press 2 to patch original values.\n" +
				   		   "Press 3 to generate font image.\n" +
						   "Press 4 to generate seperate character images.\n" +
				   		   "Press 5 to create table text file from /font_tiles folder.");
		int choice = Integer.parseInt(scanner.nextLine());
		choiceProcessor(choice);
	}
    
    private static void choiceProcessor(int choice) {
    	switch (choice) {
        case 1:
            byte modValue = (byte) 0x00; // Copy 0 to register r3 (causes error)
            byte modInstruction = (byte) 0xE3; // mov instruction
            modCharScanner(modValue, modInstruction);

            break;
            
        case 2:
            byte origValue = (byte) 0x52;
            byte origInstruction = (byte) 0x23;
            modCharScanner(origValue, origInstruction);
            
            break;
            
        case 3:
        	generateFontImage();
            
            break;
            
        case 4:
        	generateFontTiles();
            
            break;
            
        case 5:
        	generateFontTable();
            
            break;
    	}
    }
    
    private static void storeFileBytes() {
    	System.out.println("Type the file name you want to edit. (Make sure it's on the same directory).");
    	fileName = scanner.nextLine();
    	try {
            fileBytes = Files.readAllBytes(Paths.get(fileName));
            
        } catch (Exception e) {
            e.printStackTrace();            
        }
    }
    
    private static void modCharScanner(byte value, byte instruction) {
    	storeFileBytes();
    	try {
            int address1 = 0x0011872C; // Value to copy to register r3
            int address2 = 0x0011872D; // mov.l instruction

            fileBytes[address1] = value;
            fileBytes[address2] = instruction;

            Files.write(Paths.get(fileName), fileBytes);
            System.out.println("Character scanner modification successful.");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Character scanner modification unsuccessful.");
        }           
        scanner.close(); 
	}
    
    private static void generateFontImage() {
    	storeFileBytes();
    	
    	System.out.println("What is the length of bytes for the font?");
    	int fontLength = Integer.parseInt(scanner.nextLine());
    	
    	try {
            int numRows = (fileBytes.length + fontLength - 1) / fontLength;
            int paddedRowSize = fontLength * numRows;

            byte[] bmpData = new byte[paddedRowSize];
            System.arraycopy(fileBytes, 0, bmpData, 0, fileBytes.length);

            BufferedImage image = new BufferedImage(fontLength, numRows, BufferedImage.TYPE_BYTE_GRAY);
            image.getRaster().setDataElements(0, 0, fontLength, numRows, bmpData);

            String bmpFileName = "output.bmp";
            FileOutputStream fos = new FileOutputStream(bmpFileName);
            ImageIO.write(image, "BMP", fos);
            fos.close();
            
            System.out.println("Font image generation successful.");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Font image generation unsuccessful.");
        }           
        scanner.close(); 
	}
    
    private static void generateFontTiles() {
        storeFileBytes();

        System.out.println("What is the length of bytes for the font tile?");
        int fontLength = Integer.parseInt(scanner.nextLine());
        
        System.out.println("What is the height of bytes for the font tile?");
        int fontHeight = Integer.parseInt(scanner.nextLine());

        try {
            int numRows = (fileBytes.length + fontLength - 1) / fontLength;
            int paddedRowSize = fontLength * numRows;

            byte[] bmpData = new byte[paddedRowSize];
            System.arraycopy(fileBytes, 0, bmpData, 0, fileBytes.length);

            int numTilesX = fontLength / fontLength;
            int numTilesY = numRows / fontHeight;

            int tileNumber = 0;
            
            String folderName = "font_tiles/";
            File folder = new File(folderName);
            folder.mkdir();

            System.out.println("Please wait...");
            for (int y = 0; y < numTilesY; y++) {
                for (int x = 0; x < numTilesX; x++) {
                    BufferedImage tileImage = new BufferedImage(fontLength, fontHeight, BufferedImage.TYPE_BYTE_GRAY);
                    byte[] tileData = new byte[fontLength * fontHeight];

                    int tileStartX = x * fontLength;
                    int tileStartY = y * fontHeight;

                    for (int row = 0; row < fontHeight; row++) {
                        int sourceRow = tileStartY + row;
                        int sourcePos = sourceRow * fontLength + tileStartX;
                        int destPos = row * fontLength;

                        System.arraycopy(bmpData, sourcePos, tileData, destPos, fontLength);
                    }

                    tileImage.getRaster().setDataElements(0, 0, fontLength, fontHeight, tileData);

                    String bmpDirectory = folderName + "fontTile_" + tileNumber + ".bmp";
                    FileOutputStream fos = new FileOutputStream(bmpDirectory);
                    ImageIO.write(tileImage, "BMP", fos);
                    fos.close();

                    tileNumber++;
                }
            }

            System.out.println("Font images saved to fontTile folder");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Font image generation unsuccessful.");
        }

        scanner.close();
    }
    
    private static void generateFontTable() {
    	String folderPath = "font_tiles/";

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        int fileCounter = 0;

        if (files == null) {
            System.out.println("No font image files found in the folder.");
            return;
        }

        System.out.println("Do you want to rename the files with OCR character recognition? (Y/N)");
        char choice = Character.toUpperCase(scanner.next().charAt(0));

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata/");
        tesseract.setLanguage("jpn");

        try {
        	// Only gets default, replace with one that supports UTF-8 / Shift-JIS
            FileWriter writer = new FileWriter("fontTable.txt");  

            for (File file : files) {
                String imagePath = file.getAbsolutePath();
                if (file.isFile()) {
                    fileCounter += 1;

                    try {
                        String result = tesseract.doOCR(new File(imagePath));
                        String hexNumber = Integer.toHexString(fileCounter);
                        
                        writer.write(hexNumber + "=" + result + "\n");
                        System.out.print(hexNumber + "=" + result + "\n");
                        
                        if (choice == 'Y') {
                            String newFileName = (hexNumber /*+ "=" + result*/ + ".bmp"); // Problem above
                            File newFile = new File(file.getParentFile(), newFileName);
                            file.renameTo(newFile);
                        }
                        
                    } catch (TesseractException e) {
                        System.err.println("Error while performing OCR on " + imagePath);
                        e.printStackTrace();
                    }
                }
            }

            writer.close();
            System.out.println("OCR results written to fontTable.txt");

        } catch (IOException e) {
            System.err.println("Error while writing to the output file.");
            e.printStackTrace();
        }
    }
}